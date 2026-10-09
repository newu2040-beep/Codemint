package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = DarkBg,
    primaryContainer = Color(0xFF003825),
    onPrimaryContainer = MintLight,
    secondary = CyanAccent,
    onSecondary = DarkBg,
    secondaryContainer = Color(0xFF0C3852),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = IndigoAccent,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = DarkBorder,
    error = RedError,
    onError = Color.White
)

@Composable
fun CodeMintTheme(
    darkTheme: Boolean = true, // CodeMint IDE default is sleek dark mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
