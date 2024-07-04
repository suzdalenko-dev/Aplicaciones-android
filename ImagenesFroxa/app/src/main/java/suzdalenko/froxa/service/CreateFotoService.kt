package suzdalenko.froxa.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import suzdalenko.froxa.R
import suzdalenko.froxa.ui.Camara
import suzdalenko.froxa.util.MyApp.Companion.prefs
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CreateFotoService : Service() {
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private var isActivityRunning = false
    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var miHandlerThread: HandlerThread
    private lateinit var miHandler: Handler

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
                handler.postDelayed(this, 10000) // Ejecutar cada 10 segundos
            }
        }, 3000)
    }

    private fun takePhoto() {
        // Verificar si la actividad está en primer plano
        val estadoActualActivity = prefs.getString("__state", "activo").toString()

        Camara.imageCapture?.let { imageCapture ->
            val imageDir = File(getExternalFilesDir(null), "images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }

            val photoFile = File(
                imageDir,
                SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.FRANCE).format(System.currentTimeMillis()) + ".jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Configurar el flash para que esté encendido
            imageCapture.flashMode = ImageCapture.FLASH_MODE_ON

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this@CreateFotoService),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CreateFotoService", "Error al capturar la imagen: ${exception.message}", exception)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        val msg = "Imagen capturada: $savedUri"
                        Log.d("CreateFotoService", msg)
                        // Mostrar un Toast con la ruta de la imagen guardada
                        Toast.makeText(this@CreateFotoService, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } ?: run {
            Log.e("CreateFotoService", "ImageCapture no está inicializado")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Simular algún proceso en segundo plano que envía un evento cada cierto tiempo
        miHandler.postDelayed(object : Runnable {
            override fun run() {
                val intent = Intent("com.example.ACTION_EVENT")
                intent.putExtra("message", "Evento desde el servicio")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                Log.d("estadoActualActivity", "!!! enviando receiver !!!")
                miHandler.postDelayed(this, 5000) // Ejecutar cada 10 segundos
            }
        }, 5000)


        return START_STICKY
    }
}