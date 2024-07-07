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
import suzdalenko.photolapse.util.MyApp.Companion.MAKE_PHOTO_EVERY_MILISEC
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

    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var miHandlerThread: HandlerThread
    private lateinit var miHandler: Handler
    var secundosQueFaltan: String = ""
    var countSecond: Long = 0
    companion object {
        var activityCamara: WeakReference<CameraActivity>? = null
        var fotosCreadas: Long = 0
    }

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("CreateFotoServiceThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        miHandlerThread = HandlerThread("suzdalThread")
        miHandlerThread.start()
        miHandler = Handler(miHandlerThread.looper)

        startForeground(11, createNotification())
        startTakingPhotos()
        // modificar datos en Camara activity xml
        Thread {
            while (true) {
                activityCamara?.get()?.let { activity ->
                    activity.runOnUiThread {
                        val textView: TextView? = activity.findViewById(R.id.seconds_left)
                        textView?.let {
                            val secondTime = (MAKE_PHOTO_EVERY_MILISEC / 1000).toInt() - countSecond++
                            secundosQueFaltan = formatSeconds(secondTime)
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

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
        miHandlerThread.quitSafely()
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
                takePhoto()
                handler.postDelayed(this, MAKE_PHOTO_EVERY_MILISEC) // Ejecutar cada 10 segundos
            }
        }, 1000)
    }

    private fun takePhoto() {
        // Verificar si la actividad está en primer plano
        val estadoActualActivity = prefs.getString("__state", "activo").toString()

        CameraActivity.imageCapture?.let { imageCapture ->
            val imageDir = File(externalMediaDirs.firstOrNull(), "images")
            if (!imageDir.exists()) { imageDir.mkdirs() }
            val photoFile = File(imageDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.FRANCE).format(System.currentTimeMillis()) + ".jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            if(prefs.getString("flash", "x").toString() == "flash") { imageCapture.flashMode = ImageCapture.FLASH_MODE_ON
            } else { imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF }
                imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this@FotoCreateService),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CreateFotoService", "Error al capturar la imagen: ${exception.message}", exception)
                    }
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        val msg = "Imagen capturada: $savedUri"
                        Toast.makeText(this@FotoCreateService, msg, Toast.LENGTH_SHORT).show()
                        countSecond = 0
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
        miHandler.postDelayed(object : Runnable {
            override fun run() {
                val intent1 = Intent("com.example.ACTION_EVENT")
                intent1.putExtra("message", "Evento desde el servicio")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent1)
                miHandler.postDelayed(this, 1320 * 1000) // 22 minutos son 1320 segundos.
            }
        }, 5000)


        return START_STICKY
    }



}