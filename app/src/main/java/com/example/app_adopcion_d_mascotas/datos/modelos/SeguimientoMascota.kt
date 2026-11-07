package com.example.app_adopcion_d_mascotas.datos.modelos

import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite

// RF11: Formulario y reporte de seguimiento post-adopción
data class SeguimientoMascota(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val applicantId: String = "",
    val applicantName: String = "",
    val shelterName: String = "",
    val date: String = "",               // Fecha del reporte
    val healthNotes: String = "",        // Estado de salud y vacunas
    val adaptationNotes: String = "",    // Comportamiento y adaptación al hogar
    val photos: List<String> = emptyList(), // Fotos del animalito en su nuevo hogar (Appwrite Storage)
    val status: String = "ENVIADO"       // ENVIADO, REVISADO
) {
    val idMascota: String get() = petId
    val nombreMascota: String get() = petName
    val idPostulante: String get() = applicantId
    val nombrePostulante: String get() = applicantName
    val nombreRefugio: String get() = shelterName
    val fecha: String get() = date
    val notasSalud: String get() = healthNotes
    val notasAdaptacion: String get() = adaptationNotes
    val fotos: List<String> get() = photos
    val estado: String get() = status

    // Retorna las URLs de las fotos del reporte
    fun getPhotoUrls(): List<String> {
        return photos.map { ClienteAppwrite.getFileViewUrl(it) }
    }

    fun getAllPhotoUrls(): List<String> = getPhotoUrls()

    fun obtenerUrlsFotos(): List<String> = getPhotoUrls()
}

typealias PetFollowUp = SeguimientoMascota
