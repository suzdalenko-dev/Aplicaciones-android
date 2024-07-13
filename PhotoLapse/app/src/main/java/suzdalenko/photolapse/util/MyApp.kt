package suzdalenko.photolapse.util
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Patterns
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyApp: Application() {
    companion object {
        lateinit var instance: MyApp
        lateinit var prefs : SharedPreferences
        var DISPARO_CAMARA : Long = 0
        val UPLOAD_FILES_EACH_SEC: Long = 322               // 660 -> 11 minutos

        fun getDateApp(volumeFiles: Double): String {
            val currentDate = Date()
            return "DATE: "+ SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRANCE).format(currentDate) + " VOLUME: "+volumeFiles.toInt().toString()+" MB"
        }
        fun isValidEmail(email: String): Boolean {
            return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
        fun formatSeconds(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            return String.format(Locale.FRANCE, "%02d:%02d:%02d", hours, minutes, secs).toString()
        }


        private var imageCapture: ImageCapture? = null
        fun initializeCamera(context: Context, lifecycleOwner: LifecycleOwner, viewFinder: PreviewView? = null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                imageCapture = ImageCapture.Builder().build()
                try {
                    cameraProvider.unbindAll()
                    if (viewFinder != null) { preview.setSurfaceProvider(viewFinder.surfaceProvider) }
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } catch (exc: Exception) {
                    Log.e("CameraUtil", "Error initializing camera: ${exc.message}", exc)
                } }, ContextCompat.getMainExecutor(context))

        }
        fun getImageCapture(): ImageCapture? {
            return imageCapture
        }
        fun releaseCamera(context: Context) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll() }, ContextCompat.getMainExecutor(context))
        }

    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("suzdalenko.fotolapso", MODE_PRIVATE)
        prefs.edit().putString("flash", "").apply()
        prefs.edit().putLong("camera_frequency", (1800 * 1000).toLong()).apply()

        prefs.edit().putLong("camera_frequency", (22000).toLong()).apply()
    }
}