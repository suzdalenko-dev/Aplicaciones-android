package suzdalenko.speedometer.util
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpeedViewModel: ViewModel() {
    private val _currentSpeed = MutableLiveData<String>()
    val currentSpeed: LiveData<String> get() = _currentSpeed
    fun updateCurrentSpeed(speed: String) { _currentSpeed.postValue(speed) }


    private val _totalDistance = MutableLiveData<String>()
    val totalDistance: LiveData<String> get() = _totalDistance
    fun updateTotalDistance(distance: String) { _totalDistance.postValue(distance) }

    private val _altitude = MutableLiveData<String>()
    val altitude: LiveData<String> get() = _altitude
    fun updateAltitude(altitude: String) { _altitude.postValue(altitude) }

    private val _elapsedTime = MutableLiveData<String>()
    val elapsedTime: LiveData<String> get() = _elapsedTime
    fun updateElapsedTime(time: String) {
        _elapsedTime.postValue(time)
    }
}