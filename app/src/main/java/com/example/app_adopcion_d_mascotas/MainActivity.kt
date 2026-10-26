package com.example.app_adopcion_d_mascotas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import com.example.app_adopcion_d_mascotas.navigation.AppNavigation
import com.example.app_adopcion_d_mascotas.notifications.NotificationHelper
import com.example.app_adopcion_d_mascotas.ui.theme.AppAdopcionDMascotasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializamos el cliente oficial de Appwrite Cloud
        AppwriteClient.init(applicationContext)

        // Inicializar canal de notificaciones (RF14, RF20)
        NotificationHelper.createNotificationChannel(applicationContext)

        // Solicitar permiso de notificaciones en Android 13+
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

        setContent {
            AppAdopcionDMascotasTheme {
                // Aquí inicia todo el flujo de navegación de la app
                AppNavigation()
            }
        }
    }
}
