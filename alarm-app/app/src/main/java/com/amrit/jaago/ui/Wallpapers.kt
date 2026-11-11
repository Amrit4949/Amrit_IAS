package com.amrit.jaago.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Ink
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.Type1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Backgrounds for the alarm screen and the home hero. Every scene is drawn by
 * the app itself, so there is nothing to download, nothing to store, and each
 * one moves. "auto" follows the hour; "photo:<file>" is an image you chose.
 */
object Wallpapers {

    const val AUTO = "auto"
    private const val PHOTO_PREFIX = "photo:"
    private const val PHOTO_DIR = "wallpapers"

    enum class Scene(val id: String, val title: String, val accent: Color, val blurb: String) {
        DAWN("dawn", "Dawn", Alarm1, "Sun lifting over a warm horizon"),
        MESH("mesh", "Mesh", Alarm2, "Soft colour fields drifting"),
        OCEAN("ocean", "Ocean", Scan1, "Layers of slow waves"),
        SAKURA("sakura", "Sakura", Color(0xFFFF8FB1), "Petals rising through dusk"),
        EMBER("ember", "Ember", Maths2, "A warm glow in the dark"),
        AURORA("aurora", "Aurora", Good, "Curtains of light over a night sky"),
        NEBULA("nebula", "Nebula", Type1, "Clouds of colour among stars"),
        MIDNIGHT("midnight", "Midnight", TextMid, "Plain and quiet")
    }

    val scenes: List<Scene> = Scene.values().toList()

    fun isPhoto(id: String): Boolean = id.startsWith(PHOTO_PREFIX)

    /** The scene the hour calls for: dawn tones early, cool by day, embers at night. */
    fun autoScene(hour: Int): Scene = when (hour) {
        in 4..7 -> Scene.DAWN
        in 8..11 -> Scene.MESH
        in 12..16 -> Scene.OCEAN
        in 17..19 -> Scene.SAKURA
        in 20..22 -> Scene.EMBER
        23, 0 -> Scene.AURORA
        else -> Scene.NEBULA
    }

    fun resolve(id: String, hour: Int = hourNow()): Scene =
        if (id == AUTO) autoScene(hour) else scenes.firstOrNull { it.id == id } ?: autoScene(hour)

    /** The colour that goes with a wallpaper, for tinting cards and buttons. */
    fun accentOf(id: String, hour: Int = hourNow()): Color =
        if (isPhoto(id)) Alarm1 else resolve(id, hour).accent

    fun titleOf(id: String, hour: Int = hourNow()): String = when {
        id == AUTO -> "Auto  ·  ${autoScene(hour).title} now"
        isPhoto(id) -> "Your photo"
        else -> resolve(id, hour).title
    }

    fun hourNow(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    // --- the user's own image ---------------------------------------------------

    fun photoFile(context: Context, id: String): File? {
        if (!isPhoto(id)) return null
        val f = File(File(context.filesDir, PHOTO_DIR), id.removePrefix(PHOTO_PREFIX))
        return if (f.exists()) f else null
    }

    /**
     * Copies a picked image into app storage, shrunk to at most [maxDim] on its
     * long side, so it loads fast and needs no lasting permission on the original.
     */
    suspend fun importPhoto(context: Context, uri: Uri, maxDim: Int = 1600): String? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDim) sample *= 2
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    ?: return@withContext null
                val dir = File(context.filesDir, PHOTO_DIR).apply { mkdirs() }
                // Keep only the newest photo; the old file is no longer referenced anywhere.
                dir.listFiles()?.forEach { it.delete() }
                val name = "w${System.currentTimeMillis()}.jpg"
                File(dir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
                bitmap.recycle()
                PHOTO_PREFIX + name
            } catch (t: Throwable) {
                null
            }
        }

    suspend fun loadPhoto(context: Context, id: String): ImageBitmap? = withContext(Dispatchers.IO) {
        val file = photoFile(context, id) ?: return@withContext null
        try {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } catch (t: Throwable) {
            null
        }
    }
}

