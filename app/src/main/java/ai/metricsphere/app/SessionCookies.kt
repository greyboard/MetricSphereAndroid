package ai.metricsphere.app

import android.webkit.CookieManager

object SessionCookies {
    fun applyToWebView(origin: String, setCookieHeaders: List<String>) {
        val cm = CookieManager.getInstance()
        cm.setAcceptCookie(true)
        for (header in setCookieHeaders) {
            cm.setCookie(origin, header)
        }
        cm.flush()
    }

    fun clearAll() {
        val cm = CookieManager.getInstance()
        cm.removeAllCookies(null)
        cm.flush()
    }
}
