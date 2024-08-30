package suzdalenko.livetracker.ui.funcionality
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import suzdalenko.livetracker.R
import suzdalenko.livetracker.util.App.Companion.fusedLocationClient
import suzdalenko.livetracker.util.App.Companion.locationCallback
import suzdalenko.livetracker.util.App.Companion.locationManager
import suzdalenko.livetracker.util.App.Companion.locationRequest
import suzdalenko.livetracker.util.App.Companion.locationRequestInterval
import suzdalenko.livetracker.util.App.Companion.sh
import suzdalenko.livetracker.util.HttpRequest.shareLoc

object FindLocation {
    fun location(context: Context){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (sh.getBoolean("target", false)) {
            if (fusedLocationClient == null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            }
            if (locationRequest == null) {
                locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 300*1000)
                        .setMinUpdateIntervalMillis(300*1000)
                        .build()
            }
            if (locationCallback == null) {
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location == null) {
                            Log.d("log_suzdalenko", "location TARGET NULL LOCATION")
                        } else {
                            val latitude = location.latitude.toString()
                            val longitude = location.longitude.toString()
                            Log.d("log_suzdalenko", "location TARGET $longitude $latitude")
                            val userId = sh.getString("id", "").toString()
                            val rol = sh.getBoolean("target", false).toString()
                            val sVal = "id=$userId&lat=$latitude&lon=$longitude&rol=$rol"

                        //    try {
                        //        val mediaPlayer = MediaPlayer.create(context, R.raw.upload_positive)
                        //        mediaPlayer.start()
                        //        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                        //    } catch (_: Exception){

                        //    }

                            shareLoc(sVal)
                        }
                    }
               }
            }
            fusedLocationClient!!.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.getMainLooper())
            Log.d("log_suzdalenko", "location TARGET SEARCH TOP")



            /*
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    println("New Location: Latitude: $latitude, Longitude: $longitude")
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.camera_shutter)
                        mediaPlayer.start()
                        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                    } catch (_: Exception){}
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    // Request a single location update
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener, Looper.getMainLooper())
                    handler.postDelayed(this, locationRequestInterval)
                }
            }, locationRequestInterval)
            */



        } else {
            Log.d("log_suzdalenko", "location TARGET search BOTTOM")
            if (fusedLocationClient != null && locationCallback != null) {
                fusedLocationClient?.removeLocationUpdates(locationCallback!!)
            }
            locationRequest = null
            locationCallback = null
            fusedLocationClient = null
        }


    }
}