package com.example.app_adopcion_d_mascotas.navegacion

import android.net.Uri

// Clase sellada que define las rutas de navegación oficiales de la app
sealed class Ruta(val ruta: String) {

    val route: String get() = ruta

    // 1. Pantalla de Inicio de Sesión y Registro (RF1)
    object InicioSesion : Ruta("login")

    // 2. Pantalla principal del catálogo de mascotas (RF4, RF5, RF6)
    object Catalogo : Ruta("feed")

    // 3. Pantalla de detalle de una mascota (RF4, RF7)
    object Detalle : Ruta("detail/{petId}") {
        fun crearRuta(idMascota: String) = "detail/$idMascota"
        fun createRoute(petId: String) = crearRuta(petId)
    }

    // 4. Pantalla para crear o editar publicación (RF3)
    object Publicar : Ruta("publish?petId={petId}") {
        fun crearRuta(idMascota: String? = null) =
            if (idMascota != null) "publish?petId=$idMascota" else "publish"
        fun createRoute(petId: String? = null) = crearRuta(petId)
    }

    // 5. Formulario de Solicitud de Adopción (RF8)
    object FormularioAdopcion : Ruta("adopt/{petId}") {
        fun crearRuta(idMascota: String) = "adopt/$idMascota"
        fun createRoute(petId: String) = crearRuta(petId)
    }

    // 6. Panel de Solicitudes (RF9, RF10)
    object Solicitudes : Ruta("requests")

    // 7. Coordinación y agenda de entrevistas / citas (RF20)
    object Citas : Ruta("appointments")

    // 8. Perfil de Usuario con avatar (RF2)
    object Perfil : Ruta("profile")

    // 9. Chat en Tiempo Real (RF13)
    object Chat : Ruta("chat/{chatId}?petName={petName}&receiverName={receiverName}") {
        fun crearRuta(idChat: String, nombreMascota: String = "", nombreDestinatario: String = "Refugio"): String {
            val petCodificada = Uri.encode(nombreMascota)
            val destinatarioCodificado = Uri.encode(nombreDestinatario)
            return "chat/$idChat?petName=$petCodificada&receiverName=$destinatarioCodificado"
        }
        fun createRoute(chatId: String, petName: String = "", receiverName: String = "Refugio"): String =
            crearRuta(chatId, petName, receiverName)
    }

    // 10. Seguimiento Post-Adopción (RF11)
    object Seguimiento : Ruta("followup/{petId}?petName={petName}&shelterName={shelterName}") {
        fun crearRuta(idMascota: String, nombreMascota: String = "", nombreRefugio: String = "Refugio"): String {
            val petCodificada = Uri.encode(nombreMascota)
            val refugioCodificado = Uri.encode(nombreRefugio)
            return "followup/$idMascota?petName=$petCodificada&shelterName=$refugioCodificado"
        }
        fun createRoute(petId: String, petName: String = "", shelterName: String = "Refugio"): String =
            crearRuta(petId, petName, shelterName)
    }
}

// Compatibilidad con Screen
typealias Screen = Ruta
