package suzdalenko.livetracker.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import suzdalenko.livetracker.R
import suzdalenko.livetracker.ui.funcionality.FindLocation.location

class LocationService: Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(11, createNotification())
        location(applicationContext)
    }
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        location(applicationContext)
        return START_STICKY
    }



    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("y", "x", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        // val stopIntent = Intent(this, StopPhotoCreatingReceiver::class.java)
        // val stopPendingIntent = PendingIntent.getBroadcast(this, 23, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "y")
            .setContentTitle("lt")
            .setContentText("Suzdalenko")
            .setSmallIcon(R.mipmap.ic_launcher) // .addAction(R.mipmap.ic_launcher, "Stop Service Photo Creation", stopPendingIntent)
            .build()
    }


}