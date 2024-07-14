package suzdalenko.photolapse.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import suzdalenko.photolapse.service.FileUploadService
import suzdalenko.photolapse.service.PhotoCreateService
import suzdalenko.photolapse.util.MyApp
import suzdalenko.photolapse.util.MyApp.Companion.acquireWakeLock

class StartServicesReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Alarm Triggered StartServicesReceiver", Toast.LENGTH_SHORT).show()
        val wakeLock = acquireWakeLock(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, PhotoCreateService::class.java))
            context.startForegroundService(Intent(context, FileUploadService::class.java))
        } else {
            context.startService(Intent(context, PhotoCreateService::class.java))
            context.startService(Intent(context, FileUploadService::class.java))
        }
        MyApp().setExactAlarm(context)
        MyApp().scheduleExactAlarm(context)
        // Toast.makeText(context, "Alarm Triggered StartServicesReceiver", Toast.LENGTH_SHORT).show()
        wakeLock.release()
    }
}