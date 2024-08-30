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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import suzdalenko.photolapse.service.PhotoCreateService
import suzdalenko.photolapse.service.FileUploadService
import suzdalenko.photolapse.util.MyApp.Companion.getImageCapture
import suzdalenko.photolapse.util.MyApp.Companion.initializeCamera
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import suzdalenko.photolapse.util.MyApp.Companion.releaseCamera
import suzdalenko.photolapse.util.PlaySound.errorSoundGetFoto
import suzdalenko.photolapse.util.PlaySound.playSoundGetFoto
import suzdalenko.photolapse.util.Settings.LogPhotoLapse
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class CameraActivity : AppCompatActivity() {
    private lateinit var currentLocale: Locale
    private var fileUploadService: FileUploadService? = null
    private lateinit var switchCompat: SwitchCompat
    private val conFileUploading = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FileUploadService.LocalBinder
            fileUploadService = binder.getService()
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }
    private var photoServiceActive = false
    private var photoCreateService: PhotoCreateService? = null
    private val conPhotoCreating = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PhotoCreateService.LocalBinder
            photoCreateService = binder.getService()
            photoServiceActive = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            photoCreateService = null
            photoServiceActive = false
        }
    }
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        currentLocale = applicationContext.resources.configuration.locales.get(0)
        findViewById<Button>(R.id.captureButton).setOnClickListener {
            takePhoto()
            LogPhotoLapse("take-photo-with-botton-in-CameraActivity")
        }

        // Registrar el receptor de broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(FirstPlaneReceiver(), IntentFilter("com.example.ACTION_EVENT"))


        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiApp::WakeLockTag")
        wakeLock?.acquire()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        switchCompat = findViewById(R.id.switchCompat)
        switchCompat.isChecked = prefs.getString("flash", "x") == "flash"
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){ prefs.edit().putString("flash", "flash").apply(); Toast.makeText(this, "Flash ON", Toast.LENGTH_SHORT).show()
            } else { prefs.edit().putString("flash", "x").apply(); ; Toast.makeText(this, "Flash OFF", Toast.LENGTH_SHORT).show() }
            LogPhotoLapse("change-switchCompat-CameraActivity")
        }

        // Establecer la referencia de la actividad en el servicio
        PhotoCreateService.activityCamara = WeakReference(this)
        FileUploadService.activityCamara = WeakReference(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { startForegroundService(Intent(this, PhotoCreateService::class.java)); startForegroundService(Intent(this, FileUploadService::class.java))
        } else { startService(Intent(this, PhotoCreateService::class.java)); startService(Intent(this, FileUploadService::class.java))}
        // Enlazar con el Service
        bindService(Intent(this, PhotoCreateService::class.java), conPhotoCreating, Context.BIND_AUTO_CREATE)
        bindService(Intent(this, FileUploadService::class.java), conFileUploading, Context.BIND_AUTO_CREATE)

        if (allPermissionsGranted()) {
            initializeCamera(this, this,  findViewById<PreviewView>(R.id.viewFinder))
        } else { ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS) }
    }

    private fun takePhoto() {
        val imageDir = File(externalMediaDirs.firstOrNull(), "images")
        if (!imageDir.exists()) { imageDir.mkdirs() }
        val imageCapture = getImageCapture() ?: return
        val photoFile = File(imageDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", currentLocale).format(System.currentTimeMillis())+".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        if(prefs.getString("flash", "x").toString() == "flash") { imageCapture.flashMode = ImageCapture.FLASH_MODE_ON } else { imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF }
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "ACT ${exc.message}", Toast.LENGTH_SHORT).show()
                    errorSoundGetFoto(applicationContext)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    playSoundGetFoto(applicationContext)
                    if (photoServiceActive) { photoCreateService?.let { service -> service.fotosCreadasActivity += 1 } }
                    Toast.makeText(baseContext, photoFile.name.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }



    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPictureInPictureMode1()
    }
    private fun enterPictureInPictureMode1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(1, 1)
            val pipParams = PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build()
            enterPictureInPictureMode(pipParams)
        } else { Toast.makeText(this, "Picture-in-Picture no es soportado en este dispositivo.", Toast.LENGTH_SHORT).show() }
    }
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) { } else { }
    }
    override fun onPause(){
        super.onPause()
    }
    override fun onStop(){
        super.onStop()
        releaseCamera(this)
        if (photoServiceActive) { photoCreateService?.restartCamaraService() }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera(this)
        if (photoServiceActive) { photoCreateService?.restartCamaraService() }
    }
    override fun onRestart(){
        super.onRestart()
        initializeCamera(this, this, findViewById<PreviewView>(R.id.viewFinder))
    }
}