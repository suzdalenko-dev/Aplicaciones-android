package suzdalenko.photolapse.ui
import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.util.Rational
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import suzdalenko.photolapse.R
import suzdalenko.photolapse.receiver.FirstPlaneReceiver
import suzdalenko.photolapse.service.FotoCreateService
import suzdalenko.photolapse.service.FileUploadService
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class CameraActivity : AppCompatActivity() {
    private lateinit var currentLocale: Locale
    private var myService: FotoCreateService? = null
    private var isBound = false
    private lateinit var switchCompat: SwitchCompat
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FotoCreateService.LocalBinder
            myService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }
    private fun modifyServiceVariable() {
        if (isBound) { myService?.let { service -> service.fotosCreadasActivity += 1 } }
    }

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var captureButton: Button
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        var imageCapture: ImageCapture? = null
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        viewFinder = findViewById(R.id.viewFinder)
        currentLocale = applicationContext.resources.configuration.locales.get(0)
        // Solicitar permisos de cámara si no están concedidos
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        captureButton = findViewById(R.id.captureButton)
        captureButton.setOnClickListener {
            takePhoto()
        }

        // Registrar el receptor de broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(FirstPlaneReceiver(), IntentFilter("com.example.ACTION_EVENT"))


        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        // Crear un WakeLock para mantener la pantalla encendida
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiApp::WakeLockTag")
        wakeLock?.acquire()
        // Mantener la pantalla encendida mientras esta actividad está en primer plano
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Enlazar con el Service
        Intent(this, FotoCreateService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        switchCompat = findViewById(R.id.switchCompat)
        if(prefs.getString("flash", "x") == "flash"){ switchCompat.isChecked = true
        } else { switchCompat.isChecked = false }
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){ prefs.edit().putString("flash", "flash").apply(); Toast.makeText(this, "Flash ON", Toast.LENGTH_SHORT).show()
            } else { prefs.edit().putString("flash", "x").apply(); ; Toast.makeText(this, "Flash OFF", Toast.LENGTH_SHORT).show() }
        }

        // Establecer la referencia de la actividad en el servicio
        FotoCreateService.activityCamara = WeakReference(this)
        FileUploadService.activityCamara = WeakReference(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { startForegroundService(Intent(this, FotoCreateService::class.java))
        } else { startService(Intent(this, FotoCreateService::class.java)) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { startForegroundService(Intent(this, FileUploadService::class.java))
        } else { startService(Intent(this, FileUploadService::class.java)) }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configurar la vista previa de la cámara
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                // Unir el caso de uso de la vista previa y de captura de imágenes a la cámara
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "No se puede unir el caso de uso de la cámara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture?.let {
            // Crear un archivo de salida para la imagen
            val imageDir = File(externalMediaDirs.firstOrNull(), "images")
            if (!imageDir.exists()) { imageDir.mkdirs() }
            val photoFile = File(imageDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", currentLocale).format(System.currentTimeMillis()) + ".jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Configurar el flash para que esté encendido
            if(prefs.getString("flash", "x").toString() == "flash") { it.flashMode = ImageCapture.FLASH_MODE_ON
            } else { it.flashMode = ImageCapture.FLASH_MODE_OFF }

            it.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Error al capturar la imagen: ${exception.message}", exception)
                        Toast.makeText(baseContext, "Error al capturar la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        val msg = "Imagen capturada: $savedUri"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        modifyServiceVariable()
                    }
                }
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStart() {
        super.onStart()
    }
    override fun onResume() {
        super.onResume()
        // Verificar si la actividad está en modo Picture-in-Picture
        if (isInPictureInPictureMode) {
            // Si está en modo PiP, finalizar la instancia PiP
        }
    }
    override fun onPause() {
        super.onPause()
    }
    override fun onStop() {
        super.onStop()
    }
    @SuppressLint("Wakelock")
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        wakeLock?.release()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPictureInPictureMode1()
    }
    private fun enterPictureInPictureMode1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(1, 1) // Puedes ajustar la relación de aspecto según tus necesidades
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(pipParams)
        } else {
            // Manejar dispositivos con versiones anteriores a API 26
            Toast.makeText(this, "Picture-in-Picture no es soportado en este dispositivo.", Toast.LENGTH_SHORT).show()
            // O cualquier otra alternativa que desees implementar
        }

    }
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) { } else { }
    }
}