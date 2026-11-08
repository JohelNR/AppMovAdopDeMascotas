package com.example.app_adopcion_d_mascotas.datos.modelos

import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite

/*
 * ============================================================================
 * ARCHIVO: Mascota.kt
 * CAPA: Datos -> Modelos (RF3, RF4, RF5, RF6, RF7)
 * ¿QUÉ HACE?: Representa toda la ficha de una mascota publicada por un refugio,
 *             incluyendo sus fotos en la nube y sus coordenadas para el mapa.
 * ============================================================================
 */

// Estados posibles de una mascota dentro del proceso de adopción (RF6 y RF10)
enum class EstadoMascota {
    DISPONIBLE,  // Lista para recibir solicitudes de adopción
    EN_PROCESO,  // En evaluación o entrevista con un adoptante
    ADOPTADA     // Ya encontró un hogar definitivo
}

typealias PetStatus = EstadoMascota

data class Mascota(
    val id: String = "",
    val name: String = "",                                  // Nombre de la mascota
    val breed: String = "",                                 // Raza (ej. Mestizo, Golden)
    val species: String = "Perro",                          // Especie: Perro, Gato u Otro
    val ageYears: Int = 1,                                  // Edad aproximada en años
    val description: String = "",                           // Historia, vacunas y personalidad
    val imageUrl: String = "",                              // URL de imagen directa (opcional)
    val shelterName: String = "",                           // Nombre del refugio responsable
    val shelterId: String = "",                             // ID del usuario refugio dueño
    val isUrgent: Boolean = false,                          // RF5: ¿Requiere adopción urgente?
    val status: EstadoMascota = EstadoMascota.DISPONIBLE,   // Estado actual de adopción
    val gender: String = "Macho",                           // Sexo: Macho o Hembra
    val size: String = "Mediano",                           // Tamaño: Pequeño, Mediano o Grande
    val location: String = "Cajamarca",                     // Dirección o ciudad
    val photos: List<String> = emptyList(),                 // RF3: Lista de fotos en Appwrite Storage
    val latitude: Double = -7.1617,                         // RF7: Latitud real para el mapa
    val longitude: Double = -78.5128                        // RF7: Longitud real para el mapa
) {
    // Propiedades en español claro para fácil lectura y sustentación
    val nombre: String get() = name
    val raza: String get() = breed
    val especie: String get() = species
    val edadAnos: Int get() = ageYears
    val edadAnios: Int get() = ageYears
    val descripcion: String get() = description
    val nombreRefugio: String get() = shelterName
    val idRefugio: String get() = shelterId
    val esUrgente: Boolean get() = isUrgent
    val estado: EstadoMascota get() = status
    val sexo: String get() = gender
    val tamano: String get() = size
    val ubicacion: String get() = location
    val fotos: List<String> get() = photos
    val latitud: Double get() = latitude
    val longitud: Double get() = longitude

    /*
     * Obtiene la foto principal que se muestra en la tarjeta del catálogo.
     * Prioriza la primera foto subida a Appwrite Storage.
     */
    val fotoPrincipal: String
        get() = if (photos.isNotEmpty()) {
            ClienteAppwrite.obtenerUrlArchivo(photos.first())
        } else if (imageUrl.isNotBlank()) {
            imageUrl
        } else {
            when {
                species.equals("Gato", ignoreCase = true) -> "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"
                species.equals("Otro", ignoreCase = true) -> "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600"
                else -> "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600"
            }
        }

    val displayPhoto: String get() = fotoPrincipal

    /*
     * Devuelve la lista completa de enlaces (URLs) de todas las fotos
     * para mostrarlas en el carrusel de la pantalla de Detalle (RF6).
     */
    fun obtenerTodasLasFotos(): List<String> {
        return if (photos.isNotEmpty()) {
            photos.map { ClienteAppwrite.obtenerUrlArchivo(it) }
        } else if (imageUrl.isNotBlank()) {
            listOf(imageUrl)
        } else {
            listOf(fotoPrincipal)
        }
    }

    fun getAllPhotoUrls(): List<String> = obtenerTodasLasFotos()
}

typealias Pet = Mascota
