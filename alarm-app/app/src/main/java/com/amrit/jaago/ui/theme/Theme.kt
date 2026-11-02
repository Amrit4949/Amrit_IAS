package com.amrit.jaago.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- surfaces -------------------------------------------------------------

val Ink = Color(0xFF07070C)          // page
val Slate = Color(0xFF11131D)        // card
val Slate2 = Color(0xFF191C28)       // raised, inputs
val Hairline = Color(0xFF262A3A)     // 1dp borders

val TextHi = Color(0xFFF3F4F9)
val TextMid = Color(0xFFA6ABC0)
val TextLo = Color(0xFF6A7085)

// --- accents: one per thing you can do ------------------------------------

val Alarm1 = Color(0xFFFF4D6D)
val Alarm2 = Color(0xFFFF8A5B)

val Scan1 = Color(0xFF35E0F0)
val Scan2 = Color(0xFF2B96FF)

val Maths1 = Color(0xFFFFC247)
val Maths2 = Color(0xFFFF8A3D)

val Type1 = Color(0xFFA78BFA)
val Type2 = Color(0xFF7C5CFF)

val Good = Color(0xFF3DDC97)
val Bad = Color(0xFFFF5F6D)

// Kept for the older call sites that referred to these by name.
val Danger = Bad
val Warn = Maths1
val Cool = Scan1

fun accentGradient(a: Color, b: Color): Brush = Brush.linearGradient(listOf(a, b))

/** A dark wash of an accent, for the fill behind a selected card. */
fun accentWash(a: Color): Brush = Brush.verticalGradient(
    listOf(a.copy(alpha = 0.16f), a.copy(alpha = 0.04f))
)

private val Scheme = darkColorScheme(
    primary = Alarm1,
    onPrimary = Color(0xFF2A0008),
    primaryContainer = Color(0xFF3D0C18),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = Scan1,
    onSecondary = Color(0xFF00212B),
    tertiary = Maths1,
    onTertiary = Color(0xFF2A1D00),
    background = Ink,
    onBackground = TextHi,
    surface = Slate,
    onSurface = TextHi,
    surfaceVariant = Slate2,
    onSurfaceVariant = TextMid,
    outline = Hairline,
    error = Bad,
    onError = Color(0xFF2A0000)
)

private val JaagoType = Typography(
    displayLarge = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Normal, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
)

@Composable
fun JaagoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = JaagoType, content = content)
}
