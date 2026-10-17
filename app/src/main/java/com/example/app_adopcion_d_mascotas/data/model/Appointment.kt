package com.example.app_adopcion_d_mascotas.data.model

enum class AppointmentStatus {
    PROPUESTA,
    CONFIRMADA,
    RECHAZADA
}

data class Appointment(
    val id: String = "",
    val requestId: String = "",
    val petName: String = "",
    val applicantName: String = "",
    val shelterName: String = "",
    val date: String = "",
    val time: String = "",
    val modality: String = "Presencial",
    val status: AppointmentStatus = AppointmentStatus.PROPUESTA
)
