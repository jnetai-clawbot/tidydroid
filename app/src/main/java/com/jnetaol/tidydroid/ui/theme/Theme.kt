package com.jnetaol.tidydroid.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TDPrimary,
    onPrimary = Color.Black,
    primaryContainer = TDPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = TDSecondary,
    onSecondary = Color.Black,
    tertiary = TDAccent,
    onTertiary = Color.White,
    background = TDBackground,
    onBackground = TDTextPrimary,
    surface = TDSurface,
    onSurface = TDTextPrimary,
    surfaceVariant = TDSurfaceVariant,
    onSurfaceVariant = TDTextSecondary,
    error = TDError,
    onError = Color.White,
    outline = TDTextMuted
)

@Composable
fun TidyDroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography(), content = content)
}
