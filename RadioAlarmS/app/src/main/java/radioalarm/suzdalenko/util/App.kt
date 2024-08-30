package radioalarm.suzdalenko.util

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.media3.exoplayer.ExoPlayer
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class App: Application() {
    companion object {
        lateinit var instance: App
            private set

        private var _globalString: String = ""
        var globalString: String
            get() = _globalString
            set(value) { _globalString = value }

        var executor =  Executors.newSingleThreadScheduledExecutor()
        var exoPlayer: ExoPlayer? = null
        var handler = Handler(Looper.getMainLooper())
        lateinit var sh: SharedPreferences

        fun getCurrentTime(): Pair<Int, Int> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val currentTime = LocalTime.now()
                Pair(currentTime.hour, currentTime.minute)
            } else {
                val calendar = Calendar.getInstance()
                Pair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sh = getSharedPreferences("suzdalenko", MODE_PRIVATE)
    }
}