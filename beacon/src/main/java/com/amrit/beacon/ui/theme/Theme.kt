package com.amrit.beacon.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * A warm amber accent on near-black. Amber rather than the obvious red: red is reserved
 * here for the live alert screen, so it means exactly one thing when it appears.
 */
private val Amber = Color(0xFFFFB300)
private val AmberDark = Color(0xFFB37800)
private val Ink = Color(0xFF101014)
private val Surface = Color(0xFF1A1A20)
private val AlertRed = Color(0xFFD32F2F)

private val DarkScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF241A00),
    primaryContainer = AmberDark,
    onPrimaryContainer = Color(0xFFFFF3D6),
    secondary = Color(0xFF8FB6FF),
    background = Ink,
    onBackground = Color(0xFFE6E6EB),
    surface = Surface,
    onSurface = Color(0xFFE6E6EB),
    surfaceVariant = Color(0xFF2A2A33),
    onSurfaceVariant = Color(0xFFB9B9C4),
    error = AlertRed,
)

private val LightScheme = lightColorScheme(
    primary = AmberDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0A3),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF2C5AA8),
    background = Color(0xFFFBFBFE),
    onBackground = Color(0xFF1A1A20),
    surface = Color.White,
    onSurface = Color(0xFF1A1A20),
    error = AlertRed,
)

@Composable
fun BeaconTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Material You, where the platform offers it. */
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
