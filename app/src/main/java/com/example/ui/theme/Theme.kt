package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AmberGoldLight,
    onPrimary = CoffeeDark,
    primaryContainer = AmberGoldDark,
    onPrimaryContainer = Color(0xFFFFECCF),
    secondary = ForestGreenLight,
    onSecondary = Color.White,
    secondaryContainer = ForestGreen,
    onSecondaryContainer = Color(0xFFD4EED8),
    tertiary = TerraCotta,
    onTertiary = Color.White,
    background = CoffeeDark,
    onBackground = Color(0xFFF3ECE6),
    surface = CoffeeSurfaceDark,
    onSurface = Color(0xFFF3ECE6),
    surfaceVariant = CoffeeSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD7C9BF),
    outline = Color(0xFF7A6B60)
)

private val LightColorScheme = lightColorScheme(
    primary = AmberGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEFD6),
    onPrimaryContainer = Color(0xFF593800),
    secondary = ForestGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7EED9),
    onSecondaryContainer = Color(0xFF07380E),
    tertiary = TerraCotta,
    onTertiary = Color.White,
    background = WarmCream,
    onBackground = WarmTextPrimary,
    surface = WarmSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmTextSecondary,
    outline = Color(0xFFC0B2A4)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
