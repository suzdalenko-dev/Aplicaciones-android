package suzdalenko.photolapse.util
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import suzdalenko.photolapse.R
import suzdalenko.photolapse.receiver.StartServicesReceiver
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService

class MyApp: Application() {
    companion object {
        lateinit var myApp: MyApp
        lateinit var alarmManager: AlarmManager
        lateinit var prefs : SharedPreferences
        var DISPARO_CAMARA : Long = 0

        fun getDateApp(volumeFiles: Double): String {
            val currentDate = Date()
            return "DATE: "+ SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRANCE).format(currentDate) + " VOLUME: "+volumeFiles.toInt().toString()+" MB"
        }
        fun isValidEmail(email: String): Boolean {
            return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
        fun formatSeconds(seconds: Long): String {
            if (seconds <= 0) { return "00:00:00" }
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            return String.format(Locale.FRANCE, "%02d:%02d:%02d", hours, minutes, secs).toString()
        }


        private var imageCapture: ImageCapture? = null
        var videoCapture: VideoCapture<Recorder>? = null
        var recording: Recording? = null
        lateinit var cameraExecutor: ExecutorService

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
                }}, ContextCompat.getMainExecutor(context))

        }
        fun initializedVIDEO(context: Context, lifecycleOwner: LifecycleOwner, viewFinder: PreviewView? = null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                if (viewFinder != null) { preview.setSurfaceProvider(viewFinder.surfaceProvider) }
                val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
                videoCapture = VideoCapture.withOutput(recorder)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
                } catch (e: Exception) {
                    Log.e("CameraX", "Fallo al iniciar la cámara.", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
        fun monitorFileSize(videoFile: File) {
            val maxSizeInBytes = 1 * 1024 * 1024 // 22
            Thread {
                while (recording != null) {
                    Log.d("suzdalenko_x_log", (videoFile.length() / 1024 / 1024).toString())
                    if (videoFile.length() > maxSizeInBytes) {
                        recording?.stop()
                        recording = null
                        break
                    }
                    Thread.sleep(222)
                }
            }.start()
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



        fun acquireWakeLock(context: Context): PowerManager.WakeLock {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLock").apply {
                acquire(10 * 60 * 1000L /*10 minutes*/)
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        myApp = MyApp()
        prefs = getSharedPreferences("suzdalenko.fotolapso", MODE_PRIVATE)
        prefs.edit().putString("flash", "").apply()
        prefs.edit().putLong("camera_frequency", 1800000L).apply()
        prefs.edit().putLong("update_frequency", 322L).apply()
        prefs.edit().putString("log", "false").apply()
        setInexactRepeatingAlarm(applicationContext)
    }

    private fun setInexactRepeatingAlarm(context: Context) {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var intent = Intent(context, StartServicesReceiver::class.java)
        var pendingIntent = PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, pendingIntent)

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        intent = Intent(context, StartServicesReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, pendingIntent)
    }
    @SuppressLint("ScheduleExactAlarm")
    fun setExactAlarm(context: Context){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StartServicesReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR, pendingIntent)
    }

    fun scheduleExactAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StartServicesReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 6, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val intervalMillis = AlarmManager.INTERVAL_FIFTEEN_MINUTES // 15 * 60 * 1000   15 minutes in milliseconds
        val triggerAtMillis = System.currentTimeMillis() + intervalMillis
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                AlertDialog.Builder(context)
                    .setTitle("Permission Required")
                    .setMessage("This app requires permission to schedule exact alarms for its functionality.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { val alarmManager = context.getSystemService(ALARM_SERVICE) as android.app.AlarmManager; alarmManager.canScheduleExactAlarms()
        } else { true }
    }
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val model = Build.MODEL.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return "$manufacturer$model"
    }
    fun filterToAllowedChars(input: String): String {
        val res = input.replace(" ", "")
        return res.replace("[^a-z0-9]".toRegex(RegexOption.IGNORE_CASE), "")
    }

}