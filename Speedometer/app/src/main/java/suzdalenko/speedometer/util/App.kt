package suzdalenko.speedometer.util

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import kotlin.math.sqrt

class App : Application() {
    companion object {
         private val handlerThread = HandlerThread("AppHandlerThread").apply { start() }
         val handler = android.os.Handler(handlerThread.looper)
         lateinit var updateTimeRunnable: Runnable
         var previousLocation: Location? = null

        val NOTIFICATION_CHANNEL_ID = "SpeedServiceChannel"
        val NOTIFICATION_ID = 1

        val speedList = mutableListOf<Double>() // Array para almacenar velocidades
        val MAX_SPEED_ENTRIES = 3 // Número máximo de entradas para calcular la media
        val speedList2 = mutableListOf<Double>() // Array para almacenar velocidades

        var totalDistanceInMeters: Double = 0.0
        lateinit var speedViewModel: SpeedViewModel
        var startTime: Long = 0L


        lateinit var locationManager: LocationManager
        lateinit var locationListener: LocationListener
        var velocity2 = 0.0
        lateinit var gnssCallback: GnssStatus.Callback
        var usedSatellites = 0
        var totalSatellites = 0



        fun fuseSensorData(location: Location, ax: Float, ay: Float, az: Float, gx: Float, gy: Float, gz: Float) {
            val gpsSpeed = location.speed * 3.6 // Velocidad en km/h del GPS
            val gpsAccuracy = location.accuracy // Precisión del GPS

            // Magnitud de aceleración (con umbral para filtrar ruido)
            val accelerationMagnitude = sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
            val accelerationThreshold = 0.1f // Umbral para filtrar ruido
            val filteredAcceleration = if (accelerationMagnitude > accelerationThreshold) accelerationMagnitude else 0f

            // Magnitud de rotación del giroscopio (con umbral para filtrar ruido)
            val gyroscopeMagnitude = sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()
            val gyroscopeThreshold = 0.05f // Umbral para filtrar ruido
            val filteredGyroscope = if (gyroscopeMagnitude > gyroscopeThreshold) gyroscopeMagnitude else 0f

            // Ajustar pesos dinámicamente si el GPS es confiable
            val gpsWeight = if (gpsSpeed == 0.0 && gpsAccuracy < 5) 1.0 else 0.6
            val accelerometerWeight = if (gpsSpeed == 0.0 && gpsAccuracy < 5) 0.0 else 0.3
            val gyroscopeWeight = if (gpsSpeed == 0.0 && gpsAccuracy < 5) 0.0 else 0.1

            // Velocidad fusionada
            val fusedSpeed = gpsWeight * gpsSpeed + accelerometerWeight * filteredAcceleration + gyroscopeWeight * filteredGyroscope

            Log.d(
                "LocationListener",
                "Velocidad fusionada: $fusedSpeed km/h (GPS: $gpsSpeed, Acelerómetro: $filteredAcceleration, Giroscopio: $filteredGyroscope)"
            )
            speedViewModel.updateCurrentSpeed3(fusedSpeed.toInt().toString())
        }
    }

    override fun onCreate() {
        super.onCreate()

        startTime = System.currentTimeMillis()
        speedViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(SpeedViewModel::class.java)


        Toast.makeText(applicationContext, "onCreate onCreate onCreate", Toast.LENGTH_SHORT).show()


        gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                totalSatellites = status.satelliteCount
                usedSatellites = 0
                for (i in 0 until totalSatellites) {
                    if (status.usedInFix(i)) {
                        usedSatellites++
                    }
                }

                // Opcional: Actualiza tu ViewModel con el número de satélites usados
                // speedViewModel.updateSatellitesUsed(usedSatellites.toString())
            }
        }




    }

}