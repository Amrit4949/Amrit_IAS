package com.amrit.beacon.net

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to the relay: a small stateless server whose only job is to hold FCM tokens per
 * circle and fan a ring out to them. Reference implementation in [relay/](../../../../../../relay).
 *
 * The protocol is deliberately the same shape as the LAN one — plain text, one line per
 * record, no JSON parser on either side. That is not laziness: it means the payload the
 * relay forwards is byte-for-byte the same signed [Wire] line the LAN path sends, so both
 * transports converge on one verification routine on the receiving phone instead of two.
 *
 * Everything here assumes the relay is hostile-but-useful. It can refuse to forward, and it
 * learns which opaque circle id a push token belongs to. It cannot read device names, cannot
 * recover the pair code, and cannot mint a ring that any phone will act on.
 */
class RelayClient(private val baseUrl: String) {

    sealed interface Result<out T> {
        data class Ok<T>(val value: T) : Result<T>
        data class Failed(val reason: String) : Result<Nothing>
    }

    data class RemotePeer(val deviceId: String, val sealedName: String)

    /**
     * Announces this phone's push token under [circleId]. Called on first pair, on every FCM
     * token rotation, and periodically — the relay treats it as an upsert, so repeating it is
     * free and is what keeps a long-lived registration from expiring.
     */
    suspend fun register(
        circleId: String,
        deviceId: String,
        sealedName: String,
        pushToken: String,
    ): Result<Unit> {
        val body = "REGISTER $circleId $deviceId $sealedName $pushToken"
        return when (val response = post("register", body)) {
            is Result.Ok -> Result.Ok(Unit)
            is Result.Failed -> response
        }
    }

    /**
     * Pushes one signed wire line to [target], or to every other phone in the circle when
     * [target] is [ALL]. Returns how many devices the relay actually pushed to.
     */
    suspend fun ring(
        circleId: String,
        deviceId: String,
        target: String,
        wireLine: String,
    ): Result<Int> =
        when (val response = post("ring", "RING $circleId $deviceId $target\n${wireLine.trim()}")) {
            is Result.Ok -> {
                val count = response.value.firstOrNull()
                    ?.removePrefix("SENT ")
                    ?.trim()
                    ?.toIntOrNull()
                if (count == null) Result.Failed("unexpected relay reply") else Result.Ok(count)
            }

            is Result.Failed -> response
        }

    /** The other phones registered in this circle, names still sealed. */
    suspend fun peers(circleId: String, deviceId: String): Result<List<RemotePeer>> =
        when (val response = post("peers", "PEERS $circleId $deviceId")) {
            is Result.Ok -> Result.Ok(
                response.value.mapNotNull { line ->
                    val parts = line.trim().split(' ')
                    if (parts.size == 2 && parts[0].isNotEmpty()) {
                        RemotePeer(parts[0], parts[1])
                    } else {
                        null
                    }
                }
            )

            is Result.Failed -> response
        }

    private suspend fun post(path: String, body: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(baseUrl.trimEnd('/') + "/" + path)
                if (url.protocol != "https") {
                    // Tokens and circle ids must never cross the network in the clear.
                    return@withContext Result.Failed("relay URL must be https")
                }
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MILLIS
                    readTimeout = READ_TIMEOUT_MILLIS
                    doOutput = true
                    setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                }
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                if (code !in 200..299) {
                    return@withContext Result.Failed("relay returned HTTP $code")
                }
                val lines = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { reader -> reader.readLines().filter { it.isNotBlank() } }
                Result.Ok(lines)
            } catch (e: Exception) {
                Log.w(TAG, "relay $path failed", e)
                Result.Failed(e.message ?: "network error")
            } finally {
                connection?.disconnect()
            }
        }

    companion object {
        /** Fan out to every other phone in the circle. */
        const val ALL = "*"

        private const val TAG = "BeaconRelay"
        private const val CONNECT_TIMEOUT_MILLIS = 8_000
        private const val READ_TIMEOUT_MILLIS = 10_000
    }
}
