package io.github.soulhacler.headersup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PitchDark = Color(0xFF07110B)
val PitchSurface = Color(0xFF10211A)
val PitchSurfaceHigh = Color(0xFF173126)
val NeonGreen = Color(0xFF2BE36F)
val DeepGreen = Color(0xFF0B3D1E)
val PassAmber = Color(0xFFFFB020)
val Muted = Color(0xFF9DB4A6)
val Warning = Color(0xFFFF5A4E)

private val Colors = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    secondary = PassAmber,
    onSecondary = Color.Black,
    background = PitchDark,
    onBackground = Color.White,
    surface = PitchSurface,
    onSurface = Color.White,
    surfaceVariant = PitchSurfaceHigh,
    onSurfaceVariant = Muted,
    outline = Color(0xFF2E4A3B),
    error = Warning,
)

@Composable
fun HeadersUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
