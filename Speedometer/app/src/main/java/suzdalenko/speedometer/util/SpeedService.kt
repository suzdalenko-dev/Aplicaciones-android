package suzdalenko.speedometer.util
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import suzdalenko.speedometer.R
import suzdalenko.speedometer.util.App.Companion.MAX_SPEED_ENTRIES
import suzdalenko.speedometer.util.App.Companion.NOTIFICATION_CHANNEL_ID
import suzdalenko.speedometer.util.App.Companion.NOTIFICATION_ID
import suzdalenko.speedometer.util.App.Companion.fuseSensorData
import suzdalenko.speedometer.util.App.Companion.gnssCallback
import suzdalenko.speedometer.util.App.Companion.handler
import suzdalenko.speedometer.util.App.Companion.locationListener
import suzdalenko.speedometer.util.App.Companion.locationManager
import suzdalenko.speedometer.util.App.Companion.previousLocation
import suzdalenko.speedometer.util.App.Companion.speedList
import suzdalenko.speedometer.util.App.Companion.speedViewModel
import suzdalenko.speedometer.util.App.Companion.startTime
import suzdalenko.speedometer.util.App.Companion.totalDistanceInMeters
import suzdalenko.speedometer.util.App.Companion.updateTimeRunnable
import suzdalenko.speedometer.util.App.Companion.velocity2

class SpeedService : Service() {

    override fun onCreate() {
        super.onCreate()

        // Configure notification and start foreground service
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Iniciando medición de velocidad..."))


        startLocationUpdates()
        startUpdatingElapsedTime()
    }

    private fun startLocationUpdates() {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude

                if (previousLocation != null) {

                    val distanceInMeters = location.distanceTo(previousLocation!!)

                    if (location.speed * 3.6 > 1.0 && distanceInMeters > 3) {
                        addSpeedToList(location.speed * 3.6)
                        totalDistanceInMeters += distanceInMeters
                        speedViewModel.updateTotalDistance(totalDistanceInMeters.toInt().toString())
                        speedViewModel.updateAltitude(location.altitude.toInt().toString())
                    } else {
                        addSpeedToList(0.0)
                    }
                    val averageSpeed = calculateAverageSpeed()
                    speedViewModel.updateCurrentSpeed( averageSpeed.toInt().toString())
                    updateNotification("Velocity: ${averageSpeed.toInt()} km/h")


                }
                // Update previous location and time
                previousLocation = location

                val speed = location.speed
                velocity2 = location.speed * 3.6
                val altitude = location.altitude
                val accuracy = location.accuracy

                Log.d(
                    "LocationListener",
                    "GPS: $latitude, Longitud: $longitude velocity2 $velocity2 accuracy $accuracy"
                )

                speedViewModel.updateCurrentSpeed2(velocity2.toInt().toString())

                Toast.makeText(
                    applicationContext,
                    "v2=$velocity2",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onProviderEnabled(provider: String) {
                println("$provider habilitado")
            }

            override fun onProviderDisabled(provider: String) {
                println("$provider deshabilitado")
            }
        }
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf() // Stop service if no permissions
            return
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this@SpeedService, "START GPS LOCATION", Toast.LENGTH_SHORT).show()
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 1000L, 1f, locationListener)
        }
        // Registrar GnssStatus.Callback para monitorear satélites
        locationManager.registerGnssStatusCallback(gnssCallback, null)

    }


    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Medidor de Velocidad")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia por un ícono válido
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Speed Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for speed measurement service"
            }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun addSpeedToList(speed: Double) {
        // Add the new speed to the list
        speedList.add(speed)
        // Ensure the list does not exceed the maximum number of entries
        if (speedList.size > MAX_SPEED_ENTRIES) {
            speedList.removeAt(0) // Remove the oldest speed
            speedList.removeAt(1)
        }
    }

    private fun calculateAverageSpeed(): Double {
        return if (speedList.isNotEmpty()) {
            speedList.average() // Calculate the average speed
        } else {
            0.0
        }
    }
    private fun calculateElapsedTime(): String {
        val currentTime = System.currentTimeMillis()
        val elapsedMillis = currentTime - startTime
        val seconds = (elapsedMillis / 1000) % 60
        val minutes = (elapsedMillis / (1000 * 60)) % 60
        val hours = (elapsedMillis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    private fun startUpdatingElapsedTime() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val elapsedTime = calculateElapsedTime()
                speedViewModel.updateElapsedTime(elapsedTime)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateTimeRunnable)
    }
    override fun onDestroy() {
        super.onDestroy()
        stopUpdatingElapsedTime()
        locationManager.removeUpdates(locationListener)
        locationManager.unregisterGnssStatusCallback(gnssCallback)
    }
    private fun stopUpdatingElapsedTime() {
        handler.removeCallbacks(updateTimeRunnable)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
