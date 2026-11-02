package com.amrit.jaago.alarm

import com.amrit.jaago.model.SoundProfile
import kotlin.math.PI

/**
 * Generates the alarm tone one sample at a time instead of shipping an audio file.
 *
 * Why it is loud and horrible, on purpose:
 *  - square and saw waves carry far more energy than a sine at the same peak level,
 *    so they sound much louder at the same volume setting;
 *  - the pitches sit between 1 kHz and 4 kHz, the band the human ear is most
 *    sensitive to;
 *  - SCREECH puts two tones 57 Hz apart, which the ear hears as a rough beating
 *    rather than a note, plus a tritone on top. It is physically hard to ignore;
 *  - every profile keeps moving, so the brain cannot filter it out as background.
 *
 * Phase is accumulated per sample, so frequency sweeps stay clean with no clicks.
 */
class ToneSynth(private val profile: SoundProfile) {

    private var sampleIndex: Long = 0
    private var phase1 = 0.0
    private var phase2 = 0.0
    private var phase3 = 0.0

    /** Fills [out] with [count] mono 16 bit samples. */
    fun render(out: ShortArray, count: Int) {
        for (i in 0 until count) {
            val t = sampleIndex.toDouble() / SAMPLE_RATE
            val value = when (profile) {
                SoundProfile.SCREECH -> screech(t)
                SoundProfile.SIREN -> siren(t)
                SoundProfile.STAB -> stab(t)
                SoundProfile.KLAXON -> klaxon(t)
                SoundProfile.AIRHORN -> airhorn(t)
                SoundProfile.SYSTEM -> screech(t) // never used, the player picks MediaPlayer
            }
            val shaped = clip(value) * rampGain(t) * PEAK
            out[i] = shaped.toInt().toShort()
            sampleIndex++
        }
    }

    /**
     * Comes up from a third of full level to full level over [RAMP_SECONDS].
     * Long enough not to damage a cheap phone speaker or your ears, short enough
     * that you are awake before it finishes.
     */
    private fun rampGain(t: Double): Double =
        if (t >= RAMP_SECONDS) 1.0 else 0.34 + 0.66 * (t / RAMP_SECONDS)

    // --- profiles -------------------------------------------------------------

    /** Two tones 57 Hz apart beating against each other, with a tritone on top. */
    private fun screech(t: Double): Double {
        advance(1000.0, 1057.0, 1414.0)
        val gate = if ((t % 1.7) > 1.52) 0.0 else 1.0 // short stutter so it never becomes a drone
        return gate * (0.52 * square(phase1) + 0.52 * square(phase2) + 0.24 * square(phase3))
    }

    /** Square wave sweeping 600 Hz up to 1900 Hz and back, roughly 1.4 s a cycle. */
    private fun siren(t: Double): Double {
        val u = (t / 1.4) % 1.0
        val triangle = if (u < 0.5) u * 2.0 else (1.0 - u) * 2.0
        val f = 600.0 + 1300.0 * triangle
        advance(f, f * 1.5, f * 2.0)
        return 0.78 * square(phase1) + 0.3 * square(phase2) + 0.14 * square(phase3)
    }

    /** 3 kHz pulses, twelve a second. The most attention grabbing of the set. */
    private fun stab(t: Double): Double {
        advance(3000.0, 1500.0, 4200.0)
        val env = if (((t * 12.0) % 1.0) < 0.45) 1.0 else 0.0
        return env * (0.62 * square(phase1) + 0.38 * square(phase2) + 0.18 * square(phase3))
    }

    /** Hard two note honk. */
    private fun klaxon(t: Double): Double {
        val high = ((t % 0.36) >= 0.18)
        val f = if (high) 660.0 else 440.0
        advance(f, f * 2.0, f * 3.0)
        return 0.6 * square(phase1) + 0.3 * square(phase2) + 0.2 * square(phase3)
    }

    /** Three detuned saws pushed into clipping. */
    private fun airhorn(t: Double): Double {
        val wobble = 1.0 + 0.012 * kotlin.math.sin(2.0 * PI * 5.5 * t)
        advance(238.0 * wobble, 301.0 * wobble, 359.0 * wobble)
        val raw = 0.5 * saw(phase1) + 0.38 * saw(phase2) + 0.32 * saw(phase3)
        return raw * 2.3 // overdriven, then hard clipped
    }

    // --- helpers --------------------------------------------------------------

    private fun advance(f1: Double, f2: Double, f3: Double) {
        phase1 = wrap(phase1 + TWO_PI * f1 / SAMPLE_RATE)
        phase2 = wrap(phase2 + TWO_PI * f2 / SAMPLE_RATE)
        phase3 = wrap(phase3 + TWO_PI * f3 / SAMPLE_RATE)
    }

    private fun wrap(p: Double): Double = if (p >= TWO_PI) p - TWO_PI else p

    private fun square(phase: Double): Double = if (phase < PI) 1.0 else -1.0

    private fun saw(phase: Double): Double = phase / PI - 1.0

    private fun clip(v: Double): Double = when {
        v > 1.0 -> 1.0
        v < -1.0 -> -1.0
        else -> v
    }

    companion object {
        const val SAMPLE_RATE = 44100
        private const val TWO_PI = 2.0 * PI
        private const val PEAK = 30000.0 // a little under Short.MAX_VALUE to avoid wrapping
        private const val RAMP_SECONDS = 1.3
    }
}
