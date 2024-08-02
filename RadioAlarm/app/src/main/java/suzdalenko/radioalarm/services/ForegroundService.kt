package suzdalenko.radioalarm.services
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import suzdalenko.radioalarm.R
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ForegroundService: Service()  {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ServiceCompat.startForeground(this, 1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK )
        else
            startForeground(1, createNotification())

        // Schedule the task to run every 22 seconds
        executor.scheduleWithFixedDelay({ uploadFilesA() }, 11, 22, TimeUnit.SECONDS)
        Toast.makeText(this@ForegroundService, "onCreate", Toast.LENGTH_LONG).show()

        Log.d("ForegroundServiceApp", "onCreate")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundServiceApp", "onStartCommand")

        Toast.makeText(this@ForegroundService, "onStartCommand", Toast.LENGTH_LONG).show()
        return START_STICKY
    }
    override fun onBind(p0: Intent?): IBinder? {
       return null
    }
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("notificationChannelId", "Radio Alarm", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        // val stopIntent = Intent(this, StopFileUploadReceiver::class.java)
        // val stopPendingIntent = PendingIntent.getBroadcast(this, 22, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "notificationChannelId")
            .setContentTitle("Radio Alarm")
            .setContentText("Suzdalenko")
            .setSmallIcon(R.mipmap.ic_launcher) // .addAction(R.mipmap.ic_launcher, "Stop Service File Upload", stopPendingIntent)
            .build()
    }
    private fun uploadFilesA() {
        Log.d("ForegroundServiceApp", "uploadFiles called")
        mainHandler.post {
            Toast.makeText(this@ForegroundService, "Uploading files...", Toast.LENGTH_LONG).show()
        }
    }
}