// ---------------------------------------------------------------------------
// Drawing
// ---------------------------------------------------------------------------

/**
 * Fills its box with the wallpaper. [dim] lays a dark veil on top so text stays
 * readable; [animated] false freezes the scene, for thumbnails.
 */
@Composable
fun WallpaperView(
    id: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    dim: Float = 0f
) {
    val context = LocalContext.current
    val hour = remember { Wallpapers.hourNow() }

    Box(modifier = modifier.background(Ink)) {
        if (Wallpapers.isPhoto(id)) {
            var image by remember(id) { mutableStateOf<ImageBitmap?>(null) }
            LaunchedEffect(id) { image = Wallpapers.loadPhoto(context, id) }
            val loaded = image
            if (loaded != null) {
                Image(
                    bitmap = loaded,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SceneCanvas(Wallpapers.Scene.MIDNIGHT, animated = false, modifier = Modifier.fillMaxSize())
            }
        } else {
            SceneCanvas(Wallpapers.resolve(id, hour), animated = animated, modifier = Modifier.fillMaxSize())
        }
        if (dim > 0f) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim.coerceIn(0f, 0.9f))))
        }
    }
}

@Composable
private fun SceneCanvas(scene: Wallpapers.Scene, animated: Boolean, modifier: Modifier = Modifier) {
    val phase: Float = if (animated) {
        val transition = rememberInfiniteTransition()
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(16_000, easing = LinearEasing), RepeatMode.Restart)
        )
        p
    } else {
        0.3f
    }
    val stars = remember { starField(90) }
    Canvas(modifier = modifier) {
        when (scene) {
            Wallpapers.Scene.DAWN -> drawDawn(phase)
            Wallpapers.Scene.MESH -> drawMesh(phase)
            Wallpapers.Scene.OCEAN -> drawOcean(phase)
            Wallpapers.Scene.SAKURA -> drawSakura(phase)
            Wallpapers.Scene.EMBER -> drawEmber(phase)
            Wallpapers.Scene.AURORA -> drawAurora(phase, stars)
            Wallpapers.Scene.NEBULA -> drawNebula(phase, stars)
            Wallpapers.Scene.MIDNIGHT -> drawMidnight(stars)
        }
    }
}

private data class Star(val x: Float, val y: Float, val r: Float, val twinkle: Float)

private fun starField(count: Int): List<Star> {
    val rnd = Random(1947)
    return List(count) {
        Star(rnd.nextFloat(), rnd.nextFloat() * 0.75f, 0.6f + rnd.nextFloat() * 1.6f, rnd.nextFloat())
    }
}

private fun DrawScope.drawStars(stars: List<Star>, phase: Float, alpha: Float = 0.8f) {
    for (s in stars) {
        val tw = 0.55f + 0.45f * sin((phase * 2f + s.twinkle) * 2f * PI.toFloat())
        drawCircle(
            color = Color.White,
            radius = s.r * density,
            center = Offset(s.x * size.width, s.y * size.height),
            alpha = (alpha * tw).coerceIn(0f, 1f)
        )
    }
}

/** A sine-topped band from a baseline to the bottom edge. */
private fun DrawScope.wavePath(baseline: Float, amp: Float, cycles: Float, shift: Float): Path {
    val path = Path()
    path.moveTo(0f, size.height)
    val steps = 48
    for (i in 0..steps) {
        val x = size.width * i / steps
        val y = baseline + amp * sin((x / size.width) * cycles * 2f * PI.toFloat() + shift)
        path.lineTo(x, y)
    }
    path.lineTo(size.width, size.height)
    path.close()
    return path
}

