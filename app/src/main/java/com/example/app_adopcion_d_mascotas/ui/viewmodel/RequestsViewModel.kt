package com.example.app_adopcion_d_mascotas.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.AdoptionRequest
import com.example.app_adopcion_d_mascotas.data.model.Appointment
import com.example.app_adopcion_d_mascotas.data.model.AppointmentStatus
import com.example.app_adopcion_d_mascotas.data.model.RequestStatus
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteRequestsRepository
import com.example.app_adopcion_d_mascotas.notifications.NotificationHelper
import kotlinx.coroutines.launch

class RequestsViewModel : ViewModel() {

    var requestsList by mutableStateOf<List<AdoptionRequest>>(emptyList())
        private set

    var appointmentsList by mutableStateOf<List<Appointment>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)

    // Cargar solicitudes según el rol del usuario actual
    fun loadRequests(currentUser: User?) {
        if (currentUser == null) return

        viewModelScope.launch {
            isLoading = true
            requestsList = if (currentUser.role == UserRole.REFUGIO) {
                // RF9: El refugio ve las solicitudes enviadas a su refugio
                AppwriteRequestsRepository.getRequestsForShelter(currentUser.name)
            } else {
                // El adoptante ve sus propias solicitudes
                AppwriteRequestsRepository.getRequestsForApplicant(currentUser.id)
            }
            loadAppointments(currentUser)
            isLoading = false
        }
    }

    fun loadAppointments(currentUser: User?) {
        if (currentUser == null) return
        viewModelScope.launch {
            appointmentsList = AppwriteRequestsRepository.getAppointments(currentUser.name)
        }
    }

    // RF8 & RF14: Enviar solicitud de adopción con notificación
    fun submitAdoptionRequest(
        petId: String,
        petName: String,
        applicant: User,
        shelterName: String,
        reason: String,
        hasYard: Boolean,
        hasExperience: Boolean,
        termsAccepted: Boolean,
        context: Context? = null,
        onSuccess: () -> Unit
    ) {
        if (reason.isBlank()) {
            feedbackMessage = "Por favor explica por qué deseas adoptar"
            return
        }
        if (!termsAccepted) {
            feedbackMessage = "Debes aceptar el compromiso de tenencia responsable"
            return
        }

        viewModelScope.launch {
            isLoading = true
            val request = AdoptionRequest(
                petId = petId,
                petName = petName,
                applicantId = applicant.id,
                applicantName = applicant.name,
                applicantEmail = applicant.email,
                shelterName = shelterName,
                reason = reason.trim(),
                hasYard = hasYard,
                hasExperience = hasExperience,
                termsAccepted = termsAccepted,
                status = RequestStatus.PENDIENTE
            )

            val success = AppwriteRequestsRepository.createRequest(request)
            isLoading = false

            if (success) {
                feedbackMessage = "¡Solicitud enviada con éxito!"
                context?.let { ctx ->
                    NotificationHelper.showNotification(
                        context = ctx,
                        notificationId = 1001,
                        title = "🐾 Solicitud Enviada",
                        message = "Tu solicitud para adoptar a $petName ha sido registrada con éxito."
                    )
                }
                onSuccess()
            } else {
                feedbackMessage = "Error al enviar la solicitud a la nube"
            }
        }
    }

    // RF10 & RF14: Aceptar solicitud con notificación
    fun acceptRequest(request: AdoptionRequest, currentUser: User?, context: Context? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val success = AppwriteRequestsRepository.acceptRequest(request.id, request.petId)
            isLoading = false

            if (success) {
                feedbackMessage = "¡Solicitud aceptada! Mascota marcada como ADOPTADA automáticamente."
                context?.let { ctx ->
                    NotificationHelper.showNotification(
                        context = ctx,
                        notificationId = 1002,
                        title = "🎉 ¡Adopción Confirmada!",
                        message = "La adopción de ${request.petName} fue formalizada con éxito."
                    )
                }
                loadRequests(currentUser)
                onSuccess()
            } else {
                feedbackMessage = "Error al aceptar la solicitud"
            }
        }
    }

    fun rejectRequest(requestId: String, currentUser: User?) {
        viewModelScope.launch {
            isLoading = true
            val success = AppwriteRequestsRepository.rejectRequest(requestId)
            isLoading = false

            if (success) {
                feedbackMessage = "Solicitud rechazada"
                loadRequests(currentUser)
            } else {
                feedbackMessage = "Error al rechazar solicitud"
            }
        }
    }

    // RF20 & RF14: Coordinar Cita / Entrevista y programar alarma con AlarmManager
    fun scheduleAppointment(
        requestId: String,
        petName: String,
        applicantName: String,
        shelterName: String,
        date: String,
        time: String,
        modality: String,
        currentUser: User?,
        context: Context? = null,
        onSuccess: () -> Unit
    ) {
        if (date.isBlank() || time.isBlank()) {
            feedbackMessage = "Por favor ingresa fecha y hora de la cita"
            return
        }

        viewModelScope.launch {
            isLoading = true
            val appointment = Appointment(
                requestId = requestId,
                petName = petName,
                applicantName = applicantName,
                shelterName = shelterName,
                date = date,
                time = time,
                modality = modality,
                status = AppointmentStatus.PROPUESTA
            )

            val success = AppwriteRequestsRepository.createAppointment(appointment)
            isLoading = false

            if (success) {
                feedbackMessage = "Cita agendada con éxito"
                context?.let { ctx ->
                    NotificationHelper.showNotification(
                        context = ctx,
                        notificationId = 1003,
                        title = "📅 Cita Agendada",
                        message = "Cita con $shelterName para $petName agendada para el $date a las $time."
                    )
                    NotificationHelper.scheduleAppointmentAlarm(
                        context = ctx,
                        appointmentId = appointment.id.ifEmpty { System.currentTimeMillis().toString() },
                        petName = petName,
                        shelterName = shelterName,
                        date = date,
                        time = time
                    )
                }
                loadAppointments(currentUser)
                onSuccess()
            } else {
                feedbackMessage = "Error al guardar la cita"
            }
        }
    }

    fun confirmAppointment(appointmentId: String, currentUser: User?) {
        viewModelScope.launch {
            AppwriteRequestsRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMADA)
            loadAppointments(currentUser)
        }
    }
}
