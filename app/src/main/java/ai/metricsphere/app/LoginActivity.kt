package ai.metricsphere.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ai.metricsphere.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)

        if (tryResumeSession()) return

        binding.inputPin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptPair()
                true
            } else {
                false
            }
        }

        binding.buttonLogin.setOnClickListener { attemptPair() }
        binding.buttonSignup.setOnClickListener { openSignupRequest() }
    }

    private fun openSignupRequest() {
        val uri = Uri.parse(SIGNUP_URL)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    /** Reuse 1-year device session if cookie + paired origin still present. */
    private fun tryResumeSession(): Boolean {
        val origin = prefs.origin.trim()
        if (origin.isEmpty()) return false
        val host = Uri.parse(origin).host
        if (!HostConfig.isAllowedHost(host, origin)) {
            prefs.clearPairing()
            return false
        }
        val cookie = CookieManager.getInstance().getCookie(origin).orEmpty()
        if (!cookie.contains("gp_session=")) return false

        startActivity(
            Intent(this, DashboardActivity::class.java).apply {
                putExtra(DashboardActivity.EXTRA_ORIGIN, origin)
                putExtra(DashboardActivity.EXTRA_PATH, "/dashboard")
            },
        )
        finish()
        return true
    }

    private fun attemptPair() {
        val pin = binding.inputPin.text?.toString().orEmpty().filter { it.isDigit() }
        if (pin.length != 6) {
            showError(getString(R.string.error_pin))
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                AuthClient.pairWithPin(pin)
            }
            setLoading(false)

            if (result.ok && !result.origin.isNullOrBlank()) {
                val origin = result.origin
                prefs.origin = origin
                SessionCookies.applyToWebView(origin, result.cookies)
                val path = result.redirectPath?.takeIf { it.startsWith("/") } ?: "/dashboard"
                startActivity(
                    Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                        putExtra(DashboardActivity.EXTRA_ORIGIN, origin)
                        putExtra(DashboardActivity.EXTRA_PATH, path)
                    },
                )
                finish()
            } else {
                val msg = when {
                    result.httpCode == 401 -> getString(R.string.error_auth)
                    result.httpCode < 0 -> getString(R.string.error_network)
                    !result.errorMessage.isNullOrBlank() -> result.errorMessage
                    else -> getString(R.string.error_generic)
                }
                showError(msg)
            }
        }
    }

    private fun showError(message: String) {
        binding.textError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    private fun setLoading(loading: Boolean) {
        binding.buttonLogin.isEnabled = !loading
        binding.inputPin.isEnabled = !loading
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonLogin.text =
            if (loading) getString(R.string.logging_in) else getString(R.string.login_button)
        if (loading) binding.textError.visibility = View.GONE
    }

    companion object {
        private const val SIGNUP_URL =
            "https://metricsphere.ai/setup-anfragen/" +
                "?utm_source=android_app" +
                "&utm_medium=app" +
                "&utm_campaign=login_signup" +
                "&utm_content=account_anfragen"
    }
}
