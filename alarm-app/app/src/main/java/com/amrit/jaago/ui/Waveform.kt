package com.amrit.jaago.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.amrit.jaago.alarm.ToneSynth
import com.amrit.jaago.model.SoundProfile
import kotlin.math.abs

/**
 * Measures one second of a profile and reduces it to a handful of peak values.
 * The bars you see are the real signal, so a gated profile shows gaps and a
 * sweeping one shows a solid block. Computed once per profile per process.
 */
object WaveformCache {

    private const val BUCKETS = 44
    private val cache = HashMap<String, FloatArray>()

    fun bars(profile: SoundProfile): FloatArray {
        cache[profile.id]?.let { return it }
        val result = measure(profile)
        cache[profile.id] = result
        return result
    }

    private fun measure(profile: SoundProfile): FloatArray {
        // The system ringtone is a file we cannot inspect, so show a neutral shape.
        if (profile == SoundProfile.SYSTEM) {
            return FloatArray(BUCKETS) { i ->
                0.35f + 0.3f * abs(kotlin.math.sin(i * 0.45)).toFloat()
            }
        }
        val synth = ToneSynth(profile)
        val perBucket = ToneSynth.SAMPLE_RATE / BUCKETS
        val chunk = ShortArray(perBucket)
        val out = FloatArray(BUCKETS)
        for (b in 0 until BUCKETS) {
            synth.render(chunk, perBucket)
            var peak = 0
            // Every 7th sample is plenty to find the peak of a bucket.
            var i = 0
            while (i < perBucket) {
                val v = abs(chunk[i].toInt())
                if (v > peak) peak = v
                i += 7
            }
            out[b] = (peak / 32767f).coerceIn(0f, 1f)
        }
        // The first bucket catches the volume ramp, so normalise against the loudest.
        val loudest = (out.maxOrNull() ?: 1f).coerceAtLeast(0.001f)
        for (b in out.indices) out[b] = (out[b] / loudest).coerceIn(0.06f, 1f)
        return out
    }
}

/**
 * Bar chart of the tone. When [playing] is true a highlight sweeps across it so
 * you can tell which card is making the noise.
 */
@Composable
fun WaveformBars(
    profile: SoundProfile,
    from: Color,
    to: Color,
    playing: Boolean,
    modifier: Modifier = Modifier
) {
    val bars = remember(profile) { WaveformCache.bars(profile) }
    val transition = rememberInfiniteTransition()
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart)
    )

    Canvas(modifier = modifier) {
        val count = bars.size
        val gap = size.width / (count * 5f)
        val barWidth = (size.width - gap * (count - 1)) / count
        val brush = Brush.verticalGradient(listOf(to, from))
        for (i in 0 until count) {
            val value = bars[i]
            val h = (size.height * value).coerceAtLeast(size.height * 0.05f)
            val x = i * (barWidth + gap)
            val near = if (playing) {
                val d = abs(i / (count - 1f) - sweep)
                (1f - (d * 5f)).coerceIn(0f, 1f)
            } else {
                0f
            }
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, (size.height - h) / 2f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2f),
                alpha = if (playing) (0.45f + 0.55f * near) else 0.85f
            )
        }
    }
}
