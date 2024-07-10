package suzdalenko.photolapse.ui
import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import suzdalenko.photolapse.R
import suzdalenko.photolapse.service.FotoCreateService
import suzdalenko.photolapse.util.MyApp.Companion.MAKE_PHOTO_EVERY_MILISEC
import suzdalenko.photolapse.util.MyApp.Companion.isValidEmail
import suzdalenko.photolapse.util.MyApp.Companion.prefs

class MainActivity : AppCompatActivity() {
    private var minuteValue: Int = 1
    private lateinit var editEmail: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnAutoCapture: Button
    private lateinit var btnCamara: Button
    private lateinit var timePicker: TimePicker
    private var fotoCreateService: FotoCreateService? = null
    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FotoCreateService.LocalBinder
            fotoCreateService = binder.getService()
            isServiceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            fotoCreateService = null
            isServiceBound = false
        }
    }
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnGuardar = findViewById(R.id.btnGuardar)
        editEmail = findViewById<EditText>(R.id.etEmail)
        if(prefs.getString("email", "email").toString() != "email"){ editEmail.setText(prefs.getString("email", "email").toString()) }
        btnGuardar.setOnClickListener{
            val email = editEmail.text.toString().trim()
            if(isValidEmail(email)){
                prefs.edit().putString("email", email).apply()
                Toast.makeText(this, getString(R.string.email_saved), Toast.LENGTH_LONG).show();
                editEmail.setText(prefs.getString("email", "email").toString()); editEmail.clearFocus()
            } else { Toast.makeText(this, getString(R.string.insert_email_correct), Toast.LENGTH_LONG).show(); editEmail.setText("") }
        }
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnTakePhoto.setOnClickListener {
            checkPermissionsAndOpenCamera()
            dispatchTakePictureIntent()
            showAutoStartPermissionDialog()
        }
        btnAutoCapture = findViewById(R.id.btnAutoCapture)
        btnAutoCapture.setOnClickListener{
            val intent = Intent(this, ClickCaptureActivity::class.java)
            startActivity(intent)
            openXiaomiAutoStartSettings()
        }
        btnCamara = findViewById(R.id.btnCamara)
        btnCamara.setOnClickListener{
            if(prefs.getString("email", "email").toString() != "email"){
                val intent = Intent(this, CameraActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.insert_email_correct), Toast.LENGTH_LONG).show(); editEmail.setText("")
            }
        }
        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)
        timePicker.hour   = prefs.getInt("hourOfDay", 0)
        timePicker.minute = prefs.getInt("minute", 30)
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            minuteValue = if(minute < 1 && hourOfDay == 0) { 1; } else { minute }
            MAKE_PHOTO_EVERY_MILISEC = ((hourOfDay * 3600 + minuteValue * 60) * 1000).toLong()
            prefs.edit().putInt("hourOfDay", hourOfDay).apply()
            prefs.edit().putInt("minute", minuteValue).apply()
            prefs.edit().putLong("camera_frequency", MAKE_PHOTO_EVERY_MILISEC).apply()
            /* con esto dejo solo un hilo de ejecucion para hacer las fotos, si no hay varios */
            if (isServiceBound) { fotoCreateService?.restartTakingPhotos() }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
           // startActivity(Intent(this, Camara::class.java)); finish()
        }
        bindService(Intent(this, FotoCreateService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
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

        // Verificar si los permisos no están garantizados
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso de almacenamiento dependiendo de la versión de Android
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(this, CameraActivity::class.java))
                    finish()
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
    // Registro de resultado de actividad para la captura de imagen
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
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