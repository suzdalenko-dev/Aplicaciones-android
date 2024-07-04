package suzdalenko.froxa.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import suzdalenko.froxa.R
import suzdalenko.froxa.ui.Camara
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CreateFotoService : Service() {
    private val handler = Handler()

    override fun onCreate() {
        super.onCreate()
        startForeground(11, createNotification())
        startTakingPhotos()
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
                handler.postDelayed(this, 10000) // Volver a ejecutar cada 10 segundos
            }
        }, 3000)
    }

    private fun takePhoto() {
        Camara.imageCapture?.let {
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
            it.flashMode = ImageCapture.FLASH_MODE_ON

            it.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
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
}