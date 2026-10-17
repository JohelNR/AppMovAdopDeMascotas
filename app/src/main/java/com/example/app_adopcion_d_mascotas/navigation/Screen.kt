package com.example.app_adopcion_d_mascotas.navigation

// Clase sellada que define todas las pantallas del flujo oficial de la app
sealed class Screen(val route: String) {

    // 1. Pantalla de Login y Registro (RF1)
    object Login : Screen("login")

    // 2. Pantalla principal del catálogo de mascotas con búsqueda y filtros (RF4, RF5, RF6)
    object Feed : Screen("feed")

    // 3. Pantalla de detalle de una mascota con carrusel de fotos (RF4)
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
}