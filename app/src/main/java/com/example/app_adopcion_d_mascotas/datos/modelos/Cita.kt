package com.example.app_adopcion_d_mascotas.datos.modelos

/*
 * ============================================================================
 * ARCHIVO: Cita.kt
 * CAPA: Datos -> Modelos (RF20)
 * ¿QUÉ HACE?: Representa una cita agendada por el Refugio (presencial o
 *             videollamada) para entrevistar al Adoptante.
 * ============================================================================
 */

// Estados de la cita entre Refugio y Adoptante (RF20)
enum class EstadoCita {
    PROPUESTA,   // El refugio propuso fecha y hora
    CONFIRMADA,  // El adoptante aceptó asistir en esa fecha y hora
    REPROGRAMAR, // El adoptante solicitó cambiar la fecha/hora
    RECHAZADA    // Cita cancelada
}

typealias AppointmentStatus = EstadoCita

data class Cita(
    val id: String = "",
    val requestId: String = "",                         // ID de la solicitud de adopción asociada
    val petName: String = "",                           // Nombre de la mascota
    val applicantName: String = "",                     // Nombre del adoptante citado
    val shelterName: String = "",                       // Nombre del refugio que agenda
    val date: String = "",                              // Fecha de la cita (ej. 15/11/2026)
    val time: String = "",                              // Hora de la cita (ej. 10:30)
    val modality: String = "Presencial",                // Modalidad: Presencial o Videollamada
    val status: EstadoCita = EstadoCita.PROPUESTA       // Estado actual de la cita
) {
    // Propiedades en español claro
    val idSolicitud: String get() = requestId
    val nombreMascota: String get() = petName
    val nombreSolicitante: String get() = applicantName
    val nombrePostulante: String get() = applicantName
    val nombreRefugio: String get() = shelterName
    val fecha: String get() = date
    val hora: String get() = time
    val modalidad: String get() = modality
    val estado: EstadoCita get() = status
}

typealias Appointment = Cita
