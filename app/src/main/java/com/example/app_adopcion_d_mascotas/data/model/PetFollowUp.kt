package com.example.app_adopcion_d_mascotas.data.model

import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient

// RF11: Formulario y reporte de seguimiento post-adopción
data class PetFollowUp(
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
    // Retorna las URLs de las fotos del reporte
    fun getPhotoUrls(): List<String> {
        return photos.map { AppwriteClient.getFileViewUrl(it) }
    }

    fun getAllPhotoUrls(): List<String> = getPhotoUrls()

    fun obtenerUrlsFotos(): List<String> = getPhotoUrls()
}
