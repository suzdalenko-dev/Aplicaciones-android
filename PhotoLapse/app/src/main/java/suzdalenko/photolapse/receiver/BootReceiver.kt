package suzdalenko.photolapse.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import suzdalenko.photolapse.ui.MainActivity
import suzdalenko.photolapse.util.Settings.LogPhotoLapse

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Iniciar la aplicación
            val launchIntent = Intent(context, MainActivity::class.java)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            LogPhotoLapse("work-BootReceiver")
            context?.startActivity(launchIntent)
        }
    }
}