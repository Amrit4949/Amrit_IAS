package com.amrit.beacon.alert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * The synthesiser is the one piece of this app that can be tested properly without a phone,
 * so it is worth testing properly. The failures these catch are all things you would
 * otherwise only discover by holding a ringing handset: clicks at buffer boundaries, a tone
 * that is quieter than it should be, or a first buffer that pops the speaker.
 */
class SirenSynthTest {

    private val sampleRate = SirenSynth.DEFAULT_SAMPLE_RATE

    @Test
    fun `fill reports how many samples it wrote`() {
        val synth = SirenSynth()
        val buffer = ShortArray(1024)
        assertEquals(1024, synth.fill(buffer))
        assertEquals(500, synth.fill(buffer, 500))
        // Never writes past the array, even if asked to.
        assertEquals(1024, synth.fill(buffer, 99_999))
    }

    @Test
    fun `output is loud`() {
        // A siren that peaks at half scale is a siren nobody hears from the next room.
        val synth = SirenSynth()
        val buffer = ShortArray(sampleRate) // one second, past the attack ramp
        synth.fill(buffer)
        val peak = buffer.maxOf { abs(it.toInt()) }
        assertTrue("peak was $peak", peak > Short.MAX_VALUE * 0.88)

        val rms = Math.sqrt(buffer.sumOf { it.toDouble() * it.toDouble() } / buffer.size)
        // Soft saturation should hold average energy well up toward the peak. A pure sine
        // would sit near 0.35 of full scale; this must do considerably better.
        assertTrue("rms was $rms", rms > Short.MAX_VALUE * 0.5)
    }

    @Test
    fun `starts from silence so the speaker does not pop`() {
        val synth = SirenSynth()
        val buffer = ShortArray(64)
        synth.fill(buffer)
        assertEquals(0, buffer[0].toInt())
        assertTrue("attack should ramp, not jump", abs(buffer[1].toInt()) < 2_000)
    }

    @Test
    fun `successive buffers join without a click`() {
        // The bug this catches: resetting phase every buffer. It sounds like a rhythmic tick
        // under the siren and is very easy to introduce and hard to spot by reading the code.
        val synth = SirenSynth()
        val warmup = ShortArray(sampleRate)
        synth.fill(warmup) // get past the attack ramp

        val first = ShortArray(2048)
        val second = ShortArray(2048)
        synth.fill(first)
        synth.fill(second)

        val withinBuffer = (1 until first.size)
            .maxOf { abs(first[it] - first[it - 1]) }
        val acrossBoundary = abs(second[0] - first[first.size - 1])
        assertTrue(
            "step across the boundary ($acrossBoundary) should look like any other step (<= $withinBuffer)",
            acrossBoundary <= withinBuffer,
        )
    }

    @Test
    fun `reset returns to the start of the sweep`() {
        val synth = SirenSynth()
        val first = ShortArray(256)
        synth.fill(first)

        synth.reset()
        val again = ShortArray(256)
        synth.fill(again)

        assertTrue("reset must reproduce the opening buffer", first.contentEquals(again))
    }

    @Test
    fun `runs indefinitely without drifting out of range`() {
        // Thirty seconds of audio. Phase accumulators that are never wrapped lose precision
        // and eventually produce garbage; this would catch that.
        val synth = SirenSynth()
        val buffer = ShortArray(4096)
        var peak = 0
        repeat(sampleRate * 30 / buffer.size) {
            synth.fill(buffer)
            peak = maxOf(peak, buffer.maxOf { s -> abs(s.toInt()) })
        }
        assertTrue("peak was $peak", peak in (Short.MAX_VALUE * 0.88).toInt()..Short.MAX_VALUE.toInt())
    }
}
