package ai.metricsphere.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import ai.metricsphere.app.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var origin: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = Prefs(this)
        origin = intent.getStringExtra(EXTRA_ORIGIN).orEmpty().ifBlank { prefs.origin }
        val path = intent.getStringExtra(EXTRA_PATH) ?: "/dashboard"
        if (origin.isBlank() || !HostConfig.isAllowedHost(android.net.Uri.parse(origin).host, prefs.origin)) {
            goLogin()
            return
        }
        prefs.origin = origin

        val web = binding.webView
        WebView.setWebContentsDebuggingEnabled(true)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = "$userAgentString MetricSphereApp/0.1"
        }
        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(web, false)

        web.webChromeClient = object : WebChromeClient() {}
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val host = request.url.host
                if (!HostConfig.isAllowedHost(host, origin)) {
                    return true
                }
                val pathOnly = request.url.path.orEmpty()
                if (pathOnly == "/login" || pathOnly.startsWith("/login?")) {
                    Prefs(this@DashboardActivity).clearPairing()
                    SessionCookies.clearAll()
                    goLogin()
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progress.visibility = View.GONE
                val pathOnly = android.net.Uri.parse(url).path.orEmpty()
                if (pathOnly == "/login" || pathOnly.startsWith("/login")) {
                    Prefs(this@DashboardActivity).clearPairing()
                    SessionCookies.clearAll()
                    goLogin()
                }
            }
        }

        val startUrl = origin.trimEnd('/') + path
        web.loadUrl(startUrl)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (web.canGoBack()) {
                        web.goBack()
                    } else {
                        // Stay in app on root — move to background
                        moveTaskToBack(true)
                    }
                }
            },
        )
    }

    private fun goLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }

    companion object {
        const val EXTRA_ORIGIN = "origin"
        const val EXTRA_PATH = "path"
    }
}
