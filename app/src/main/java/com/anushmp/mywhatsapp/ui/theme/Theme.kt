package com.anushmp.mywhatsapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary             = Color(0xFF075E54),
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFDCF8C6),
    onPrimaryContainer  = Color(0xFF111111),
    background          = Color(0xFFECE5DD),
    onBackground        = Color(0xFF111111),
    surface             = Color.White,
    onSurface           = Color(0xFF111111),
    surfaceVariant      = Color(0xFFF0F2F1),
    onSurfaceVariant    = Color(0xFF111111),
    outline             = Color(0xFF8A9BA8),
)

private val DarkColorScheme = darkColorScheme(
    primary             = Color(0xFF1F2C34),
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF005C4B),
    onPrimaryContainer  = Color(0xFFE8E8E8),
    background          = Color(0xFF0B1117),
    onBackground        = Color(0xFFE8E8E8),
    surface             = Color(0xFF2A3942),
    onSurface           = Color(0xFFE8E8E8),
    surfaceVariant      = Color(0xFF1F2C34),
    onSurfaceVariant    = Color(0xFFE8E8E8),
    outline             = Color(0xFF8A9BA8),
)

@Composable
fun MyWhatsappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
