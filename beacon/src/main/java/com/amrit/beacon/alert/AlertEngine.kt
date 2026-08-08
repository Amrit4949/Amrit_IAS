package com.amrit.beacon.alert

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The single place that knows an alert is running.
 *
 * Audio, vibration, torch and the system overrides are separate objects so each can be
 * reasoned about (and fail) on its own; this coordinates them and, critically, owns the
 * teardown. Every path out of an alert — the user tapping stop, the sender cancelling, the
 * safety timeout, the service being destroyed — funnels through [stop], so there is exactly
 * one place where "put the phone back how we found it" has to be correct.
 *
 * Start and stop are synchronized because they legitimately arrive from three different
 * threads: a socket reader, the main thread, and the timeout coroutine.
 */
class AlertEngine(private val appContext: Context) {

    enum class Trigger { REMOTE, LOCAL_TEST }

    enum class StopReason { USER, REMOTE, TIMEOUT, SHUTDOWN }

    data class State(
        val active: Boolean = false,
        val profile: AlertProfile = AlertProfile.DEFAULT,
        val trigger: Trigger = Trigger.LOCAL_TEST,
        /** Display name of the phone that asked for this, when it came from a peer. */
        val sourceName: String? = null,
        val startedAtElapsedMillis: Long = 0L,
    )

    private val siren = SirenPlayer(appContext)
    private val haptics = Haptics(appContext)
    private val torch = TorchStrobe(appContext)
    private val systemOverride = SystemOverride(appContext)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timeoutJob: Job? = null

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    val isActive: Boolean get() = _state.value.active

    /** Whether the DND override has been granted; surfaced to the setup screen. */
    val canOverrideDnd: Boolean get() = systemOverride.canOverrideDnd

    val hasTorch: Boolean get() = torch.isAvailable

    val hasVibrator: Boolean get() = haptics.isAvailable

    @Synchronized
    fun start(profile: AlertProfile, trigger: Trigger, sourceName: String? = null) {
        if (_state.value.active) {
            Log.d(TAG, "alert already running; ignoring duplicate start")
            return
        }
        Log.i(TAG, "starting ${profile.wireName} alert (trigger=$trigger, source=$sourceName)")

        // Overrides first: raising alarm volume after the track is already streaming would
        // produce an audible step, and lifting DND late can lose the first second entirely.
        systemOverride.engage(profile)
        siren.start(profile)
        if (profile.vibrate) haptics.start()
        if (profile.strobeTorch) torch.start()

        _state.value = State(
            active = true,
            profile = profile,
            trigger = trigger,
            sourceName = sourceName,
            startedAtElapsedMillis = android.os.SystemClock.elapsedRealtime(),
        )

        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(MAX_DURATION_MILLIS)
            Log.i(TAG, "safety timeout reached")
            stop(StopReason.TIMEOUT)
        }
    }

    @Synchronized
    fun stop(reason: StopReason) {
        timeoutJob?.cancel()
        timeoutJob = null
        if (!_state.value.active) return
        Log.i(TAG, "stopping alert ($reason)")

        // Reverse order of start, and every step independent: one throwing must not strand
        // the rest, or the phone is left with DND off and the volume pinned.
        runCatching { torch.stop() }.onFailure { Log.w(TAG, "torch teardown", it) }
        runCatching { haptics.stop() }.onFailure { Log.w(TAG, "haptics teardown", it) }
        runCatching { siren.stop() }.onFailure { Log.w(TAG, "siren teardown", it) }
        runCatching { systemOverride.release() }.onFailure { Log.w(TAG, "override teardown", it) }

        _state.value = State()
    }

    companion object {
        private const val TAG = "BeaconAlert"

        /**
         * Alerts stop themselves after three minutes. A phone that is genuinely lost is
         * better off with battery left to be found again than screaming until it dies.
         */
        const val MAX_DURATION_MILLIS = 3 * 60 * 1000L
    }
}
