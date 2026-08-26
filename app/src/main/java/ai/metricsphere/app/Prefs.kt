package ai.metricsphere.app

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("metricsphere", Context.MODE_PRIVATE)

    /** Paired instance origin, e.g. https://greyboard.metricsphere.ai */
    var origin: String
        get() = prefs.getString(KEY_ORIGIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ORIGIN, value).apply()

    fun clearPairing() {
        prefs.edit().remove(KEY_ORIGIN).apply()
    }

    companion object {
        private const val KEY_ORIGIN = "origin"
    }
}