private fun DrawScope.drawDawn(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF130F3A), Color(0xFF4A1F63), Color(0xFFB8445C), Color(0xFFFF7A4F), Color(0xFFFFC06B)),
            startY = 0f,
            endY = size.height
        )
    )
    val pulse = 0.9f + 0.1f * sin(phase * 2f * PI.toFloat())
    val sun = Offset(size.width * 0.5f, size.height * 0.66f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFE6A3).copy(alpha = 0.55f * pulse), Color.Transparent),
            center = sun,
            radius = size.width * 0.55f
        ),
        radius = size.width * 0.55f,
        center = sun
    )
    drawCircle(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFE08A), Color(0xFFFF6B57)),
            startY = sun.y - size.width * 0.17f,
            endY = sun.y + size.width * 0.17f
        ),
        radius = size.width * 0.17f,
        center = sun
    )
    // Haze bands low on the horizon.
    for (i in 0 until 3) {
        val y = size.height * (0.70f + 0.07f * i)
        drawRect(
            color = Color(0xFFFF9A6B),
            topLeft = Offset(0f, y),
            size = Size(size.width, size.height * 0.02f),
            alpha = 0.18f - 0.05f * i
        )
    }
}

private fun DrawScope.drawMesh(phase: Float) {
    drawRect(color = Color(0xFF0F1024))
    val t = phase * 2f * PI.toFloat()
    val blobs = listOf(
        Triple(Color(0xFFB08DFF), Offset(0.2f + 0.08f * sin(t), 0.25f + 0.06f * cos(t)), 0.65f),
        Triple(Color(0xFFFFB45A), Offset(0.85f + 0.06f * cos(t * 1.3f), 0.2f + 0.08f * sin(t * 1.1f)), 0.6f),
        Triple(Color(0xFF3DDC97), Offset(0.75f + 0.07f * sin(t * 0.8f), 0.8f + 0.05f * cos(t)), 0.62f),
        Triple(Color(0xFF3DE3F0), Offset(0.15f + 0.06f * cos(t * 0.9f), 0.85f + 0.07f * sin(t * 1.2f)), 0.58f)
    )
    for ((color, c, r) in blobs) {
        val centre = Offset(c.x * size.width, c.y * size.height)
        val radius = size.width * r
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.12f), Color.Transparent),
                center = centre,
                radius = radius
            ),
            radius = radius,
            center = centre
        )
    }
}

private fun DrawScope.drawOcean(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF041B33), Color(0xFF07395C), Color(0xFF0A5C7A)),
            startY = 0f,
            endY = size.height
        )
    )
    val t = phase * 2f * PI.toFloat()
    val layers = listOf(
        Triple(0.52f, Color(0xFF1CB8C9), 1.0f),
        Triple(0.62f, Color(0xFF14A0C8), -1.4f),
        Triple(0.72f, Color(0xFF0E7FB3), 2.1f),
        Triple(0.82f, Color(0xFF0B5F93), -2.6f)
    )
    layers.forEachIndexed { i, (base, color, speed) ->
        val path = wavePath(size.height * base, size.height * 0.03f, 1.6f + 0.4f * i, t * speed)
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.15f)),
                startY = size.height * base - size.height * 0.03f,
                endY = size.height
            )
        )
    }
    val moon = Offset(size.width * 0.78f, size.height * 0.16f)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), center = moon, radius = size.width * 0.3f),
        radius = size.width * 0.3f,
        center = moon
    )
    drawCircle(color = Color(0xFFEAF6FF), radius = size.width * 0.06f, center = moon)
}

private fun DrawScope.drawSakura(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF1E1030), Color(0xFF5A2454), Color(0xFFB54A7A), Color(0xFFFF9EB5)),
            startY = 0f,
            endY = size.height
        )
    )
    val rnd = Random(7)
    for (i in 0 until 26) {
        val x = rnd.nextFloat()
        val startY = rnd.nextFloat()
        val r = 3f + rnd.nextFloat() * 9f
        val speed = 0.4f + rnd.nextFloat() * 0.8f
        val y = ((startY - phase * speed) % 1f + 1f) % 1f
        val sway = 0.02f * sin((phase * 4f + i) * 2f * PI.toFloat())
        drawCircle(
            color = Color(0xFFFFD6E3),
            radius = r * density,
            center = Offset((x + sway) * size.width, y * size.height),
            alpha = 0.35f + 0.4f * (1f - y)
        )
    }
}

