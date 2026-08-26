package ai.metricsphere.app

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("metricsphere", Context.MODE_PRIVATE)

    /** Paired instance origin, e.g. https://greyboard.metricsphere.ai */
    var origin: String
        get() = prefs.getString(KEY_ORIGIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ORIGIN, value).apply()

    /** Stable per-install id so re-pair replaces the same device entry. */
    val installationId: String
        get() {
            val existing = prefs.getString(KEY_INSTALLATION_ID, null)
            if (!existing.isNullOrBlank()) return existing
            val created = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_INSTALLATION_ID, created).apply()
            return created
        }

    fun clearPairing() {
        prefs.edit().remove(KEY_ORIGIN).apply()
    }

    companion object {
        private const val KEY_ORIGIN = "origin"
        private const val KEY_INSTALLATION_ID = "installation_id"
    }
}
