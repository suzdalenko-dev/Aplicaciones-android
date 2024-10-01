package suzdalenko.photolapse.ui
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import suzdalenko.photolapse.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoActivity : AppCompatActivity() {
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        if (allPermissionsGranted()) { startCamera()
        } else { ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS) }

        // Configurar el directorio donde se guardarán los videos
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Botón para iniciar/detener la grabación
        val recordButton = findViewById<Button>(R.id.record_button)
        recordButton.setOnClickListener {
            if (recording != null) {
                // Detener la grabación
                recording?.stop()
                recording = null
                recordButton.text = "Start Recording"
            } else {
                // Iniciar la grabación
                startRecording()
                recordButton.text = "Stop Recording"
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configuración del visor de la cámara
            val preview = Preview.Builder().build()
            val previewView = findViewById<PreviewView>(R.id.previewView)
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Configuración del VideoCapture
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind de cualquier cámara anterior y bind a la nueva
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Fallo al iniciar la cámara.", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        val videoFile = File(
            outputDirectory, SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS", Locale.US
            ).format(System.currentTimeMillis()) + ".mp4"
        )

        val outputOptions = FileOutputOptions.Builder(videoFile).build()
        recording = videoCapture.output
            .prepareRecording(this, outputOptions).apply {
                if (ActivityCompat.checkSelfPermission(this@VideoActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        monitorFileSize(videoFile)
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                            Log.d("CameraX", "Video recording finished. File saved: ${videoFile.absolutePath}"
                            )
                        } else {
                            Log.e("CameraX", "Video recording error: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = File(externalMediaDirs.firstOrNull(), "video")
        if (!mediaDir.exists()) { mediaDir.mkdirs() }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
    private fun monitorFileSize(videoFile: File) {
        val maxSizeInBytes = 22 * 1024 * 1024 // 22 MB
        Thread {
            while (recording != null) {
                if (videoFile.length() > maxSizeInBytes) {
                    recording?.stop()
                    recording = null
                    break
                }
                Thread.sleep(333)
            }
        }.start()
    }
}