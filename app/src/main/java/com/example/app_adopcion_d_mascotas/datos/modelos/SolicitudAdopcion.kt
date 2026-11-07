package com.example.app_adopcion_d_mascotas.datos.modelos

enum class EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    NO_DISPONIBLE
}

typealias RequestStatus = EstadoSolicitud

data class SolicitudAdopcion(
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
    val status: EstadoSolicitud = EstadoSolicitud.PENDIENTE
) {
    val idMascota: String get() = petId
    val nombreMascota: String get() = petName
    val idPostulante: String get() = applicantId
    val nombrePostulante: String get() = applicantName
    val correoPostulante: String get() = applicantEmail
    val nombreRefugio: String get() = shelterName
    val motivo: String get() = reason
    val tienePatio: Boolean get() = hasYard
    val tieneExperiencia: Boolean get() = hasExperience
    val terminosAceptados: Boolean get() = termsAccepted
    val estado: EstadoSolicitud get() = status
}

typealias AdoptionRequest = SolicitudAdopcion
