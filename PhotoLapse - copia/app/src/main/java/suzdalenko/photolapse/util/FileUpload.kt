package suzdalenko.photolapse.util

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class FileUpload(private val file: File) {

    companion object {
        private const val SERVER_URL = "https://intranet.froxa.net/aimagen/"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(999999, TimeUnit.SECONDS) // Tiempo máximo para conectar con el servidor
        .readTimeout(999999, TimeUnit.SECONDS)    // Tiempo máximo para leer datos del servidor
        .writeTimeout(999999, TimeUnit.SECONDS)   // Tiempo máximo para escribir datos al servidor
        .build()

    fun uploadFile(callback: (String) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback.invoke("Error al conectar con el servidor: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    callback.invoke(it)
                } ?: run {
                    callback.invoke("Error: Respuesta vacía del servidor")
                }
            }
        })
    }
}