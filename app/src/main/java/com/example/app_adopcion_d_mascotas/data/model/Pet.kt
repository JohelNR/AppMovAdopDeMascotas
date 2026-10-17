package com.example.app_adopcion_d_mascotas.data.model

import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient

enum class PetStatus {
    DISPONIBLE,
    ADOPTADA
}

data class Pet(
    val id: String = "",
    val name: String = "",
    val breed: String = "",             // Raza
    val species: String = "Perro",       // Perro o Gato
    val ageYears: Int = 1,              // Edad
    val description: String = "",       // Historia / descripción
    val imageUrl: String = "",          // Foto por defecto / compatibilidad
    val shelterName: String = "",       // Nombre del refugio
    val isUrgent: Boolean = false,      // RF5: ¿Es caso urgente?
    val status: PetStatus = PetStatus.DISPONIBLE,
    val gender: String = "Macho",       // RF6: Macho o Hembra
    val size: String = "Mediano",       // RF6: Pequeño, Mediano o Grande
    val location: String = "Lima",      // RF6: Ciudad o distrito
    val photos: List<String> = emptyList() // RF3: IDs de fotos en Appwrite Storage
) {
    // Retorna la URL de la foto principal (de Appwrite Storage o fallback)
    val displayPhoto: String
        get() = if (photos.isNotEmpty()) {
            AppwriteClient.getFileViewUrl(photos.first())
        } else if (imageUrl.isNotBlank()) {
            imageUrl
        } else {
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500"
        }

    // Retorna todas las URLs de las fotos para el carrusel
    fun getAllPhotoUrls(): List<String> {
        return if (photos.isNotEmpty()) {
            photos.map { AppwriteClient.getFileViewUrl(it) }
        } else if (imageUrl.isNotBlank()) {
            listOf(imageUrl)
        } else {
            listOf(displayPhoto)
        }
    }
}