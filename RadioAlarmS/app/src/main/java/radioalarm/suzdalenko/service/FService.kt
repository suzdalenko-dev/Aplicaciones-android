package radioalarm.suzdalenko.service
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
import androidx.media3.common.MediaItem
import radioalarm.suzdalenko.util.App.Companion.executor
import radioalarm.suzdalenko.R
import radioalarm.suzdalenko.util.App.Companion.exoPlayer
import radioalarm.suzdalenko.util.App.Companion.getCurrentTime
import radioalarm.suzdalenko.util.App.Companion.handler
import radioalarm.suzdalenko.util.App.Companion.sh
import radioalarm.suzdalenko.util.Player.play
import java.util.concurrent.TimeUnit

class FService: Service() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ServiceCompat.startForeground(this, 1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else
            startForeground(1, createNotification())

        scheduleImageCheck()

    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    fun scheduleImageCheck(){
        executor.scheduleWithFixedDelay({ checkGitSettings() }, 0, 22, TimeUnit.SECONDS)
    }
    fun checkGitSettings(){
        val (hour, minute) = getCurrentTime()
        if(sh.getInt("hour", -1) == hour && sh.getInt("min", -1) == minute){
            handler.post { play("https://net1-cope-rrcast.flumotion.com/cope/net1.mp3", applicationContext) }
        }
        // Log.d("suzdalenkoLogA", "hour: $hour minute: $minute")
        // Log.d("suzdalenkoLogA", "sh hour: $shHour minute: $shMinute")
    }
    private fun createNotification(): Notification {
        val notificationChannelId = "UPLOAD_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId, "Upload Service", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        // val stopIntent = Intent(this, StopFileUploadReceiver::class.java)
        // val stopPendingIntent = PendingIntent.getBroadcast(this, 22, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Radio Alarm")
            .setContentText("Suzdalenko")
            .setSmallIcon(R.mipmap.ic_launcher) // .addAction(R.mipmap.ic_launcher, "Stop Service File Upload", stopPendingIntent)
            .build()
    }
}