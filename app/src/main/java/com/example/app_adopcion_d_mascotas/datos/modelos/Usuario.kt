package com.example.app_adopcion_d_mascotas.datos.modelos

import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite

// Roles de usuario (RF1)
enum class RolUsuario {
    ADOPTANTE,
    REFUGIO
}

typealias UserRole = RolUsuario

// Modelo de datos del Usuario (RF1 y RF2)
data class Usuario(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: RolUsuario = RolUsuario.ADOPTANTE,
    val phone: String = "",
    val bio: String = "",
    val avatarPhotoId: String = ""
) {
    val nombre: String get() = name
    val correo: String get() = email
    val rol: RolUsuario get() = role
    val telefono: String get() = phone
    val biografia: String get() = bio
    val idFotoAvatar: String get() = avatarPhotoId

    // URL del avatar (de Appwrite Storage o generado con Dicebear)
    val avatarUrl: String
        get() = if (avatarPhotoId.isNotBlank()) {
            ClienteAppwrite.getFileViewUrl(avatarPhotoId)
        } else {
            "https://api.dicebear.com/7.x/bottts/png?seed=${name.ifBlank { "User" }}"
        }

    val urlAvatar: String get() = avatarUrl
}

typealias User = Usuario
