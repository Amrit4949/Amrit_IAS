package com.amrit.beacon.alert

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Generates the alert tone one buffer at a time, in pure Kotlin.
 *
 * Synthesising rather than shipping an mp3 is a deliberate choice. It costs nothing in APK
 * size, it can never be "the wrong sample rate for this device", and — the real reason — the
 * tone is tunable against how hearing actually works:
 *
 *  - The sweep lives between 1.5 kHz and 3.4 kHz. Equal-loudness contours put human hearing
 *    at its most sensitive right there, so a given amount of speaker energy is heard as
 *    louder than the same energy at, say, 500 Hz.
 *  - It sweeps rather than holding a pitch. A steady tone gets tuned out within seconds;
 *    a moving one keeps triggering the orienting response.
 *  - Harmonics are stacked on the fundamental. A pure sine wastes the tiny speaker's
 *    excursion on one frequency; a bright, buzzy timbre reads as much louder at the same
 *    peak amplitude.
 *  - The output is soft-saturated. This is a limiter, not distortion for its own sake: it
 *    lifts average energy toward the peak the speaker can produce, which is where perceived
 *    loudness comes from, without the crackle of hard clipping.
 *
 * The class holds phase across calls, so successive [fill] calls join seamlessly — audible
 * clicks at buffer boundaries would be the giveaway that this was done naively.
 */
class SirenSynth(
    private val sampleRate: Int = DEFAULT_SAMPLE_RATE,
    private val lowHz: Double = 1500.0,
    private val highHz: Double = 3400.0,
    private val sweepSeconds: Double = 0.42,
    /** Peak amplitude as a fraction of full scale. Left just below 1.0 to spare the DAC. */
    private val headroom: Double = 0.98,
) {

    private var phase = 0.0
    private var sweepPosition = 0.0
    private var samplesEmitted = 0L

    /** Rewinds to the start of a sweep, including the anti-pop attack ramp. */
    fun reset() {
        phase = 0.0
        sweepPosition = 0.0
        samplesEmitted = 0L
    }

    /**
     * Writes [count] mono 16-bit samples into [out] and returns how many were written.
     * Safe to call repeatedly forever; nothing accumulates unbounded.
     */
    fun fill(out: ShortArray, count: Int = out.size): Int {
        val n = min(count, out.size)
        val sweepStep = 1.0 / (sweepSeconds * sampleRate)
        val attackSamples = (ATTACK_SECONDS * sampleRate).toLong()

        for (i in 0 until n) {
            // Triangle sweep: up for the first half of the period, back down for the second.
            // Continuous in frequency, so there is no click where the direction turns.
            val tri = 1.0 - abs(2.0 * sweepPosition - 1.0)
            val frequency = lowHz + (highHz - lowHz) * tri

            phase += TWO_PI * frequency / sampleRate
            if (phase > TWO_PI) phase -= TWO_PI

            // Fundamental plus two harmonics: bright enough to cut through a pocket or a sofa.
            var sample = sin(phase) + 0.45 * sin(2 * phase) + 0.22 * sin(3 * phase)
            sample /= 1.67 // back to roughly unit peak before saturation

            // Gentle tremolo locked to the sweep, so the tone pulses instead of droning.
            val tremolo = 0.84 + 0.16 * cos(TWO_PI * sweepPosition)
            sample *= tremolo

            // Soft clip. tanh is smooth, so this adds density rather than crackle.
            sample = tanh(SATURATION * sample) / tanh(SATURATION)

            // One short fade-in on the very first buffer, purely to avoid a speaker pop.
            if (samplesEmitted < attackSamples) {
                sample *= samplesEmitted.toDouble() / attackSamples
            }

            out[i] = (sample * headroom * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()

            sweepPosition += sweepStep
            if (sweepPosition >= 1.0) sweepPosition -= 1.0
            samplesEmitted++
        }
        return n
    }

    companion object {
        const val DEFAULT_SAMPLE_RATE = 44_100
        private const val TWO_PI = 2.0 * PI
        private const val ATTACK_SECONDS = 0.02
        private const val SATURATION = 1.7
    }
}
