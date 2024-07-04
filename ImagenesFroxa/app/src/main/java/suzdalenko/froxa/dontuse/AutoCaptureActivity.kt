package suzdalenko.froxa.dontuse

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import suzdalenko.froxa.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoCaptureActivity : AppCompatActivity() {

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var photoUri: Uri
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        // Inicializar el launcher para capturar la imagen
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                uploadImageToServer(photoUri)
                scheduleNextCapture()
                playShutterSound()
            }
        }

        // Inicializar el SoundPool con AudioAttributes adecuados
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Cargar el sonido del obturador predeterminado del sistema
        soundId = soundPool.load(MediaStore.Audio.Media.INTERNAL_CONTENT_URI.toString(), 1)

        // Iniciar la captura automática o solicitar permisos si no están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            dispatchTakePictureIntent()
        }
    }

    // Método para iniciar la captura de la imagen
    private fun dispatchTakePictureIntent() {
        val photoFile: File = createImageFile()
        photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    // Método para subir la imagen al servidor
    private fun uploadImageToServer(photoUri: Uri) {
        try {
            // Código para subir la imagen al servidor
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Método para crear un archivo de imagen temporal
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    // Manejo de permisos de la aplicación
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Permisos necesarios no concedidos", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Programar la próxima captura automática
    private fun scheduleNextCapture() {
        // Esperar 5 segundos antes de la próxima captura automática
        Thread.sleep(5000)
        dispatchTakePictureIntent()
    }

    // Reproducir el sonido del obturador predeterminado del sistema
    private fun playShutterSound() {
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    // Liberar recursos cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}