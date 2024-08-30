package suzdalenko.photolapse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import suzdalenko.photolapse.ui.CameraActivity
import suzdalenko.photolapse.util.Settings.LogPhotoLapse

class FirstPlaneReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (it.action == "com.example.ACTION_EVENT") {
                val message = it.getStringExtra("message")
                val startIntent = Intent(context, CameraActivity::class.java)
                // intent show Camera activity in first plane
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                LogPhotoLapse("work-FirstPlaneReceiver")
                context?.startActivity(startIntent)
            }
        }
    }
}