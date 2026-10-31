package com.example.app_adopcion_d_mascotas.datos.modelos

enum class EstadoCita {
    PROPUESTA,
    CONFIRMADA,
    RECHAZADA
}

typealias AppointmentStatus = EstadoCita

data class Cita(
    val id: String = "",
    val requestId: String = "",
    val petName: String = "",
    val applicantName: String = "",
    val shelterName: String = "",
    val date: String = "",
    val time: String = "",
    val modality: String = "Presencial",
    val status: EstadoCita = EstadoCita.PROPUESTA
) {
    val idSolicitud: String get() = requestId
    val nombreMascota: String get() = petName
    val nombrePostulante: String get() = applicantName
    val nombreRefugio: String get() = shelterName
    val fecha: String get() = date
    val hora: String get() = time
    val modalidad: String get() = modality
    val estado: EstadoCita get() = status
}

typealias Appointment = Cita
