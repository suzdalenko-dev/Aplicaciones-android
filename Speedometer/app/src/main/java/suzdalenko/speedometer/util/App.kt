package suzdalenko.speedometer.util

import android.app.Application
import androidx.lifecycle.ViewModelProvider

class App : Application() {
    companion object {
        lateinit var speedViewModel: SpeedViewModel
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializa el ViewModel
        speedViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(SpeedViewModel::class.java)
    }
}