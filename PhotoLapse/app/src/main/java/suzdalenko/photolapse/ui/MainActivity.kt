package suzdalenko.photolapse.ui
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
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
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat.canScheduleExactAlarms
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import suzdalenko.photolapse.R
import suzdalenko.photolapse.receiver.StartServicesReceiver
import suzdalenko.photolapse.service.FileUploadService
import suzdalenko.photolapse.service.PhotoCreateService
import suzdalenko.photolapse.util.MyApp
import suzdalenko.photolapse.util.MyApp.Companion.isValidEmail
import suzdalenko.photolapse.util.MyApp.Companion.myApp
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import suzdalenko.photolapse.util.Settings.LogPhotoLapse

class MainActivity : AppCompatActivity() {
    private lateinit var requestExactAlarmPermissionLauncher: ActivityResultLauncher<Intent>
    private var minuteValue: Double = .1
    private lateinit var editEmail: EditText
    private lateinit var btnGuardar: Button
    private lateinit var timePicker: TimePicker
    private var fotoCreateService: PhotoCreateService? = null
    private var isServiceBound = false
    private val conPhotoCreating = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PhotoCreateService.LocalBinder
            fotoCreateService = binder.getService()
            isServiceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            fotoCreateService = null
            isServiceBound = false
        }
    }
    private var fileUploadService: FileUploadService? = null
    private val conFileUploading = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FileUploadService.LocalBinder
            fileUploadService = binder.getService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            fileUploadService = null
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
                Toast.makeText(this, getString(R.string.email_saved), Toast.LENGTH_SHORT).show();
                editEmail.setText(prefs.getString("email", "email").toString()); editEmail.clearFocus()
                LogPhotoLapse("save-btnGuardar-MainActivity-${email}")
            } else { Toast.makeText(this, getString(R.string.insert_email_correct), Toast.LENGTH_SHORT).show(); editEmail.setText("") }
        }
        // test button
        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            checkPermissionsAndOpenCamera()
            dispatchTakePictureIntent()
        }
        // setting button
        findViewById<Button>(R.id.btnAutoCapture).setOnClickListener{
            startActivity(Intent(this, SettingActivity::class.java))
        }
        // camara button
        findViewById<Button>(R.id.btnCamara).setOnClickListener{
            if(prefs.getString("email", "email").toString() != "email"){
                val intent = Intent(this, CameraActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.insert_email_correct), Toast.LENGTH_SHORT).show(); editEmail.setText("")
            }
        }
        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)
        timePicker.hour   = prefs.getInt("hourOfDay", 0)
        timePicker.minute = prefs.getInt("minute", 30)
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            minuteValue = if(minute.toInt() < 1 && hourOfDay.toInt() == 0) { 0.1; } else { minute.toDouble() }
            val x = ((hourOfDay.toInt()  * 3600 + minuteValue * 60) * 1000).toLong()
            prefs.edit().putInt("hourOfDay", hourOfDay.toInt()).apply()
            prefs.edit().putInt("minute", minuteValue.toInt()).apply()
            prefs.edit().putLong("camera_frequency", x).apply()
            LogPhotoLapse("change-timePicker-MainActivity-${x}")
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { startForegroundService(Intent(this, PhotoCreateService::class.java)); startForegroundService(Intent(this, FileUploadService::class.java))
            } else { startService(Intent(this, PhotoCreateService::class.java)); startService(Intent(this, FileUploadService::class.java))}
            // this line make it imposible to turn off the service
            bindService(Intent(this, PhotoCreateService::class.java), conPhotoCreating, Context.BIND_AUTO_CREATE)
            bindService(Intent(this, FileUploadService::class.java), conFileUploading, Context.BIND_AUTO_CREATE)
        }
        requestExactAlarmPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (myApp.canScheduleExactAlarms(applicationContext)) {
                myApp.setExactAlarm(applicationContext)
                myApp.scheduleExactAlarm(applicationContext)
            } else {
                Toast.makeText(this, "Permission denied. Exact alarms won't work.", Toast.LENGTH_SHORT).show()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!myApp.canScheduleExactAlarms(applicationContext)) {
                requestScheduleExactAlarmsPermission()
            } else {
                myApp.setExactAlarm(applicationContext)
                myApp.scheduleExactAlarm(applicationContext)
            }
        } else {
            myApp.setExactAlarm(applicationContext)
            myApp.scheduleExactAlarm(applicationContext)
        }

        startActivity(Intent(this, VideoActivity::class.java))
    }
    private fun requestScheduleExactAlarmsPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app requires permission to schedule exact alarms for its functionality.")
            .setPositiveButton("Grant Permission") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                requestExactAlarmPermissionLauncher.launch(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission denied. Exact alarms won't work.", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun checkPermissionsAndOpenCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE) }
        if (storagePermission != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE) }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> }
    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

}