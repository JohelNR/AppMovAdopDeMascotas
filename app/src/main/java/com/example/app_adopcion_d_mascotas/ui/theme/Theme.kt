package com.example.app_adopcion_d_mascotas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de Colores Oficial para Adopta Huellitas
private val LightColorScheme = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    primaryContainer = WarmOrangeContainer,
    onPrimaryContainer = OnWarmOrangeContainer,
    secondary = SoftTeal,
    onSecondary = Color.White,
    secondaryContainer = SoftTealContainer,
    onSecondaryContainer = OnSoftTealContainer,
    background = WarmBackground,
    onBackground = OnSurfaceText,
    surface = WarmSurface,
    onSurface = OnSurfaceText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantText,
    error = UrgentRed,
    errorContainer = UrgentRedContainer,
    onError = Color.White,
    onErrorContainer = UrgentRed
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmOrange,
    secondary = SoftTeal,
    background = Color(0xFF1E1A18),
    surface = Color(0xFF2B2522)
)

@Composable
fun AppAdopcionDMascotasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Lo dejamos en false para que SIEMPRE use nuestros colores hermosos y no el gris genérico de Android
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