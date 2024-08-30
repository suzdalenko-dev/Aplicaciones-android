package suzdalenko.livetracker.ui
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import suzdalenko.livetracker.R
import suzdalenko.livetracker.ui.funcionality.LocationPermision.locationPerm
import suzdalenko.livetracker.ui.funcionality.StartService.serviceStart
import suzdalenko.livetracker.util.App.Companion.sh
class MapActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val webView: WebView = findViewById(R.id.webview)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        val id = sh.getString("id", "").toString()
        webView.loadUrl("https://live-tracker-solution.web.app?id=$id")

        serviceStart(this)
    }
}