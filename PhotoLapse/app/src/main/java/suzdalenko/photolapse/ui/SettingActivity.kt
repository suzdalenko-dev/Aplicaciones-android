package suzdalenko.photolapse.ui
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat.canScheduleExactAlarms
import androidx.core.content.ContextCompat
import org.w3c.dom.Text
import suzdalenko.photolapse.R


class SettingActivity : AppCompatActivity() {
    private val requestExactAlarmPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> }
    private fun requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        } else{
            Toast.makeText(this, "DON'T NECESARY", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        findViewById<TextView>(R.id.auto_start_background).setOnClickListener { openAutoStartSettings() }
        findViewById<TextView>(R.id.enable_programing_alarm).setOnClickListener { requestScheduleExactAlarmPermission() }
        findViewById<TextView>(R.id.enable_photo_creating).setOnClickListener { openCameraPermissionSettings() }
        findViewById<TextView>(R.id.battery_optimizations).setOnClickListener { bareryOprimizations() }
    }
    @SuppressLint("BatteryLife")
    private fun bareryOprimizations(){
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
    private fun openCameraPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    private fun openAutoStartSettings() {
        val manufacturer = Build.MANUFACTURER.lowercase()
        when {
            manufacturer.contains("xiaomi") -> openMiuiAutoStartSettings()
            manufacturer.contains("oppo") -> openOppoAutoStartSettings()
            manufacturer.contains("vivo") -> openVivoAutoStartSettings()
            manufacturer.contains("oneplus") -> openOnePlusAutoStartSettings()
            manufacturer.contains("huawei") -> openHuaweiAutoStartSettings()
            else -> openDefaultAutoStartSettings()
        }
    }
    private fun openMiuiAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openDefaultAutoStartSettings()
        }
    }
    private fun openOppoAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openDefaultAutoStartSettings()
        }
    }
    private fun openVivoAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openDefaultAutoStartSettings()
        }
    }
    private fun openOnePlusAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openDefaultAutoStartSettings()
        }
    }
    private fun openHuaweiAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openDefaultAutoStartSettings()
        }
    }
    private fun openDefaultAutoStartSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + packageName)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}