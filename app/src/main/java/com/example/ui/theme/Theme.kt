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

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberGoldLight,
    onPrimaryContainer = DeepMaroon,
    secondary = AmberGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECC7),
    onSecondaryContainer = Color(0xFF3E1F00),
    tertiary = ForestGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD4EED8),
    onTertiaryContainer = Color(0xFF09311E),
    background = SacredIvory,
    onBackground = TextPrimaryLight,
    surface = SurfaceWarm,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF3E7D7),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFD5C3B2),
    outlineVariant = Color(0xFFE9DCCE)
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronLight,
    onPrimary = DeepMaroon,
    primaryContainer = SaffronDark,
    onPrimaryContainer = AmberGoldLight,
    secondary = AmberGold,
    onSecondary = DeepMaroon,
    secondaryContainer = Color(0xFF4E2600),
    onSecondaryContainer = Color(0xFFFFECC7),
    tertiary = Color(0xFF73C09B),
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF1E4E38),
    onTertiaryContainer = Color(0xFFD4EED8),
    background = SacredIvoryDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceWarmDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF382A21),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF6B5446),
    outlineVariant = Color(0xFF493A30)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep serene Buddhist palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
