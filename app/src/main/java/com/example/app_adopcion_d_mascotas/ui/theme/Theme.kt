package com.example.app_adopcion_d_mascotas.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/*
 * ============================================================================
 * ARCHIVO: Theme.kt
 * CAPA: UI -> Tema Visual (RF16: Modo Claro, Modo Oscuro y Automático)
 * ¿QUÉ HACE?: Configura los colores de toda la aplicación y permite al usuario
 *             elegir entre Modo Automático (del celular), Modo Claro o Modo Oscuro.
 * ============================================================================
 */

// Modos visuales que el usuario puede elegir (RF16)
enum class ModoTemaVisual {
    AUTOMATICO, // Sigue la configuración del teléfono Android
    CLARO,      // Fuerza colores claros cálidos
    OSCURO      // Fuerza colores oscuros de alto contraste
}

object GestorTemaApp {
    private const val PREFS_TEMA = "preferencias_tema_adopta"
    private const val CLAVE_MODO = "modo_tema"

    var modoActual by mutableStateOf(ModoTemaVisual.AUTOMATICO)
        private set

    fun cargarPreferencia(contexto: Context) {
        val prefs = contexto.getSharedPreferences(PREFS_TEMA, Context.MODE_PRIVATE)
        val guardado = prefs.getString(CLAVE_MODO, ModoTemaVisual.AUTOMATICO.name) ?: ModoTemaVisual.AUTOMATICO.name
        modoActual = try {
            ModoTemaVisual.valueOf(guardado)
        } catch (_: Exception) {
            ModoTemaVisual.AUTOMATICO
        }
    }

    fun cambiarModo(contexto: Context, nuevoModo: ModoTemaVisual) {
        modoActual = nuevoModo
        contexto.getSharedPreferences(PREFS_TEMA, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE_MODO, nuevoModo.name)
            .apply()
    }
}

// Esquema de Colores Oficial - Modo Oscuro
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

// Esquema de Colores Oficial - Modo Claro
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

val AppColorScheme = DarkAppColorScheme

@Composable
fun AppAdopcionDMascotasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Determinar si se usa tema oscuro según la preferencia elegida por el usuario (RF16)
    val usarTemaOscuro = when (GestorTemaApp.modoActual) {
        ModoTemaVisual.AUTOMATICO -> darkTheme
        ModoTemaVisual.CLARO -> false
        ModoTemaVisual.OSCURO -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (usarTemaOscuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        usarTemaOscuro -> DarkAppColorScheme
        else -> LightAppColorScheme
    }

    // Configurar la apariencia de los iconos de la barra de estado según el tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !usarTemaOscuro
                insetsController.isAppearanceLightNavigationBars = !usarTemaOscuro
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
