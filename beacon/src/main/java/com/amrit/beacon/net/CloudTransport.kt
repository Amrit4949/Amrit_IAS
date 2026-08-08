package com.amrit.beacon.net

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reaching a phone that is not on your Wi-Fi.
 *
 * Everything the LAN transport gets for free — discovery, a socket, an immediate signed
 * reply — has to be replaced here, and the replacement is a push. A ring becomes: hand the
 * signed line to the relay, the relay pushes it to the circle's other tokens as a
 * high-priority FCM data message, and the receiving phone runs it through exactly the same
 * [Wire.decode] → [ReplayGuard] → alert path the socket listener uses.
 *
 * One consequence worth being honest about in the UI: there is no end-to-end acknowledgement.
 * Over the LAN, "Ringing X" means phone X authenticated the request. Here, success means the
 * relay accepted it for delivery — which is a weaker claim, and the wording says so.
 */
class CloudTransport(private val pushTokens: PushTokens) {

    data class CloudPeer(val deviceId: String, val deviceName: String)

    sealed interface SendResult {
        data class Ok(val deliveredTo: Int) : SendResult
        data class Failed(val reason: String) : SendResult
    }

    private val _peers = MutableStateFlow<List<CloudPeer>>(emptyList())
    val peers: StateFlow<List<CloudPeer>> = _peers.asStateFlow()

    private val _registered = MutableStateFlow(false)

    /** True once this phone's push token is lodged with the relay, i.e. it is reachable. */
    val registered: StateFlow<Boolean> = _registered.asStateFlow()

    /** False on builds without push support; see `PushTokens`. */
    val canReceive: Boolean get() = pushTokens.canReceive

    fun forget() {
        _peers.value = emptyList()
        _registered.value = false
    }

    /**
     * Registers this phone and refreshes the remote peer list.
     *
     * Registration is an upsert and is repeated on every pair change, token rotation and
     * service start. That repetition is deliberate: a stale token on the relay means a phone
     * that looks reachable and silently is not, which is the failure this app exists to avoid.
     */
    suspend fun sync(relayUrl: String, circleKey: ByteArray, identity: Identity) {
        val client = clientFor(relayUrl) ?: return
        val circleId = CircleId.forKey(circleKey)

        val token = pushTokens.current()
        if (token == null) {
            // Sending still works without a token; only being rung does not.
            Log.i(TAG, "no push token on this build; cloud receive is unavailable")
            _registered.value = false
        } else {
            when (
                val result = client.register(
                    circleId = circleId,
                    deviceId = identity.deviceId,
                    sealedName = CircleId.sealName(circleKey, identity.deviceName),
                    pushToken = token,
                )
            ) {
                is RelayClient.Result.Ok -> _registered.value = true
                is RelayClient.Result.Failed -> {
                    Log.w(TAG, "relay registration failed: ${result.reason}")
                    _registered.value = false
                }
            }
        }

        when (val result = client.peers(circleId, identity.deviceId)) {
            is RelayClient.Result.Ok -> {
                _peers.value = result.value.mapNotNull { remote ->
                    // A name that will not unseal means the entry was written by something
                    // that does not hold our key. Drop it rather than show a placeholder.
                    CircleId.openName(circleKey, remote.sealedName)
                        ?.let { CloudPeer(remote.deviceId, it) }
                }.sortedBy { it.deviceName.lowercase() }
            }

            is RelayClient.Result.Failed -> Log.w(TAG, "peer fetch failed: ${result.reason}")
        }
    }

    suspend fun ring(
        relayUrl: String,
        circleKey: ByteArray,
        identity: Identity,
        target: String,
        verb: Wire.Verb,
        profile: String,
    ): SendResult {
        val client = clientFor(relayUrl)
            ?: return SendResult.Failed("no relay configured")

        val line = Wire.Message(
            verb = verb,
            deviceId = identity.deviceId,
            deviceName = identity.deviceName,
            nonce = Crypto.newNonce(),
            sentAtMillis = System.currentTimeMillis(),
            profile = profile,
        ).encode(circleKey)

        return when (
            val result = client.ring(CircleId.forKey(circleKey), identity.deviceId, target, line)
        ) {
            is RelayClient.Result.Ok -> SendResult.Ok(result.value)
            is RelayClient.Result.Failed -> SendResult.Failed(result.reason)
        }
    }

    private fun clientFor(relayUrl: String): RelayClient? =
        relayUrl.trim().takeIf { it.isNotEmpty() }?.let(::RelayClient)

    private companion object {
        const val TAG = "BeaconCloud"
    }
}
