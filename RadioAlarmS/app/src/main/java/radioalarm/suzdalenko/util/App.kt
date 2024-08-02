package radioalarm.suzdalenko.util

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
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

        lateinit var executor: ScheduledExecutorService
        lateinit var exoPlayer: ExoPlayer
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        executor = Executors.newSingleThreadScheduledExecutor()
        exoPlayer = ExoPlayer.Builder(this).build()
    }
}