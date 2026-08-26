package ai.metricsphere.app

import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceName {
    /** User-visible device name (Settings), fallback to manufacturer + model. */
    fun resolve(context: Context): String {
        val fromSettings = runCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.trim().orEmpty()

        if (fromSettings.isNotEmpty()) return fromSettings.take(80)

        val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
        val model = Build.MODEL?.trim().orEmpty()
        val combined = when {
            manufacturer.isNotEmpty() && model.startsWith(manufacturer, ignoreCase = true) -> model
            manufacturer.isNotEmpty() && model.isNotEmpty() -> "$manufacturer $model"
            model.isNotEmpty() -> model
            else -> "Android-Gerät"
        }
        return combined.take(80)
    }
}
