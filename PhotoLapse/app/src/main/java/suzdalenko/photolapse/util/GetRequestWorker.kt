package suzdalenko.photolapse.util

import android.content.Context
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class GetRequestWorker(val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Toast.makeText(appContext, "Worker executed", Toast.LENGTH_SHORT).show()

        val client = OkHttpClient()
        val request = Request.Builder().url("https://suzdalenko-dev.github.io/settings/photo_lapse/photo_lapse.json").build()
        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Handle the response here
                val responseData = response.body?.string()
                // Process the response data (e.g., save it to a file or a database)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.retry()
        }
    }

}