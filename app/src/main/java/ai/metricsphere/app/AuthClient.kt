package ai.metricsphere.app

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LoginResult(
    val ok: Boolean,
    val origin: String? = null,
    val redirectPath: String? = null,
    val cookies: List<String> = emptyList(),
    val errorMessage: String? = null,
    val httpCode: Int = 0,
)

object AuthClient {
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    /** PIN → Admin redeem → instance complete → session cookie (1 year). */
    fun pairWithPin(
        pinRaw: String,
        deviceName: String = "",
        clientDeviceId: String = "",
    ): LoginResult {
        val pin = pinRaw.filter { it.isDigit() }
        if (pin.length != 6) {
            return LoginResult(ok = false, errorMessage = "PIN muss 6 Ziffern haben.", httpCode = 400)
        }

        val redeemBody = JSONObject().put("pin", pin).toString().toRequestBody(jsonType)
        val redeemReq = Request.Builder()
            .url(HostConfig.ADMIN_REDEEM_URL)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post(redeemBody)
            .build()

        val (origin, grant) = try {
            client.newCall(redeemReq).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(raw) }.getOrNull()
                if (!response.isSuccessful || json?.optBoolean("ok") != true) {
                    val msg = json?.optString("error")?.takeIf { it.isNotBlank() }
                        ?: "PIN ungültig oder abgelaufen."
                    return LoginResult(ok = false, errorMessage = msg, httpCode = response.code)
                }
                val o = HostConfig.normalizeOrigin(json.optString("origin", ""))
                    ?: return LoginResult(
                        ok = false,
                        errorMessage = "Ungültige Instanz-URL vom Server.",
                        httpCode = response.code,
                    )
                val g = json.optString("grant", "").trim()
                if (g.length < 16) {
                    return LoginResult(
                        ok = false,
                        errorMessage = "Ungültiger Kopplungs-Code.",
                        httpCode = response.code,
                    )
                }
                o to g
            }
        } catch (e: Exception) {
            return LoginResult(ok = false, errorMessage = e.message, httpCode = -1)
        }

        val completeJson = JSONObject().put("grant", grant)
        val trimmedName = deviceName.trim()
        if (trimmedName.isNotEmpty()) {
            completeJson.put("device_name", trimmedName.take(80))
        }
        val trimmedClientId = clientDeviceId.trim()
        if (trimmedClientId.isNotEmpty()) {
            completeJson.put("client_device_id", trimmedClientId.take(64))
        }
        val completeBody = completeJson.toString().toRequestBody(jsonType)
        val completeReq = Request.Builder()
            .url("$origin/api/device-pair/complete")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post(completeBody)
            .build()

        return try {
            client.newCall(completeReq).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                val cookies = response.headers("Set-Cookie")
                val json = runCatching { JSONObject(raw) }.getOrNull()
                if (response.isSuccessful && json?.optBoolean("ok") == true) {
                    LoginResult(
                        ok = true,
                        origin = origin,
                        redirectPath = json.optString("redirect", "/dashboard"),
                        cookies = cookies,
                        httpCode = response.code,
                    )
                } else {
                    val msg = json?.optString("error")?.takeIf { it.isNotBlank() }
                    LoginResult(
                        ok = false,
                        origin = origin,
                        errorMessage = msg,
                        httpCode = response.code,
                        cookies = cookies,
                    )
                }
            }
        } catch (e: Exception) {
            LoginResult(ok = false, origin = origin, errorMessage = e.message, httpCode = -1)
        }
    }
}
