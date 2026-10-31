package com.example.app_adopcion_d_mascotas.datos.modelos

import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite

enum class EstadoMascota {
    DISPONIBLE,
    ADOPTADA
}

typealias PetStatus = EstadoMascota

data class Mascota(
    val id: String = "",
    val name: String = "",
    val breed: String = "",             // Raza
    val species: String = "Perro",       // Perro o Gato
    val ageYears: Int = 1,              // Edad
    val description: String = "",       // Historia / descripción
    val imageUrl: String = "",          // Foto por defecto / compatibilidad
    val shelterName: String = "",       // Nombre del refugio
    val isUrgent: Boolean = false,      // RF5: ¿Es caso urgente?
    val status: EstadoMascota = EstadoMascota.DISPONIBLE,
    val gender: String = "Macho",       // RF6: Macho o Hembra
    val size: String = "Mediano",       // RF6: Pequeño, Mediano o Grande
    val location: String = "Lima",      // RF6: Ciudad o distrito
    val photos: List<String> = emptyList(), // RF3: IDs de fotos en Appwrite Storage
    val latitude: Double = -12.0464,    // RF7: Coordenadas de ubicación del refugio
    val longitude: Double = -77.0428
) {
    // Alias en español para facilitar la lectura
    val nombre: String get() = name
    val raza: String get() = breed
    val especie: String get() = species
    val edadAnos: Int get() = ageYears
    val descripcion: String get() = description
    val nombreRefugio: String get() = shelterName
    val esUrgente: Boolean get() = isUrgent
    val estado: EstadoMascota get() = status
    val sexo: String get() = gender
    val tamano: String get() = size
    val ubicacion: String get() = location
    val fotos: List<String> get() = photos
    val latitud: Double get() = latitude
    val longitud: Double get() = longitude

    // Retorna la URL de la foto principal (de Appwrite Storage o fallback)
    val displayPhoto: String
        get() = if (photos.isNotEmpty()) {
            ClienteAppwrite.getFileViewUrl(photos.first())
        } else if (imageUrl.isNotBlank()) {
            imageUrl
        } else {
            when {
                name.contains("Luna", ignoreCase = true) -> "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"
                name.contains("Mimi", ignoreCase = true) -> "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=600"
                name.contains("Rocky", ignoreCase = true) -> "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600"
                name.contains("Max", ignoreCase = true) -> "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600"
                species.equals("Gato", ignoreCase = true) -> "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600"
                else -> "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600"
            }
        }

    val fotoPrincipal: String get() = displayPhoto

    // Retorna todas las URLs de las fotos para el carrusel
    fun getAllPhotoUrls(): List<String> {
        return if (photos.isNotEmpty()) {
            photos.map { ClienteAppwrite.getFileViewUrl(it) }
        } else if (imageUrl.isNotBlank()) {
            listOf(imageUrl)
        } else {
            listOf(displayPhoto)
        }
    }

    fun obtenerTodasLasFotos(): List<String> = getAllPhotoUrls()
}

// Alias para compatibilidad total
typealias Pet = Mascota
