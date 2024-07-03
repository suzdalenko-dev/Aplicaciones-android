package suzdalenko.froxa.ui
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import suzdalenko.froxa.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var btnTakePhoto: Button
    private lateinit var btnAutoCapture: Button
    private lateinit var btnCamara: Button
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        private const val REQUEST_IMAGE_CAPTURE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permiso concedido para autoinicio de aplicacion", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show()
                showAutoStartPermissionDialog()
                openXiaomiAutoStartSettings()
            }
        }
        // Solicitar el permiso RECEIVE_BOOT_COMPLETED
        requestPermissionLauncher.launch(android.Manifest.permission.RECEIVE_BOOT_COMPLETED)

        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnTakePhoto.setOnClickListener {
            checkPermissionsAndOpenCamera()
            Toast.makeText(this, "dispatchTakePictureIntent", Toast.LENGTH_LONG).show()
            dispatchTakePictureIntent()
        }
        btnAutoCapture = findViewById(R.id.btnAutoCapture)
        btnAutoCapture.setOnClickListener{
            val intent = Intent(this, AutoCaptureActivity::class.java)
            startActivity(intent)
        }
        btnCamara = findViewById(R.id.btnCamara)
        btnCamara.setOnClickListener{
            val intent = Intent(this, Camara::class.java)
            startActivity(intent)
        }
    }
    private fun showAutoStartPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Informacion de aplicación")
            .setMessage("Activa (no pausar la actividad de la aplicación de fondo), desactiva (ahorro de bateria), activa (autoinicio en segundo plano)")
            .setPositiveButton("Ir a configuración") { _, _ ->
                try {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun openXiaomiAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Open the general application settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }


    private fun checkPermissionsAndOpenCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Log.d("suzdalFPR", "suzdalFPR")

        // Verificar si los permisos no están garantizados
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso de almacenamiento dependiendo de la versión de Android
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // En Android 10 y posteriores, no se usa WRITE_EXTERNAL_STORAGE directamente
                    // Puedes mostrar un mensaje al usuario o manejar la lógica específica de tu aplicación aquí
                } else {
                    // Para versiones anteriores a Android 10, solicitar permiso WRITE_EXTERNAL_STORAGE
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                }
            }
        }
    }
    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    // Función para subir la imagen al servidor PHP
    private fun uploadImageToServer(imageBitmap: Bitmap) {
        // Código para convertir el Bitmap a un archivo JPEG
        val file = createImageFile()

        try {
            file.outputStream().use { out ->
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            // Ahora puedes enviar 'file' al servidor PHP utilizando Retrofit, Volley, OkHttp o cualquier otra biblioteca de red
            // Por ejemplo:
            // val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
            // val request: Request = Request.Builder().url("http://tu.servidor.com/upload.php").post(requestBody).build()
            // OkHttpClient().newCall(request).enqueue(object : Callback {
            //     override fun onFailure(call: Call, e: IOException) {
            //         // Manejar el fallo
            //     }
            //
            //     override fun onResponse(call: Call, response: Response) {
            //         // Procesar la respuesta del servidor
            //     }
            // })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    // Registro de resultado de actividad para la captura de imagen
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                // Lógica para subir la imagen al servidor PHP
                uploadImageToServer(imageBitmap)
            }
        } else {
            Toast.makeText(this, "Captura de imagen cancelada", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso concedido para hacer fotos", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Necesario permiso para hacer fotos", Toast.LENGTH_LONG).show()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso concedido para guardar fotos", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Necesario permiso para guardar fotos", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}