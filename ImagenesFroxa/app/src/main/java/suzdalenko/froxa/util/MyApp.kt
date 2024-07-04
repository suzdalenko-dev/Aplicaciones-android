package suzdalenko.froxa.util
import android.app.Application
import android.content.SharedPreferences
import android.provider.Settings.System.putString

class MyApp: Application() {
    companion object {
        lateinit var instance: MyApp
        lateinit var prefs : SharedPreferences
        val HACER_FOTO_CADA_MILISEC: Long = 30000
        val SUBIR_ARCHIVOS_CADA_SEC: Long = 22
    }
    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("suzdalenko.froxa", MODE_PRIVATE)
    }
}