package suzdalenko.froxa.util
import android.app.Application
import android.content.SharedPreferences
import android.provider.Settings.System.putString

class MyApp: Application() {
    companion object {
        lateinit var instance: MyApp
        lateinit var prefs : SharedPreferences
        val MAKE_PHOTO_EVERY_MILISEC: Long = 1800 * 1000
        val UPLOAD_FILES_EACH_SEC: Long = 99
        var photos_created  = 0
        var photos_uploaded = 0
        var seconds_to_photo_creation = 0
    }
    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("suzdalenko.froxa", MODE_PRIVATE)
    }
}