package com.example.app_adopcion_d_mascotas.datos.modelos

/*
 * ============================================================================
 * ARCHIVO: SolicitudAdopcion.kt
 * CAPA: Datos -> Modelos (RF8, RF9, RF10)
 * ¿QUÉ HACE?: Representa el formulario que envía un Adoptante cuando postula
 *             para adoptar a una mascota de un Refugio.
 * ============================================================================
 */

// Estados por los que pasa una solicitud de adopción
enum class EstadoSolicitud {
    PENDIENTE,      // Recién enviada, esperando respuesta del refugio
    ACEPTADA,       // Aprobada por el refugio (la mascota pasa a ADOPTADA)
    RECHAZADA,      // Descartada manualmente por el refugio
    NO_DISPONIBLE   // Cerrada automáticamente en cascada porque otro adoptante fue elegido (RF10)
}

typealias RequestStatus = EstadoSolicitud

data class SolicitudAdopcion(
    val id: String = "",
    val petId: String = "",                                     // ID de la mascota solicitada
    val petName: String = "",                                   // Nombre de la mascota
    val applicantId: String = "",                               // ID del usuario adoptante
    val applicantName: String = "",                             // Nombre del adoptante
    val applicantEmail: String = "",                            // Correo del adoptante
    val shelterName: String = "",                               // Nombre del refugio dueño
    val reason: String = "",                                    // Motivo por el que desea adoptar
    val hasYard: Boolean = false,                               // ¿Cuenta con patio o espacio abierto?
    val hasExperience: Boolean = false,                         // ¿Tiene experiencia previa con mascotas?
    val termsAccepted: Boolean = false,                         // ¿Aceptó el compromiso responsable?
    val status: EstadoSolicitud = EstadoSolicitud.PENDIENTE     // Estado actual de la solicitud
) {
    // Propiedades en español claro para fácil lectura en las pantallas
    val idMascota: String get() = petId
    val nombreMascota: String get() = petName
    val idSolicitante: String get() = applicantId
    val idPostulante: String get() = applicantId
    val nombreSolicitante: String get() = applicantName
    val nombrePostulante: String get() = applicantName
    val correoSolicitante: String get() = applicantEmail
    val correoPostulante: String get() = applicantEmail
    val nombreRefugio: String get() = shelterName
    val motivo: String get() = reason
    val tienePatio: Boolean get() = hasYard
    val tieneExperiencia: Boolean get() = hasExperience
    val aceptaTerminos: Boolean get() = termsAccepted
    val terminosAceptados: Boolean get() = termsAccepted
    val estado: EstadoSolicitud get() = status
}

typealias AdoptionRequest = SolicitudAdopcion
