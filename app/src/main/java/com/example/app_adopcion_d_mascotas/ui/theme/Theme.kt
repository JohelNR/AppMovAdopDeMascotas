package com.example.app_adopcion_d_mascotas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de Colores Oficial para Adopta Huellitas
// Un solo esquema oscuro para toda la app (fondo marrón oscuro cálido)
private val AppColorScheme = darkColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C2A00),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = SoftTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFB2DFDB),
    background = WarmBackground,
    onBackground = OnSurfaceText,
    surface = WarmSurface,
    onSurface = OnSurfaceText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantText,
    error = UrgentRed,
    errorContainer = Color(0xFF5C0A0A),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFB4B4)
)

@Composable
fun AppAdopcionDMascotasTheme(
    // Siempre usamos nuestro esquema oscuro/cálido para toda la app
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}