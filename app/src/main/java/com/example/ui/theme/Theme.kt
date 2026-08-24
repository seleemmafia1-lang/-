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
    primary = Color(0xFF90CAF9),
    onPrimary = RaneenNavyDark,
    primaryContainer = RaneenNavy,
    onPrimaryContainer = Color(0xFFE2EAFC),
    secondary = RaneenGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF452B00),
    onSecondaryContainer = RaneenGoldLight,
    background = CanvasBgDark,
    onBackground = Color(0xFFF1F5F9),
    surface = CardBgDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = RaneenNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EAFC),
    onPrimaryContainer = RaneenNavyDark,
    secondary = RaneenGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = CanvasBgLight,
    onBackground = Color(0xFF1E293B),
    surface = CardBgLight,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

