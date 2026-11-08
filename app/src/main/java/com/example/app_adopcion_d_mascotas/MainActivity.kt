package com.example.app_adopcion_d_mascotas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import com.example.app_adopcion_d_mascotas.navegacion.NavegacionApp
import com.example.app_adopcion_d_mascotas.notificaciones.AyudanteNotificaciones
import com.example.app_adopcion_d_mascotas.ui.theme.AppAdopcionDMascotasTheme
import com.example.app_adopcion_d_mascotas.ui.theme.GestorTemaApp

/*
 * ============================================================================
 * ARCHIVO: MainActivity.kt
 * ¿QUÉ HACE?: Es la puerta de entrada principal cuando el usuario toca el
 *             ícono de la aplicación en su teléfono Android.
 *             1. Conecta con el servidor Appwrite Cloud.
 *             2. Carga la preferencia de Modo Claro / Oscuro (RF16).
 *             3. Prepara el canal de notificaciones (RF14 y RF20).
 *             4. Arranca la navegación visual de las pantallas.
 * ============================================================================
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Paso 1: Inicializar la conexión con Appwrite Cloud
        ClienteAppwrite.inicializar(applicationContext)

        // Paso 2: Cargar la preferencia visual guardada (Modo Claro / Oscuro / Automático - RF16)
        GestorTemaApp.cargarPreferencia(applicationContext)

        // Paso 3: Crear el canal de notificaciones locales de Android (RF14 y RF20)
        AyudanteNotificaciones.crearCanalNotificaciones(applicationContext)

        // Paso 4: Pedir permiso de notificaciones en Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // Paso 5: Dibujar la aplicación con nuestro tema de colores y navegación
        setContent {
            AppAdopcionDMascotasTheme {
                NavegacionApp()
            }
        }
    }
}
