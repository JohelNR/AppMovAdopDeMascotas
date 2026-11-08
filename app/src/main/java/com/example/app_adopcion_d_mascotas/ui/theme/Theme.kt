package com.example.app_adopcion_d_mascotas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de Colores Oficial para Adopta Huellitas - Modo Oscuro
val DarkAppColorScheme = darkColorScheme(
    primary = WarmOrangeLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF632800),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = SoftTealLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFB2DFDB),
    background = DarkWarmBackground,
    onBackground = DarkOnSurfaceText,
    surface = DarkWarmSurface,
    onSurface = DarkOnSurfaceText,
    surfaceVariant = DarkWarmSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariantText,
    outline = DarkOutline,
    outlineVariant = Color(0xFF4D3327),
    error = UrgentRedDark,
    errorContainer = UrgentRedContainerDark,
    onError = Color.White,
    onErrorContainer = Color(0xFFFFB4B4)
)

// Esquema de Colores Oficial para Adopta Huellitas - Modo Claro
val LightAppColorScheme = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    primaryContainer = WarmOrangeContainer,
    onPrimaryContainer = OnWarmOrangeContainer,
    secondary = SoftTeal,
    onSecondary = Color.White,
    secondaryContainer = SoftTealContainer,
    onSecondaryContainer = OnSoftTealContainer,
    background = LightWarmBackground,
    onBackground = LightOnSurfaceText,
    surface = LightWarmSurface,
    onSurface = LightOnSurfaceText,
    surfaceVariant = LightWarmSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariantText,
    outline = LightOutline,
    outlineVariant = Color(0xFFE8DDD5),
    error = UrgentRed,
    errorContainer = UrgentRedContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF5C0A0A)
)

// Alias para compatibilidad
val AppColorScheme = DarkAppColorScheme

@Composable
fun AppAdopcionDMascotasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkAppColorScheme
        else -> LightAppColorScheme
    }

    // Configurar la apariencia de los iconos de la barra de estado según el tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Íconos oscuros en barra de estado si el fondo es claro, y claros si el fondo es oscuro
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
