package suzdalenko.froxa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import suzdalenko.froxa.ui.Camara

class PrimerPlanoReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        /* no lo usare ya que no es capaz de abrir activity */
        // Log.d("estadoActualActivity", "---------------------------------------")
        // intent?.let {
        //     if (it.action == "com.example.ACTION_EVENT") {
        //         Log.d("estadoActualActivity", it.action.toString())
        //         val message = it.getStringExtra("message")
        //         val startIntent = Intent(context, Camara::class.java)
        //         startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //         context?.startActivity(startIntent)
        //         Log.d("estadoActualActivity", "receiver funcionando")
        //     }
        // }
    }
}