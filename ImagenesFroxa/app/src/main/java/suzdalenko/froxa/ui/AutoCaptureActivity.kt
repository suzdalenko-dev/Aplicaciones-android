package suzdalenko.froxa.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import suzdalenko.froxa.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class AutoCaptureActivity : AppCompatActivity() {
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            dispatchTakePictureIntent()
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                uploadImageToServer(photoUri)
                // Programar la próxima captura automática
                scheduleNextCapture()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File = createImageFile()
        photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun uploadImageToServer(photoUri: Uri) {
        try {
            val imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
            val file = createImageFile()
            file.outputStream().use { out ->
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            // Aquí puedes implementar la lógica para subir el archivo al servidor PHP
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

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

    private fun scheduleNextCapture() {
        // Esperar 5 segundos antes de la próxima captura automática
        Thread.sleep(5000)
        dispatchTakePictureIntent()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 102
    }
}