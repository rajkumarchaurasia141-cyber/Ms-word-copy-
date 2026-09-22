package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WordWriterScreen(
                        modifier = Modifier.padding(innerPadding),
                        activity = this
                    )
                }
            }
        }
    }
}

// Javascript Interface to handle PDF export via Android system PrintManager
class WebAppInterface(private val context: Context, private val webView: WebView) {
    @JavascriptInterface
    fun printDocument(fileName: String) {
        val activity = context as? ComponentActivity ?: return
        activity.runOnUiThread {
            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return@runOnUiThread
            val printAdapter = webView.createPrintDocumentAdapter(fileName)
            val jobName = "$fileName Document"
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WordWriterScreen(modifier: Modifier = Modifier, activity: ComponentActivity) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        // Allow all standard navigation inside our webview
                        return false
                    }
                }
                
                webChromeClient = WebChromeClient()

                // Set software rendering layer to prevent any MESA/GPU driver failures in headless environment
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                // Register standard Android print bridge
                addJavascriptInterface(WebAppInterface(context, this), "AndroidPrint")

                // Load our local React + Tailwind single-page app
                loadUrl("file:///android_asset/index.html")
            }
        }
    )
}
