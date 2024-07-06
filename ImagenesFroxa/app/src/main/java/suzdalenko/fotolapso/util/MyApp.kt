package suzdalenko.fotolapso.util
import android.app.Application
import android.content.SharedPreferences
import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyApp: Application() {
    companion object {
        lateinit var instance: MyApp
        lateinit var prefs : SharedPreferences
        val MAKE_PHOTO_EVERY_MILISEC: Long = 1800 * 1000    // 30 minutos
        val UPLOAD_FILES_EACH_SEC: Long = 660               // 11 minutos

        fun getDateApp(): String {
            val currentDate = Date()
            return "DATE: "+ SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRANCE).format(currentDate)
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
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("suzdalenko.fotolapso", MODE_PRIVATE)
        prefs.edit().putString("flash", "").apply()
    }
}