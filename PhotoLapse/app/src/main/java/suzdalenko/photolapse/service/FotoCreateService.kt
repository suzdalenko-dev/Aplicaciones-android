package suzdalenko.photolapse.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import suzdalenko.photolapse.R
import suzdalenko.photolapse.ui.CameraActivity
import suzdalenko.photolapse.util.MyApp.Companion.DISPARO_CAMARA
import suzdalenko.photolapse.util.MyApp.Companion.formatSeconds
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale

class FotoCreateService : Service() {
    var fotosCreadasActivity: Long = 0
    inner class LocalBinder : Binder() {
        fun getService(): FotoCreateService = this@FotoCreateService
    }
    private val binder = LocalBinder()
    private lateinit var currentLocale: Locale
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var miHandlerThreadSecondLive: HandlerThread
    private lateinit var miHandlerSecondLive: Handler
    private var secundosQueFaltan: String = ""
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
        // modificar datos en Camara activity xml
        Thread {
            while (true) {
                activityCamara?.get()?.let { activity ->
                    activity.runOnUiThread {
                        val textView: TextView? = activity.findViewById(R.id.seconds_left)
                        textView?.let {
                            val CAMERA_FREQUENCY = prefs.getLong("camera_frequency", 1800 * 1000)
                            DISPARO_CAMARA = (CAMERA_FREQUENCY / 1000).toInt() - countSecond++
                            // Log.d("CAMERA_FREQUENCY", "CAMERA_FREQUENCY: ${DISPARO_CAMARA} " +"countSecond $countSecond")
                            if(DISPARO_CAMARA < 0) { DISPARO_CAMARA = 0L }
                            secundosQueFaltan = formatSeconds(DISPARO_CAMARA)
                            it.text = getString(R.string.segundos)+" ${secundosQueFaltan}"
                        }
                        val textView2: TextView? = activity.findViewById(R.id.photos_created)
                        textView2?.let { it.text = getString(R.string.photos_created)+" ${fotosCreadas+fotosCreadasActivity}" }
                    }
                }
                Thread.sleep(1000)
            }
        }.start()
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
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Create Foto Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.mipmap.ic_launcher)
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
        if(fotosCreadas > 999999) fotosCreadas = 0

        CameraActivity.imageCapture?.let { imageCapture ->
            val imageDir = File(externalMediaDirs.firstOrNull(), "images")
            if (!imageDir.exists()) { imageDir.mkdirs() }
            val photoFile = File(imageDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", currentLocale).format(System.currentTimeMillis()) + ".jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            if(prefs.getString("flash", "x").toString() == "flash") { imageCapture.flashMode = ImageCapture.FLASH_MODE_ON
            } else { imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF }
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this@FotoCreateService),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(this@FotoCreateService, getString(R.string.error_captured)+exception.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Toast.makeText(this@FotoCreateService, getString(R.string.image_captured)+photoFile.name.toString(), Toast.LENGTH_SHORT).show()
                        fotosCreadas++
                    }
                }
            )
        } ?: run {
            Log.e("CreateFotoService", "ImageCapture no está inicializado")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Simular algún proceso en segundo plano que envía un evento cada cierto tiempo
        miHandlerSecondLive.postDelayed(object : Runnable {
            override fun run() {
                val intent1 = Intent("com.example.ACTION_EVENT")
                intent1.putExtra("message", "Evento desde el servicio")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent1)
                miHandlerSecondLive.postDelayed(this, 1320 * 1000) // 22 minutos son 1320 segundos.
            }
        }, 5000)


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Detener todos los mensajes pendientes y callbacks
        miHandlerSecondLive.removeCallbacksAndMessages(null) // Detener todos los mensajes pendientes y callbacks
        handlerThread.quitSafely() // Detener el handlerThread principal
        miHandlerThreadSecondLive.quitSafely() // Detener el handlerThread principal
    }

    fun restartTakingPhotos() {
        // handler.removeCallbacksAndMessages(null)
        // startTakingPhotos()
        // countSecond = 0
        // DISPARO_CAMARA = 0
    }
}