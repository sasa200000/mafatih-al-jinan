package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = EmeraldDark,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = GoldLight,
    secondary = TurquoiseNeon,
    onSecondary = SapphireNight,
    secondaryContainer = CardDark,
    onSecondaryContainer = TextSecondaryDark,
    tertiary = GoldWarm,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = CardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SapphireCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = TurquoiseDeep,
    onSecondary = Color.White,
    tertiary = GoldDark,
    background = SurfaceLight,
    onBackground = Color(0xFF14241F),
    surface = Color.White,
    onSurface = Color(0xFF14241F),
    surfaceVariant = Color(0xFFE8F5EE),
    onSurfaceVariant = Color(0xFF2C4940),
    outline = Color(0xFFC5DFD6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark for vibrant light show and spiritual night reading
    dynamicColor: Boolean = false, // Keep tailored Islamic gold & emerald palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
