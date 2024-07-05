package suzdalenko.froxa.util
import android.app.Application
import android.content.SharedPreferences
import android.provider.Settings.System.putString

class MyApp: Application() {
    companion object {
        lateinit var instance: MyApp
        lateinit var prefs : SharedPreferences
        val MAKE_PHOTO_EVERY_MILISEC: Long = 1800 * 1000    // 30 minutos
        val UPLOAD_FILES_EACH_SEC: Long = 660               // 11 minutos
    }
    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("suzdalenko.froxa", MODE_PRIVATE)
    }
}