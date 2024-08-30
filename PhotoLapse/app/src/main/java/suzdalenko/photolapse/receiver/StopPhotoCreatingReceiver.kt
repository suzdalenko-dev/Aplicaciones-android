package suzdalenko.photolapse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import suzdalenko.photolapse.service.PhotoCreateService
import suzdalenko.photolapse.util.Settings.LogPhotoLapse

class StopPhotoCreatingReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stopIntent = Intent(context, PhotoCreateService::class.java)
        stopIntent.action = "ACTION_STOP_SERVICE"
        LogPhotoLapse("work-StopPhotoCreatingReceiver")
        context.stopService(stopIntent)
    }
}