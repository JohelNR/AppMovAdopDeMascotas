package com.example.app_adopcion_d_mascotas.navigation

// Clase sellada que define las pantallas permitidas en nuestra app
sealed class Screen(val route: String) {

    // 1. Pantalla de Login y Registro
    object Login : Screen("login")

    // 2. Pantalla principal del catálogo de mascotas
    object Feed : Screen("feed")

    // 3. Pantalla de detalle de una mascota, recibe el id de la mascota
    object Detail : Screen("detail/{petId}") {
        fun createRoute(petId: String) = "detail/$petId"
    }

    // 4. Pantalla para que el refugio cree o edite una mascota
    object Publish : Screen("publish?petId={petId}") {
        fun createRoute(petId: String? = null) =
            if (petId != null) "publish?petId=$petId" else "publish"
    }

}