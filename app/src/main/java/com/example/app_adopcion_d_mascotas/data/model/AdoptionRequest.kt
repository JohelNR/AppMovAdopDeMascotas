package com.example.app_adopcion_d_mascotas.data.model

enum class RequestStatus {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    NO_DISPONIBLE
}

data class AdoptionRequest(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val applicantId: String = "",
    val applicantName: String = "",
    val applicantEmail: String = "",
    val shelterName: String = "",
    val reason: String = "",
    val hasYard: Boolean = false,
    val hasExperience: Boolean = false,
    val termsAccepted: Boolean = false,
    val status: RequestStatus = RequestStatus.PENDIENTE
)
