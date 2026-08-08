package com.amrit.beacon.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amrit.beacon.BeaconApp
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.alert.AlertProfile
import com.amrit.beacon.data.BeaconSettings
import com.amrit.beacon.net.AlertProfileWire
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.net.PairCode
import com.amrit.beacon.net.Peer
import com.amrit.beacon.net.Wire
import com.amrit.beacon.service.BeaconService
import com.amrit.beacon.setup.Readiness
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BeaconViewModel(app: Application) : AndroidViewModel(app) {

    private val container = BeaconApp.container(app)

    val settings: StateFlow<BeaconSettings?> = container.settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val peers: StateFlow<List<Peer>> = container.transport.peers

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
     * Rings a peer. The result reported to the user comes from the peer's *signed reply*,
     * not from the socket write succeeding — so "Ringing X" on screen means that phone
     * really did receive and authenticate the request.
     */
    fun ring(peer: Peer, profile: AlertProfile) {
        viewModelScope.launch {
            _sending.value = _sending.value + peer.deviceId
            try {
                when (val result = container.transport.send(peer, Wire.Verb.RING, profile.wireName)) {
                    is LanTransport.SendResult.Ok ->
                        _message.value = "Ringing ${result.peerName}"

                    is LanTransport.SendResult.Failed ->
                        _message.value = "Could not reach ${peer.deviceName}: ${result.reason}"
                }
            } finally {
                _sending.value = _sending.value - peer.deviceId
            }
        }
    }

    /** Tells a peer we already found the phone, so it can stop on its own. */
    fun stopPeer(peer: Peer) {
        viewModelScope.launch {
            container.transport.send(peer, Wire.Verb.STOP, AlertProfileWire.NONE)
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
