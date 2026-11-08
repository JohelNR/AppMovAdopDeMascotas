package com.example.app_adopcion_d_mascotas.datos.modelos

import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite

/*
 * ============================================================================
 * ARCHIVO: SeguimientoMascota.kt
 * CAPA: Datos -> Modelos (RF11)
 * ¿QUÉ HACE?: Representa un reporte post-adopción con fotos y notas de salud
 *             y adaptación que el adoptante sube a la nube para el refugio.
 * ============================================================================
 */
data class SeguimientoMascota(
    val id: String = "",
    val petId: String = "",                     // ID de la mascota adoptada
    val petName: String = "",                   // Nombre de la mascota
    val applicantId: String = "",               // ID del adoptante que envía el reporte
    val applicantName: String = "",             // Nombre del adoptante
    val shelterName: String = "",               // Nombre del refugio que supervisa
    val date: String = "",                      // Fecha del reporte (DD/MM/AAAA)
    val healthNotes: String = "",               // Notas de salud, vacunas y alimentación
    val adaptationNotes: String = "",           // Notas de comportamiento en el nuevo hogar
    val photos: List<String> = emptyList(),     // IDs de fotos en Appwrite Storage
    val status: String = "ENVIADO"              // Estado: ENVIADO o REVISADO
) {
    // Propiedades en español claro
    val idMascota: String get() = petId
    val nombreMascota: String get() = petName
    val idSolicitante: String get() = applicantId
    val idPostulante: String get() = applicantId
    val nombreSolicitante: String get() = applicantName
    val nombrePostulante: String get() = applicantName
    val nombreRefugio: String get() = shelterName
    val fecha: String get() = date
    val notasSalud: String get() = healthNotes
    val notasAdaptacion: String get() = adaptationNotes
    val fotos: List<String> get() = photos
    val estado: String get() = status

    /*
     * Convierte los IDs de las fotos del reporte en URLs listas para mostrarse en pantalla
     */
    fun obtenerUrlsFotos(): List<String> {
        return photos.map { ClienteAppwrite.obtenerUrlArchivo(it) }
    }

    fun getPhotoUrls(): List<String> = obtenerUrlsFotos()
    fun getAllPhotoUrls(): List<String> = obtenerUrlsFotos()
}

typealias PetFollowUp = SeguimientoMascota
