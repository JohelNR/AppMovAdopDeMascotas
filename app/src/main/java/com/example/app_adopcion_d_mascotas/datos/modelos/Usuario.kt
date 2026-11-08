package com.example.app_adopcion_d_mascotas.datos.modelos

import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite

/*
 * ============================================================================
 * ARCHIVO: Usuario.kt
 * CAPA: Datos -> Modelos (RF1 y RF2)
 * ¿QUÉ HACE?: Representa a una persona o institución registrada en la app.
 *             Puede tener uno de dos roles: ADOPTANTE o REFUGIO.
 * ============================================================================
 */

// Los dos tipos de usuario permitidos en el sistema (RF1)
enum class RolUsuario {
    ADOPTANTE, // Persona que busca adoptar una mascota
    REFUGIO    // Albergue o rescatista que publica mascotas en adopción
}

typealias UserRole = RolUsuario

data class Usuario(
    val id: String = "",            // Identificador único del usuario en Appwrite Auth
    val name: String = "",          // Nombre completo o nombre del refugio
    val email: String = "",         // Correo electrónico
    val role: RolUsuario = RolUsuario.ADOPTANTE, // Rol asignado (ADOPTANTE o REFUGIO)
    val phone: String = "",         // Teléfono de contacto (9 dígitos - RF2)
    val bio: String = "",           // Biografía o dirección del refugio (RF2)
    val avatarPhotoId: String = ""  // ID de la foto de perfil en Appwrite Storage (RF2)
) {
    // Propiedades en español claro para usar en toda la aplicación
    val nombre: String get() = name
    val correo: String get() = email
    val rol: RolUsuario get() = role
    val telefono: String get() = phone
    val biografia: String get() = bio
    val idFotoAvatar: String get() = avatarPhotoId

    /*
     * Devuelve el enlace (URL) de la foto de perfil.
     * Si el usuario subió una foto a Appwrite, usa esa foto real.
     * Si aún no tiene foto, genera un avatar simpático con su nombre.
     */
    val urlAvatar: String
        get() = if (avatarPhotoId.isNotBlank()) {
            ClienteAppwrite.obtenerUrlArchivo(avatarPhotoId)
        } else {
            val semilla = name.ifBlank { "Usuario" }.replace(" ", "")
            "https://api.dicebear.com/7.x/bottts/png?seed=$semilla"
        }

    val avatarUrl: String get() = urlAvatar
}

typealias User = Usuario
