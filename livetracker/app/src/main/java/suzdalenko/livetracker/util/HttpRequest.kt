package suzdalenko.livetracker.util
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import suzdalenko.livetracker.util.App.Companion.sh
import java.io.IOException
import java.util.Locale

object HttpRequest {
    fun getHttp(emal: String, pass: String){
        val stringArray: Array<String> = emal.lowercase().split("").toTypedArray()
        var id = ""
        val letters = arrayOf("q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m")
        val resLett = arrayOf("m","n","b","v","c","x","z","l","k","j","h","g","f","d","s","a","p","o","i","u","y","t","r","e","w","q","1","2","3","4","5","6","7","8","9","0")
        for (l in letters) {
            for (c in stringArray) {
                if (c == l) {
                    val index = letters.indexOf(l)
                    id += resLett[index]
                }
            }
        }
        val data = "id="+id+"&cred="+emal+"_"+pass
        sh.edit().putString("id", id).apply()
        Log.d("log_suzdalenko", data)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client  =  OkHttpClient()
                val request = Request.Builder().url("https://intranet.froxa.net/aimagen/live/user/index.php?$data").build()
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            Log.d("log_suzdalenko", "TOP "+responseData.toString())
                        } else {
                            Log.d("log_suzdalenko", "BOTTOM "+response.toString())
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shareLoc(locData: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client =  OkHttpClient()
                val request = Request.Builder().url("https://intranet.froxa.net/aimagen/live/loc/index.php?$locData").build()
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                        } else {

                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}