package com.example.app_adopcion_d_mascotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app_adopcion_d_mascotas.navigation.AppNavigation
import com.example.app_adopcion_d_mascotas.ui.theme.AppAdopcionDMascotasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppAdopcionDMascotasTheme {
                // Aquí inicia todo el flujo de navegación de la app
                AppNavigation()
            }
        }
    }
}