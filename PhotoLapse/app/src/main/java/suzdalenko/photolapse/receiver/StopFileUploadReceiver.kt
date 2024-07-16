package suzdalenko.photolapse.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import suzdalenko.photolapse.service.FileUploadService
import suzdalenko.photolapse.util.Settings.LogPhotoLapse

class StopFileUploadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stopIntent = Intent(context, FileUploadService::class.java)
        stopIntent.action = "ACTION_STOP_SERVICE"
        LogPhotoLapse("work-StopFileUploadReceiver")
        context.stopService(stopIntent)
    }
}