package suzdalenko.photolapse.service
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import suzdalenko.photolapse.R
import suzdalenko.photolapse.receiver.StopFileUploadReceiver
import suzdalenko.photolapse.receiver.StopPhotoCreatingReceiver
import suzdalenko.photolapse.ui.CameraActivity
import suzdalenko.photolapse.util.GetRequestWorker
import suzdalenko.photolapse.util.MyApp.Companion.DISPARO_CAMARA
import suzdalenko.photolapse.util.MyApp.Companion.formatSeconds
import suzdalenko.photolapse.util.MyApp.Companion.getImageCapture
import suzdalenko.photolapse.util.MyApp.Companion.initializeCamera
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import suzdalenko.photolapse.util.PlaySound.errorSoundGetFoto
import suzdalenko.photolapse.util.PlaySound.playSoundGetFoto
import suzdalenko.photolapse.util.Settings.LogPhotoLapse
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class PhotoCreateService : LifecycleService() {
    var fotosCreadasActivity: Long = 0
    inner class LocalBinder : Binder() {
        fun getService(): PhotoCreateService = this@PhotoCreateService
    }
    private lateinit var handlerUI: Handler
    private var isRunningTimer = true
    private val binder = LocalBinder()
    private lateinit var currentLocale: Locale
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var miHandlerThreadSecondLive: HandlerThread
    private lateinit var miHandlerSecondLive: Handler
    var countSecond: Long = 0

    companion object {
        var activityCamara: WeakReference<CameraActivity>? = null
        var fotosCreadas: Long = 0
    }

    override fun onCreate() {
        super.onCreate()
        currentLocale = applicationContext.resources.configuration.locales.get(0)
        handlerThread = HandlerThread("CreateFotoServiceThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)



        miHandlerThreadSecondLive = HandlerThread("secondLive")
        miHandlerThreadSecondLive.start()
        miHandlerSecondLive = Handler(miHandlerThreadSecondLive.looper)

        startForeground(11, createNotification())
        startTakingPhotos()
        handlerUI = Handler(Looper.getMainLooper())
        startUpdatingUI()
        startFirstPlaneReceiver()
        LogPhotoLapse("onCreate-PhotoCreateService")
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "CREATE_FOTO_SERVICE"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Create Foto Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val stopIntent = Intent(this, StopPhotoCreatingReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(this, 23, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Create Foto Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.mipmap.ic_launcher).addAction(R.mipmap.ic_launcher, "Stop Service Photo Creation", stopPendingIntent)
            .build()
    }

    private fun startTakingPhotos() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if(DISPARO_CAMARA.toInt() <= 0 || DISPARO_CAMARA.toInt() == 0) {
                    takePhoto()
                    DISPARO_CAMARA = prefs.getLong("camera_frequency", 1000)
                    countSecond = 0
                }
                handler.postDelayed(this, 1000)
            }
        }, 5000)
    }

    private fun takePhoto() {
         val imageCapture = getImageCapture() ?: return
         val imageDir = File(externalMediaDirs.firstOrNull(), "images")
         if (!imageDir.exists()) { imageDir.mkdirs() }
         val photoFile = File(imageDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg")
         val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
         if (prefs.getString("flash", "x").toString() == "flash") { imageCapture.flashMode = ImageCapture.FLASH_MODE_ON } else { imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF }

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                 override fun onError(exc: ImageCaptureException) {
                     initializeCamera(this@PhotoCreateService, this@PhotoCreateService)
                     Toast.makeText(baseContext, "SER ${exc.message}", Toast.LENGTH_SHORT).show()
                     Log.e("PhotoCreateService", "Photo capture failed: ${exc.message}", exc)
                     LogPhotoLapse("ERROR-takePhoto-PhotoCreateService")
                     errorSoundGetFoto(applicationContext)
                 }
                 override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                     Toast.makeText(baseContext, getString(R.string.image_captured) + photoFile.name.toString(), Toast.LENGTH_SHORT).show()
                     fotosCreadasActivity += 1
                     LogPhotoLapse("good-takePhoto-PhotoCreateService")
                     playSoundGetFoto(applicationContext)
                 }
             }
        )
    }


    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == "ACTION_STOP_SERVICE") {
            stopPhotoService()
            return START_NOT_STICKY
        }
        LogPhotoLapse("onStartCommand-PhotoCreateService")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        miHandlerSecondLive.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
        miHandlerThreadSecondLive.quitSafely()
        LogPhotoLapse("onDestroy-PhotoCreateService")
    }
    fun stopPhotoService(){
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isRunningTimer = false
    }

    private val updateUITask = object : Runnable {
        override fun run() {
            activityCamara?.get()?.let { activity ->
                activity.runOnUiThread {
                    DISPARO_CAMARA = (prefs.getLong("camera_frequency", 1800 * 1000) / 1000).toInt() - countSecond++
                    val textView: TextView? = activity.findViewById(R.id.seconds_left)
                    textView?.let { it.text = getString(R.string.segundos)+" "+formatSeconds(DISPARO_CAMARA) }
                    val textView2: TextView? = activity.findViewById(R.id.photos_created)
                    textView2?.let { it.text = getString(R.string.photos_created)+ " ${fotosCreadas + fotosCreadasActivity}" }
                }
            }
            handler.postDelayed(this, 1000)
        }
    }
    fun startUpdatingUI(){
        handler.post(updateUITask)
    }
    fun restartCamaraService() {
        initializeCamera(this@PhotoCreateService, this@PhotoCreateService)
    }
    fun startFirstPlaneReceiver(){
        // Simular algún proceso en segundo plano que envía un evento cada cierto tiempo
        miHandlerSecondLive.postDelayed(object : Runnable {
            override fun run() {
                val intent1 = Intent("com.example.ACTION_EVENT")
                intent1.putExtra("message", "Evento desde el servicio")
                miHandlerSecondLive.postDelayed(this, 3600 * 1000) // 1 hora
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent1)
            }
        }, 5000)
    }

}