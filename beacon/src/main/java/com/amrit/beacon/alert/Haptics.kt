package com.amrit.beacon.alert

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * The vibration half of the alert.
 *
 * Worth having even though the siren is the headline: a phone face-down on a mattress, or
 * inside a bag on a bus, is often felt before it is heard. The pattern is tagged
 * `USAGE_ALARM` for the same reason the audio is — alarm-usage vibration is not suppressed
 * by silent mode.
 */
class Haptics(context: Context) {

    private val vibrator: Vibrator? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator

        else -> context.getSystemService(Vibrator::class.java)
    }

    private var running = false

    val isAvailable: Boolean get() = vibrator?.hasVibrator() == true

    @Synchronized
    fun start() {
        val v = vibrator ?: return
        if (running || !v.hasVibrator()) return

        val effect = buildEffect(v)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrateAsAlarm33(v, effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(effect, ALARM_AUDIO_ATTRIBUTES)
            }
            running = true
        } catch (e: Exception) {
            Log.w(TAG, "could not start vibration", e)
        }
    }

    @Synchronized
    fun stop() {
        running = false
        runCatching { vibrator?.cancel() }
    }

    /**
     * Three sharp pulses then a pause, looping. An unbroken buzz is easy to mistake for a
     * notification; a rhythm that repeats reads as "something wants you now".
     */
    private fun buildEffect(v: Vibrator): VibrationEffect =
        if (v.hasAmplitudeControl()) {
            VibrationEffect.createWaveform(TIMINGS, AMPLITUDES, REPEAT_FROM_INDEX)
        } else {
            // No amplitude control: the same rhythm, expressed as plain on/off.
            VibrationEffect.createWaveform(TIMINGS, REPEAT_FROM_INDEX)
        }

    /**
     * Isolated so the reference to [VibrationAttributes] — which only exists from API 30, and
     * whose `createForUsage` factory only from 33 — sits behind an explicit API gate rather
     * than in a field the verifier would touch on older devices.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun vibrateAsAlarm33(v: Vibrator, effect: VibrationEffect) {
        v.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
    }

    companion object {
        private const val TAG = "BeaconHaptics"

        //                              off  on   off  on   off  on   off (long)
        private val TIMINGS = longArrayOf(0, 380, 140, 380, 140, 700, 520)
        private val AMPLITUDES = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        private const val REPEAT_FROM_INDEX = 0

        private val ALARM_AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }
}
