package suzdalenko.speedometer.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import suzdalenko.speedometer.R
import suzdalenko.speedometer.util.App.Companion.speedViewModel

class SpeedService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var previousLocation: Location? = null
    private var previousTime: Long = 0L

    private val NOTIFICATION_CHANNEL_ID = "SpeedServiceChannel"
    private val NOTIFICATION_ID = 1

    private val speedList = mutableListOf<Double>() // Array para almacenar velocidades
    private val MAX_SPEED_ENTRIES = 5 // Número máximo de entradas para calcular la media

    private var totalDistanceInMeters: Double = 0.0
    private var startTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        startTime = System.currentTimeMillis()

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configure notification and start foreground service
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Iniciando medición de velocidad..."))

        // Configure location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation: Location? = locationResult.lastLocation
                if (currentLocation != null) {
                    val currentTime = System.currentTimeMillis()

                    if (previousLocation != null) {
                        // Calculate distance and time
                        val distanceInMeters = currentLocation.distanceTo(previousLocation!!)
                        if (distanceInMeters > 0) {
                            totalDistanceInMeters += distanceInMeters // Sumamos a la distancia total
                        }
                        val timeInSeconds = (currentTime - previousTime) / 1000.0

                        if (timeInSeconds > 0) {
                            // Calculate speed in km/h
                            val speedInKmH = (distanceInMeters / timeInSeconds) * 3.6
                            // Add the speed to the list
                            if (speedInKmH > 1.0) { // Ignorar velocidades menores a 1 km/h
                                addSpeedToList(speedInKmH)
                            }
                            // Calculate average speed
                            val averageSpeed = calculateAverageSpeed()

                            updateNotification("Velocidad: ${averageSpeed.toInt()} km/h")
                            // Actualizar datos en el ViewModel
                            speedViewModel.updateCurrentSpeed(averageSpeed.toInt().toString())
                            speedViewModel.updateTotalDistance(totalDistanceInMeters.toInt().toString())
                            speedViewModel.updateAltitude(currentLocation.altitude.toInt().toString())
                            speedViewModel.updateElapsedTime(calculateElapsedTime())
                        }
                    }

                    // Update previous location and time
                    previousLocation = currentLocation
                    previousTime = currentTime
                }
            }
        }

        // Start location updates
        startLocationUpdates()

        Toast.makeText(this, "Servicio de medición de velocidad iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3000 ) //  PRIORITY_HIGH_ACCURACY
            .setMinUpdateIntervalMillis(3000)                                     // PRIORITY_BALANCED_POWER_ACCURACY
            .build()

        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf() // Stop service if no permissions
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
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
                description = "Canal para el servicio de medición de velocidad"
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
    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Este servicio no se enlaza con componentes
    }
}
