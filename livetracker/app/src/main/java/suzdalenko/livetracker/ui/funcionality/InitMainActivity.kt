package suzdalenko.livetracker.ui.funcionality
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import suzdalenko.livetracker.R
import suzdalenko.livetracker.ui.Empty
import suzdalenko.livetracker.ui.MainActivity
import suzdalenko.livetracker.ui.MapActivity
import suzdalenko.livetracker.ui.funcionality.StartService.serviceStart
import suzdalenko.livetracker.util.App.Companion.sh
import suzdalenko.livetracker.util.HttpRequest.getHttp

object InitMainActivity {
    fun initMain(activity: MainActivity, context: Context){
        val email_edit            = activity.findViewById<EditText>(R.id.email)
        val password_edit         = activity.findViewById<EditText>(R.id.password)
        val confirm_password_edit = activity.findViewById<EditText>(R.id.confirm_password)
        val switch_monitor        = activity.findViewById<SwitchCompat>(R.id.switch_monitor)

        switch_monitor.isChecked = sh.getBoolean("target", false)
        switch_monitor.setOnCheckedChangeListener { _, isChecked ->
            if(sh.getString("password", "").toString() == confirm_password_edit.text.toString().trim() && confirm_password_edit.text.toString().trim().length >= 3){
                if(isChecked){ sh.edit().putBoolean("target", true).apply();
                } else { sh.edit().putBoolean("target", false).apply() }
            } else {
                Toast.makeText(activity, activity.getString(R.string.confirm_change_role), Toast.LENGTH_SHORT).show()
            }
            doSomething(switch_monitor, confirm_password_edit)
        }

        activity.findViewById<Button>(R.id.btn_login).setOnClickListener {
            if(sh.getString("logged", "").toString() == "logged"){
                if(email_edit.text.toString().trim() == sh.getString("email", "") && password_edit.text.toString().trim() == sh.getString("password", "").toString()){
                    ejecuteRequest(email_edit, password_edit)
                    if (!sh.getBoolean("target", false)) {
                        // no dejo entrar si no es monitor
                        activity.startActivity(Intent(activity, MapActivity::class.java))
                    } else {
                        Toast.makeText(activity, "Login success", Toast.LENGTH_SHORT).show()
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps/?hl=es"))
                        context.startActivity(browserIntent)
                    }
                } else {
                    Toast.makeText(activity, "Insert valid credentials", Toast.LENGTH_SHORT).show()
                }
            } else {
                if(isValidEmail(email_edit.text.toString().trim()) && password_edit.text.toString().trim().length >= 3){
                    sh.edit().putString("logged", "logged").apply()
                    sh.edit().putString("email", email_edit.text.toString().trim()).apply()
                    sh.edit().putString("password", password_edit.text.toString().trim()).apply()
                    Toast.makeText(activity, "Register and login success", Toast.LENGTH_SHORT).show()
                    ejecuteRequest(email_edit, password_edit)
                    if (!sh.getBoolean("target", false)) {
                        // no dejo entrar si no es monitor
                        activity.startActivity(Intent(activity, MapActivity::class.java))
                    } else {
                        Toast.makeText(activity, "Login success", Toast.LENGTH_SHORT).show()
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps/?hl=es"))
                        context.startActivity(browserIntent)
                    }
                } else {
                    Toast.makeText(activity, "Insert valid credentials", Toast.LENGTH_SHORT).show()
                }
            }
            serviceStart(activity)
        }

        if(sh.contains("email").toString().length >= 3 && sh.getString("password", "").toString().length >= 3){
            email_edit.setText(sh.getString("email", "").toString())
            password_edit.setText(sh.getString("password", "").toString())
        }
    }


    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun ejecuteRequest(email_edit: EditText, password_edit: EditText){
        val sVal = if(sh.getBoolean("target", false)) {1} else {0}
        getHttp(email_edit.text.toString().trim(), "${password_edit.text.toString().trim()}_$sVal")
    }
    fun doSomething(switch_monitor: SwitchCompat, confirm_password_edit: EditText){
        Handler(Looper.getMainLooper()).postDelayed({
            switch_monitor.isChecked = sh.getBoolean("target", false)
            confirm_password_edit.setText("")
        }, 1100)
    }



}