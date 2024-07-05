package suzdalenko.froxa.service
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
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import suzdalenko.froxa.R
import suzdalenko.froxa.ui.Camara
import suzdalenko.froxa.util.MyApp.Companion.UPLOAD_FILES_EACH_SEC
import suzdalenko.froxa.util.UploadFile
import suzdalenko.froxa.util.MyApp.Companion.getDateApp
import suzdalenko.froxa.util.MyApp.Companion.prefs
import suzdalenko.froxa.util.SentEmail.Companion.enviarCorreoAutomaticamente
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
class UploadFileService: Service() {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val handler = Handler(Looper.getMainLooper())
    var countFiles = 0
    companion object {
        var activityCamara: WeakReference<Camara>? = null
        var uploadLeenda = "Archivos subidos: "
        var photosUploaded: Long = 0
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceCompat.startForeground(this, 1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(1, createNotification())
        }
        scheduleImageCheck()
        Thread {
            while (true) {
                UploadFileService.activityCamara?.get()?.let { activity ->
                    activity.runOnUiThread {
                        val textView: TextView? = activity.findViewById(R.id.uploaded_photos)
                        textView?.let {
                            it.text = "${uploadLeenda} ${photosUploaded}"
                        }
                    }
                }
                Thread.sleep(3000)
            }
        }.start()
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
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
    private fun scheduleImageCheck() {
        executor.scheduleWithFixedDelay({ checkAndUploadImages() }, 0, UPLOAD_FILES_EACH_SEC, TimeUnit.SECONDS)
    }
    private fun checkAndUploadImages() {
        countFiles = 0
        val imageDir = File(externalMediaDirs.firstOrNull(), "images")
        if (imageDir.exists() && imageDir.isDirectory) {
            val imageFiles = imageDir.listFiles { file -> file.extension == "jpg" }
            val numeroFiles = imageFiles?.size
            imageFiles?.forEach { imageFile ->
                countFiles++
                if (countFiles <= 3) {
                    uploadOrSentImage(imageFile)
                }
            }
        } else {
            Log.d("UploadService", "No images found or directory does not exist.")
        }

    }
    private fun uploadOrSentImage(imageFile: File) {
        if (prefs.getString("email", "email").toString() == "taller@froxa.com"){
            val uploadFile = UploadFile(imageFile)
            uploadFile.uploadFile { response ->
                if(response.toString().contains("exitosamente")){
                    imageFile.delete()
                    uploadLeenda = "Archivos subidos: "
                    photosUploaded++
                }
            }
        } else {
            enviarCorreoAutomaticamente("alexey.saron@gmail.com", "Auto Photo App", getDateApp(), imageFile){ emailSented ->
                if (emailSented){
                    photosUploaded++
                    uploadLeenda = "Archivos enviados: "
                    imageFile.delete()
                }
            }
        }


    }
    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

}
