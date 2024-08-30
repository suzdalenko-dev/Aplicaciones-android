package suzdalenko.livetracker.receiver
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import suzdalenko.livetracker.ui.funcionality.StartService.serviceStart
import suzdalenko.livetracker.util.App.Companion.showMessage
class BootReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
            serviceStart(context)
            // Optionally show a Toast message
            showMessage(context, "Device Booted, Service Started")
            Log.d("log_suzdalenko", "Device Booted, Service Started BOOT_COMPLETED ")
    }
}

/*
0 => 2,2 M




 */