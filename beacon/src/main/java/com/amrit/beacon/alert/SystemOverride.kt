package com.amrit.beacon.alert

import android.app.NotificationManager
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log

/**
 * Takes temporary control of the things that would otherwise keep the phone quiet, and — the
 * part that is easy to skip and unforgivable to get wrong — puts every one of them back.
 *
 * An app that leaves Do Not Disturb switched off, or alarm volume pinned at maximum, after
 * the user has found their phone is worse than one that never rang. [engage] and [release]
 * are symmetric and [release] is idempotent, so it is safe to call from a service teardown
 * path that may run more than once.
 */
class SystemOverride(private val context: Context) {

    private val audioManager: AudioManager? = context.getSystemService(AudioManager::class.java)
    private val notificationManager: NotificationManager? =
        context.getSystemService(NotificationManager::class.java)

    private var savedAlarmVolume: Int? = null
    private var savedInterruptionFilter: Int? = null
    private var focusRequest: AudioFocusRequest? = null

    /** True when the user has granted the DND override on the system settings screen. */
    val canOverrideDnd: Boolean
        get() = notificationManager?.isNotificationPolicyAccessGranted == true

    @Synchronized
    fun engage(profile: AlertProfile) {
        // Order is load-bearing. From API 23 onward, changing a stream volume while Do Not
        // Disturb is active throws SecurityException unless the app holds notification policy
        // access. Lifting DND first means the volume change below simply works.
        if (profile.overrideDnd) liftDoNotDisturb()
        if (profile.overrideVolume) maximiseAlarmVolume()
        requestAudioFocus()
    }

    @Synchronized
    fun release() {
        abandonAudioFocus()
        restoreAlarmVolume()
        restoreDoNotDisturb()
    }

    private fun liftDoNotDisturb() {
        val nm = notificationManager ?: return
        if (!nm.isNotificationPolicyAccessGranted) {
            Log.i(TAG, "no notification policy access; DND (if on) may still suppress the alert")
            return
        }
        try {
            val current = nm.currentInterruptionFilter
            if (current == NotificationManager.INTERRUPTION_FILTER_ALL) return
            if (savedInterruptionFilter == null) savedInterruptionFilter = current
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } catch (e: Exception) {
            Log.w(TAG, "could not lift Do Not Disturb", e)
        }
    }

    private fun restoreDoNotDisturb() {
        val nm = notificationManager ?: return
        val saved = savedInterruptionFilter ?: return
        savedInterruptionFilter = null
        if (!nm.isNotificationPolicyAccessGranted) return
        // UNKNOWN is what the platform reports when it will not tell us; writing it back would
        // be meaningless, so leave the user's current setting alone instead.
        if (saved == NotificationManager.INTERRUPTION_FILTER_UNKNOWN) return
        runCatching { nm.setInterruptionFilter(saved) }
            .onFailure { Log.w(TAG, "could not restore Do Not Disturb", it) }
    }

    private fun maximiseAlarmVolume() {
        val am = audioManager ?: return
        try {
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val current = am.getStreamVolume(AudioManager.STREAM_ALARM)
            if (savedAlarmVolume == null) savedAlarmVolume = current
            if (current < max) {
                // Flag 0: no volume toast. The full-screen alert is the UI here.
                am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "denied permission to raise alarm volume", e)
        } catch (e: Exception) {
            Log.w(TAG, "could not raise alarm volume", e)
        }
    }

    private fun restoreAlarmVolume() {
        val am = audioManager ?: return
        val saved = savedAlarmVolume ?: return
        savedAlarmVolume = null
        runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, saved, 0) }
            .onFailure { Log.w(TAG, "could not restore alarm volume", it) }
    }

    /**
     * Asked for so music and video get out of the way. Focus *loss* is deliberately not
     * handled: an alarm that politely stops because something else wanted the speaker is an
     * alarm that failed. The platform expects alarm-usage streams to behave this way.
     */
    private fun requestAudioFocus() {
        val am = audioManager ?: return
        if (focusRequest != null) return
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(SirenPlayer.alarmAttributes())
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener { change ->
                Log.d(TAG, "audio focus changed to $change; alert continues regardless")
            }
            .build()
        focusRequest = request
        runCatching { am.requestAudioFocus(request) }
            .onFailure { Log.w(TAG, "audio focus request failed", it) }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        val request = focusRequest ?: return
        focusRequest = null
        runCatching { am.abandonAudioFocusRequest(request) }
    }

    companion object {
        private const val TAG = "BeaconOverride"
    }
}
