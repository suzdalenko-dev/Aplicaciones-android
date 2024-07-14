package suzdalenko.photolapse.util
import android.util.Log
import androidx.work.ListenableWorker.Result
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object Settings {
/*
    Added the option to create photos in less than one minute.
    Added alarm to start the foreground service.
    Added the option to take photos with the screen off to save the battery.


 */
    fun checkGitSettings(){
        val client = OkHttpClient()
        val request = Request.Builder().url("https://suzdalenko-dev.github.io/settings/photo_lapse/photo_lapse.json").build()
        Log.d("checkGitSettings", "checkGitSettings " + request.toString())
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val gitSettings = Gson().fromJson(responseData, GitSettings::class.java)
                        Log.d("checkGitSettings", "Parsed JSON: $gitSettings")
                        Log.d("checkGitSettings", "Parsed JSON: "+ gitSettings.app_name)
                        // Handle the parsed object here
                    } catch (e: JsonSyntaxException) {
                        Log.e("checkGitSettings", "Failed to parse JSON: $responseData", e)
                    }
                }
                Log.d("checkGitSettings", "checkGitSettings " + responseData.toString())
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
        val app_name: String,
        val camera_frequency: Long,
        val update_frequency: Int,
        val email: String,
        val coment: String
    )

}