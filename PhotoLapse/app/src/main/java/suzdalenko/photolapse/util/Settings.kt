package suzdalenko.photolapse.util
import android.util.Log
import androidx.work.ListenableWorker.Result
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import suzdalenko.photolapse.util.MyApp.Companion
import suzdalenko.photolapse.util.MyApp.Companion.myApp
import suzdalenko.photolapse.util.MyApp.Companion.prefs
import java.io.IOException

object Settings {
/*
    Added the option to create photos in less than one minute.
    Added alarm to start the foreground service.
    Added the option to take photos with the screen off to save the battery.


 */
    fun checkGitSettings(){
        val client  = OkHttpClient()
        val request = Request.Builder().url("https://suzdalenko-dev.github.io/settings/photo_lapse/photo_lapse.json").build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val gitSettings = Gson().fromJson(responseData, GitSettings::class.java)
                        if(gitSettings.use_settings == "true"){
                            prefs.edit().putLong("camera_frequency", gitSettings.camera_frequency * 1000L).apply()
                            prefs.edit().putLong("update_frequency", gitSettings.update_frequency).apply()
                        }
                        if(gitSettings.log == "true") prefs.edit().putString("log", "true").apply()
                        else prefs.edit().putString("log", "false").apply()

                        Log.d("getGitSettings", gitSettings.toString())
                    } catch (_: JsonSyntaxException) { ; }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.retry()
        }
    }

    data class GitSettings(
        val use_settings: String,
        val camera_frequency: Long,
        val update_frequency: Long,
        val log: String,
        val coment: String
    )

    fun LogPhotoLapse(message: String){
        if(prefs.getString("log", "false") == "true"){
            CoroutineScope(Dispatchers.IO).launch {
                var phoneName = myApp.getDeviceName()
                phoneName = myApp.filterToAllowedChars(phoneName)
                val url = "https://intranet.froxa.net/aimagen/logs?log_data=$message&phone_name=$phoneName"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Result.success()
                    } else {
                        Result.retry()
                    }
                } catch (e: IOException){
                    Result.retry()
                }
            }
        }
    }

}