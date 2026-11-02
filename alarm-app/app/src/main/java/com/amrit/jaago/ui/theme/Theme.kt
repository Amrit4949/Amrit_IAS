package com.amrit.jaago.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Danger = Color(0xFFFF4D4D)
val Good = Color(0xFF4ADE80)
val Cool = Color(0xFF4DD4FF)
val Warn = Color(0xFFFFD166)

private val JaagoColors = darkColorScheme(
    primary = Color(0xFFFF5A5A),
    onPrimary = Color(0xFF240000),
    primaryContainer = Color(0xFF4A0F12),
    onPrimaryContainer = Color(0xFFFFD9D9),
    secondary = Cool,
    onSecondary = Color(0xFF00212B),
    tertiary = Warn,
    onTertiary = Color(0xFF2A1D00),
    background = Color(0xFF0B0B10),
    onBackground = Color(0xFFECEDF2),
    surface = Color(0xFF14141C),
    onSurface = Color(0xFFECEDF2),
    surfaceVariant = Color(0xFF232330),
    onSurfaceVariant = Color(0xFFC3C6D2),
    outline = Color(0xFF50556A),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0000)
)

@Composable
fun JaagoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = JaagoColors, content = content)
}
