package com.example.app_adopcion_d_mascotas.navigation

import android.net.Uri

// Clase sellada que define todas las pantallas del flujo oficial de la app
sealed class Screen(val route: String) {

    // 1. Pantalla de Login y Registro (RF1)
    object Login : Screen("login")

    // 2. Pantalla principal del catálogo de mascotas con búsqueda y filtros (RF4, RF5, RF6)
    object Feed : Screen("feed")

    // 3. Pantalla de detalle de una mascota con carrusel de fotos, mapa y accesos (RF4, RF7)
    object Detail : Screen("detail/{petId}") {
        fun createRoute(petId: String) = "detail/$petId"
    }

    // 4. Pantalla para que el refugio cree o edite una mascota con fotos de galería (RF3)
    object Publish : Screen("publish?petId={petId}") {
        fun createRoute(petId: String? = null) =
            if (petId != null) "publish?petId=$petId" else "publish"
    }

    // 5. Formulario de Solicitud de Adopción para el adoptante (RF8)
    object AdoptionForm : Screen("adopt/{petId}") {
        fun createRoute(petId: String) = "adopt/$petId"
    }

    // 6. Panel de Solicitudes recibidas / enviadas (RF9, RF10)
    object Requests : Screen("requests")

    // 7. Coordinación y agenda de entrevistas / citas (RF20)
    object Appointments : Screen("appointments")

    // 8. Perfil de Usuario con foto de avatar y gestión (RF2)
    object Profile : Screen("profile")

    // 9. Chat en Tiempo Real Adoptante - Refugio (RF13)
    object Chat : Screen("chat/{chatId}?petName={petName}&receiverName={receiverName}") {
        fun createRoute(chatId: String, petName: String = "", receiverName: String = "Refugio"): String {
            val encodedPet = Uri.encode(petName)
            val encodedReceiver = Uri.encode(receiverName)
            return "chat/$chatId?petName=$encodedPet&receiverName=$encodedReceiver"
        }
    }

    // 10. Seguimiento Post-Adopción con fotos y reportes (RF11)
    object FollowUp : Screen("followup/{petId}?petName={petName}&shelterName={shelterName}") {
        fun createRoute(petId: String, petName: String = "", shelterName: String = "Refugio"): String {
            val encodedPet = Uri.encode(petName)
            val encodedShelter = Uri.encode(shelterName)
            return "followup/$petId?petName=$encodedPet&shelterName=$encodedShelter"
        }
    }
}
