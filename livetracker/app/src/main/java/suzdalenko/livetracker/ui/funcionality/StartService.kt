package suzdalenko.livetracker.ui.funcionality

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import suzdalenko.livetracker.service.LocationService

object StartService {
    fun serviceStart(c: Context){
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                c.startForegroundService(Intent(c, LocationService::class.java))
            } else {
                c.startService(Intent(c, LocationService::class.java))
            }
        }
    }
}