package suzdalenko.froxa.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import suzdalenko.froxa.R
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
class UploadService: Service() {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val handler = Handler(Looper.getMainLooper())
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        super.onCreate()
        startForeground(111, createNotification())
        scheduleImageCheck()
    }
    private fun createNotification(): Notification {
        val notificationChannelId = "UPLOAD_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Upload Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Upload Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }
    private fun scheduleImageCheck() {
        executor.scheduleWithFixedDelay({
            checkAndUploadImages()
        }, 0, 1, TimeUnit.HOURS)
    }
    private fun checkAndUploadImages() {
        val imageDir = File(externalMediaDirs.firstOrNull(), "images")
        if (imageDir.exists() && imageDir.isDirectory) {
            val imageFiles = imageDir.listFiles { file -> file.extension == "jpg" }
            imageFiles?.forEach { imageFile ->
                uploadImage(imageFile)
            }
        } else {
            Log.d(TAG, "No images found or directory does not exist.")
        }
    }
    private fun uploadImage(imageFile: File) {
        // Lógica para subir la imagen al servidor PHP
        // Por ejemplo, usando una librería de HTTP como Retrofit o HttpURLConnection
        Log.d(TAG, "Uploading image: ${imageFile.name}")
    }
    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
    companion object {
        private const val TAG = "UploadService"
        private const val NOTIFICATION_ID = 1
    }
}
