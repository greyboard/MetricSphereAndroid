package ai.metricsphere.app

import android.net.Uri

object HostConfig {
    const val ZONE = "metricsphere.ai"
    const val ADMIN_ORIGIN = "https://admin.metricsphere.ai"
    const val ADMIN_REDEEM_URL = "$ADMIN_ORIGIN/api/v1/device-pair/redeem"

    /** True if host is managed (*.metricsphere.ai) or the previously paired enterprise origin. */
    fun isAllowedHost(host: String?, pairedOrigin: String? = null): Boolean {
        if (host.isNullOrBlank()) return false
        val h = host.lowercase()
        if (h == ZONE || h.endsWith(".$ZONE")) return true
        val pairedHost = pairedOrigin?.let { Uri.parse(it).host?.lowercase() }
        return pairedHost != null && h == pairedHost
    }

    fun normalizeOrigin(raw: String): String? {
        return try {
            val u = Uri.parse(raw.trim())
            val scheme = u.scheme?.lowercase()
            val host = u.host?.lowercase()
            if (host.isNullOrBlank()) return null
            if (scheme != "https" && scheme != "http") return null
            "$scheme://$host"
        } catch (_: Exception) {
            null
        }
    }
}
