package com.amrit.beacon.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amrit.beacon.BeaconApp
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.alert.AlertProfile
import com.amrit.beacon.data.BeaconSettings
import com.amrit.beacon.net.AlertProfileWire
import com.amrit.beacon.net.CloudTransport
import com.amrit.beacon.net.DirectoryEntry
import com.amrit.beacon.net.Identity
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.net.PairCode
import com.amrit.beacon.net.Peer
import com.amrit.beacon.net.PeerDirectory
import com.amrit.beacon.net.RelayClient
import com.amrit.beacon.net.Wire
import com.amrit.beacon.service.BeaconService
import com.amrit.beacon.setup.Readiness
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BeaconViewModel(app: Application) : AndroidViewModel(app) {

    private val container = BeaconApp.container(app)

    val settings: StateFlow<BeaconSettings?> = container.settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val peers: StateFlow<List<Peer>> = container.transport.peers

    /**
     * The single list the UI renders: local and remote phones merged, so the user never sees
     * the same handset twice and never has to know which route will be used.
     */
    val directory: StateFlow<List<DirectoryEntry>> =
        combine(container.transport.peers, container.cloudTransport.peers, PeerDirectory::merge)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** True once this phone's own push registration is live, i.e. it can be rung from afar. */
    val reachableRemotely: StateFlow<Boolean> = container.cloudTransport.registered

    /** False on the `lan` build, which links no push support at all. */
    val canReceiveRemotely: Boolean = container.cloudTransport.canReceive

    val transportStatus: StateFlow<LanTransport.Status> = container.transport.status

    val alertState: StateFlow<AlertEngine.State> = container.alertEngine.state

    private val _readiness = MutableStateFlow(Readiness.inspect(app))
    val readiness: StateFlow<Readiness> = _readiness.asStateFlow()

    /** One-shot user-facing messages; cleared once shown. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Device ids currently mid-send, so their row can show a spinner. */
    private val _sending = MutableStateFlow<Set<String>>(emptySet())
    val sending: StateFlow<Set<String>> = _sending.asStateFlow()

    init {
        viewModelScope.launch { container.settingsStore.ensureIdentity() }
    }

    /** Re-read permission state; the user may have changed it in Settings and come back. */
    fun refreshReadiness() {
        _readiness.value = Readiness.inspect(getApplication<Application>())
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun createCircle(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val code = PairCode.generate()
            container.settingsStore.setPairCode(code)
            startService()
            onCreated(PairCode.format(code))
        }
    }

    fun joinCircle(typed: String): Boolean {
        if (!PairCode.isComplete(typed)) return false
        viewModelScope.launch {
            container.settingsStore.setPairCode(typed)
            startService()
        }
        return true
    }

    fun leaveCircle() {
        viewModelScope.launch {
            container.settingsStore.leaveCircle()
            BeaconService.stop(getApplication<Application>())
        }
    }

    fun setDeviceName(name: String) {
        viewModelScope.launch { container.settingsStore.setDeviceName(name) }
    }

    fun setDefaultProfile(profile: AlertProfile) {
        viewModelScope.launch { container.settingsStore.setDefaultProfile(profile) }
    }

    /**
     * Rings a phone over whichever route is available, preferring the local network.
     *
     * The wording of the result is not cosmetic. Over the LAN the peer signs a reply, so
     * "Ringing X" is a claim about that handset. Through the relay there is no end-to-end
     * acknowledgement — success means the relay accepted it for delivery — and the message
     * says "Sent to" instead. Overstating the weaker guarantee would be the one thing you
     * cannot afford in an app people use to find a lost phone.
     */
    fun ring(entry: DirectoryEntry, profile: AlertProfile) {
        viewModelScope.launch {
            _sending.value = _sending.value + entry.deviceId
            try {
                val lanPeer = entry.lan
                _message.value = if (lanPeer != null) {
                    when (val result = container.transport.send(lanPeer, Wire.Verb.RING, profile.wireName)) {
                        is LanTransport.SendResult.Ok -> "Ringing ${result.peerName}"
                        is LanTransport.SendResult.Failed ->
                            "Could not reach ${entry.deviceName}: ${result.reason}"
                    }
                } else {
                    when (val result = sendViaRelay(entry.deviceId, Wire.Verb.RING, profile.wireName)) {
                        is CloudTransport.SendResult.Ok ->
                            if (result.deliveredTo > 0) {
                                "Sent to ${entry.deviceName}"
                            } else {
                                "${entry.deviceName} is not registered with the relay"
                            }

                        is CloudTransport.SendResult.Failed ->
                            "Could not reach ${entry.deviceName}: ${result.reason}"
                    }
                }
            } finally {
                _sending.value = _sending.value - entry.deviceId
            }
        }
    }

    /** Tells a phone we already found it, so it can stop on its own. */
    fun stopPeer(entry: DirectoryEntry) {
        viewModelScope.launch {
            val lanPeer = entry.lan
            if (lanPeer != null) {
                container.transport.send(lanPeer, Wire.Verb.STOP, AlertProfileWire.NONE)
            } else {
                sendViaRelay(entry.deviceId, Wire.Verb.STOP, AlertProfileWire.NONE)
            }
        }
    }

    private suspend fun sendViaRelay(
        target: String,
        verb: Wire.Verb,
        profile: String,
    ): CloudTransport.SendResult {
        val settings = container.settingsStore.current()
        if (!settings.hasRelay) {
            return CloudTransport.SendResult.Failed("no relay set up for distant phones")
        }
        val key = container.circleKey()
            ?: return CloudTransport.SendResult.Failed("not in a circle")
        return container.cloudTransport.ring(
            relayUrl = settings.relayUrl,
            circleKey = key,
            identity = Identity(settings.deviceId, settings.deviceName),
            target = target,
            verb = verb,
            profile = profile,
        )
    }

    /** Rings every other phone in the circle at once, for when you have no idea where it is. */
    fun ringEverything(profile: AlertProfile) {
        viewModelScope.launch {
            _message.value = when (
                val result = sendViaRelay(RelayClient.ALL, Wire.Verb.RING, profile.wireName)
            ) {
                is CloudTransport.SendResult.Ok -> when (result.deliveredTo) {
                    0 -> "No phones are registered with the relay yet"
                    1 -> "Sent to 1 phone"
                    else -> "Sent to ${result.deliveredTo} phones"
                }

                is CloudTransport.SendResult.Failed -> "Could not send: ${result.reason}"
            }
        }
    }

    fun setRelayUrl(url: String) {
        viewModelScope.launch {
            val outcome = runCatching { container.settingsStore.setRelayUrl(url) }
            _message.value = outcome.fold(
                onSuccess = {
                    BeaconService.syncCloud(getApplication<Application>())
                    if (url.isBlank()) "Relay cleared" else "Relay saved"
                },
                onFailure = { it.message ?: "That relay address was not accepted" },
            )
        }
    }

    fun testAlert(profile: AlertProfile) {
        BeaconService.testAlert(getApplication<Application>(), profile)
    }

    fun stopLocalAlert() {
        container.alertEngine.stop(AlertEngine.StopReason.USER)
    }

    fun startService() {
        BeaconService.start(getApplication<Application>())
    }
}