private fun DrawScope.drawEmber(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF07050A), Color(0xFF160909), Color(0xFF2A0E08)),
            startY = 0f,
            endY = size.height
        )
    )
    val flicker = 0.85f + 0.15f * sin(phase * 2f * PI.toFloat() * 3f)
    val core = Offset(size.width * 0.5f, size.height * 1.05f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFB15C).copy(alpha = 0.75f * flicker), Color(0xFFFF6A2B).copy(alpha = 0.35f), Color.Transparent),
            center = core,
            radius = size.width * 0.95f
        ),
        radius = size.width * 0.95f,
        center = core
    )
    val rnd = Random(21)
    for (i in 0 until 18) {
        val x = rnd.nextFloat()
        val startY = rnd.nextFloat()
        val y = ((startY - phase * (0.5f + rnd.nextFloat())) % 1f + 1f) % 1f
        drawCircle(
            color = Color(0xFFFFC27A),
            radius = (1.2f + rnd.nextFloat() * 1.6f) * density,
            center = Offset(x * size.width, y * size.height),
            alpha = (0.6f * y).coerceIn(0f, 0.6f)
        )
    }
}

private fun DrawScope.drawAurora(phase: Float, stars: List<Star>) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF03081A), Color(0xFF07163A), Color(0xFF0A2548)),
            startY = 0f,
            endY = size.height
        )
    )
    drawStars(stars, phase)
    val t = phase * 2f * PI.toFloat()
    val curtains = listOf(
        Triple(0.30f, Color(0xFF3DDC97), 1.0f),
        Triple(0.40f, Color(0xFF35C8E8), -0.7f),
        Triple(0.50f, Color(0xFF9B7CFF), 1.3f)
    )
    curtains.forEachIndexed { i, (base, color, speed) ->
        val baseline = size.height * base
        val amp = size.height * (0.05f + 0.01f * i)
        val path = wavePath(baseline, amp, 1.2f + 0.3f * i, t * speed)
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.50f), color.copy(alpha = 0.10f), Color.Transparent),
                startY = baseline - amp,
                endY = baseline + size.height * 0.35f
            )
        )
    }
    // Dark ground at the bottom.
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF02050F)), startY = size.height * 0.72f, endY = size.height),
        topLeft = Offset(0f, size.height * 0.72f),
        size = Size(size.width, size.height * 0.28f)
    )
}

private fun DrawScope.drawNebula(phase: Float, stars: List<Star>) {
    drawRect(color = Color(0xFF090516))
    val t = phase * 2f * PI.toFloat()
    val clouds = listOf(
        Triple(Color(0xFFB23FFF), 0.0f, 0.55f),
        Triple(Color(0xFF7C5CFF), 2.1f, 0.5f),
        Triple(Color(0xFFFF5FA8), 4.2f, 0.45f),
        Triple(Color(0xFF2B96FF), 1.0f, 0.4f)
    )
    for ((color, offset, r) in clouds) {
        val a = t * 0.5f + offset
        val centre = Offset(size.width * (0.5f + 0.28f * cos(a)), size.height * (0.45f + 0.22f * sin(a)))
        val radius = size.width * r
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0.08f), Color.Transparent),
                center = centre,
                radius = radius
            ),
            radius = radius,
            center = centre
        )
    }
    drawStars(stars, phase, alpha = 0.9f)
}

private fun DrawScope.drawMidnight(stars: List<Star>) {
    drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF080A16), Color(0xFF10132A)), startY = 0f, endY = size.height)
    )
    drawStars(stars, 0.3f, alpha = 0.35f)
}
