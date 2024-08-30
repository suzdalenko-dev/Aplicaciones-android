package suzdalenko.livetracker.ui.funcionality
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import suzdalenko.livetracker.ui.MainActivity
import suzdalenko.livetracker.ui.funcionality.FindLocation.location
import suzdalenko.livetracker.util.App.Companion.showMessage

object LocationPermision {
    fun locationPerm(c: Context, a: MainActivity){
         if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
             || ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                 ActivityCompat.requestPermissions(a, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.FOREGROUND_SERVICE_LOCATION), 11)
             } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                 ActivityCompat.requestPermissions(a, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE), 11)
             } else {
                 ActivityCompat.requestPermissions(a, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 11)
             }
         } else {
             // Permissions are already granted
             showMessage(c, "Permission location granted")
             location(c)
        }
    }
}