package suzdalenko.livetracker.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import org.mozilla.geckoview.GeckoRuntime
import suzdalenko.livetracker.ui.MapActivity

class App: Application() {
    companion object {
        lateinit var sh: SharedPreferences
        fun showMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        var fusedLocationClient: FusedLocationProviderClient? = null
        var locationRequest: LocationRequest? = null
        var locationCallback: LocationCallback? = null

        var locationManager: LocationManager? = null
        val locationRequestInterval: Long = 1000 // 1 seconds
    }

    override fun onCreate() {
        super.onCreate()
        sh = getSharedPreferences("suzdalenko.livetracker", MODE_PRIVATE)
    }

}