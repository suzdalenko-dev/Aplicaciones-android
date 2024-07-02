package suzdalenko.froxa.ui
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import suzdalenko.froxa.R
class MainActivity : AppCompatActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show()
                showAutoStartPermissionDialog()
                openXiaomiAutoStartSettings()
            }
        }
        // Solicitar el permiso RECEIVE_BOOT_COMPLETED
        requestPermissionLauncher.launch(android.Manifest.permission.RECEIVE_BOOT_COMPLETED)

        // Mostrar diálogo para configurar el inicio automático en dispositivos Xiaomi
    }
    private fun showAutoStartPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Informacion de aplicación")
            .setMessage("Activa (no pausar la actividad de la aplicación de fondo), desactiva (ahorro de bateria), activa (autoinicio en segundo plano)")
            .setPositiveButton("Ir a configuración") { _, _ ->
                try {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun openXiaomiAutoStartSettings() {
        try {
            val intent = Intent()
            intent.component = android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Open the general application settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
}