package com.example.app_adopcion_d_mascotas.data.model

import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient

// Roles únicos de los usuarios
enum class UserRole {
    ADOPTANTE,
    REFUGIO
}

// Información del usuario (RF1 y RF2)
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.ADOPTANTE,
    val phone: String = "",
    val bio: String = "",
    val avatarPhotoId: String = ""
) {
    // URL del avatar (de Appwrite Storage o avatar generado)
    val avatarUrl: String
        get() = if (avatarPhotoId.isNotBlank()) {
            AppwriteClient.getFileViewUrl(avatarPhotoId)
        } else {
            "https://api.dicebear.com/7.x/bottts/png?seed=${name.ifBlank { "User" }}"
        }
}
