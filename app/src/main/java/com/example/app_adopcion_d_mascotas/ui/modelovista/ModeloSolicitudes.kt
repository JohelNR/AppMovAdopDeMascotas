package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.AdoptionRequest
import com.example.app_adopcion_d_mascotas.datos.modelos.Appointment
import com.example.app_adopcion_d_mascotas.datos.modelos.AppointmentStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.Cita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoCita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoSolicitud
import com.example.app_adopcion_d_mascotas.datos.modelos.RequestStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioSolicitudesAppwrite
import com.example.app_adopcion_d_mascotas.notificaciones.AyudanteNotificaciones
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelChildren

class ModeloSolicitudes(private val RepositorioSolicitudesAppwrite: RepositorioSolicitudesAppwrite) : ViewModel() {

    var listaSolicitudes by mutableStateOf<List<SolicitudAdopcion>>(emptyList())
        private set

    var listaCitas by mutableStateOf<List<Cita>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeRespuesta by mutableStateOf<String?>(null)

    fun limpiarSesion() {
        viewModelScope.coroutineContext.cancelChildren()
        listaSolicitudes = emptyList()
        listaCitas = emptyList()
        mensajeRespuesta = null
        estaCargando = false
    }

    // Alias para compatibilidad
    var requestsList: List<SolicitudAdopcion>
        get() = listaSolicitudes
        private set(value) { listaSolicitudes = value }

    var appointmentsList: List<Cita>
        get() = listaCitas
        private set(value) { listaCitas = value }

    val isLoading: Boolean get() = estaCargando
    var feedbackMessage: String?
        get() = mensajeRespuesta
        set(value) { mensajeRespuesta = value }

    fun cargarSolicitudes(usuarioActual: Usuario?) {
        if (usuarioActual == null) return

        viewModelScope.launch {
            estaCargando = true
            listaSolicitudes = if (usuarioActual.role == RolUsuario.REFUGIO) {
                RepositorioSolicitudesAppwrite.getRequestsForShelter(usuarioActual.name)
            } else {
                RepositorioSolicitudesAppwrite.getRequestsForApplicant(usuarioActual.id)
            }
            cargarCitas(usuarioActual)
            estaCargando = false
        }
    }

    fun loadRequests(currentUser: Usuario?) = cargarSolicitudes(currentUser)

    fun cargarCitas(usuarioActual: Usuario?) {
        if (usuarioActual == null) return
        viewModelScope.launch {
            listaCitas = RepositorioSolicitudesAppwrite.getAppointments(usuarioActual.name)
        }
    }

    fun loadAppointments(currentUser: Usuario?) = cargarCitas(currentUser)

    fun enviarSolicitudAdopcion(
        idMascota: String,
        nombreMascota: String,
        postulante: Usuario,
        nombreRefugio: String,
        motivo: String,
        tienePatio: Boolean,
        tieneExperiencia: Boolean,
        terminosAceptados: Boolean,
        contexto: Context? = null,
        alTenerExito: () -> Unit
    ) {
        if (motivo.isBlank()) {
            mensajeRespuesta = "Por favor explica por qué deseas adoptar"
            return
        }
        if (!terminosAceptados) {
            mensajeRespuesta = "Debes aceptar el compromiso de tenencia responsable"
            return
        }

        viewModelScope.launch {
            estaCargando = true
            val solicitud = SolicitudAdopcion(
                petId = idMascota,
                petName = nombreMascota,
                applicantId = postulante.id,
                applicantName = postulante.name,
                applicantEmail = postulante.email,
                shelterName = nombreRefugio,
                reason = motivo.trim(),
                hasYard = tienePatio,
                hasExperience = tieneExperiencia,
                termsAccepted = terminosAceptados,
                status = EstadoSolicitud.PENDIENTE
            )

            val exito = RepositorioSolicitudesAppwrite.createRequest(solicitud)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "¡Solicitud enviada con éxito!"
                contexto?.let { ctx ->
                    AyudanteNotificaciones.showNotification(
                        context = ctx,
                        notificationId = 1001,
                        title = "🐾 Solicitud Enviada",
                        message = "Tu solicitud para adoptar a $nombreMascota ha sido registrada con éxito."
                    )
                }
                alTenerExito()
            } else {
                mensajeRespuesta = "Error al enviar la solicitud a la nube"
            }
        }
    }

    fun submitAdoptionRequest(
        petId: String,
        petName: String,
        applicant: Usuario,
        shelterName: String,
        reason: String,
        hasYard: Boolean,
        hasExperience: Boolean,
        termsAccepted: Boolean,
        context: Context? = null,
        onSuccess: () -> Unit
    ) = enviarSolicitudAdopcion(
        idMascota = petId,
        nombreMascota = petName,
        postulante = applicant,
        nombreRefugio = shelterName,
        motivo = reason,
        tienePatio = hasYard,
        tieneExperiencia = hasExperience,
        terminosAceptados = termsAccepted,
        contexto = context,
        alTenerExito = onSuccess
    )

    fun aceptarSolicitud(solicitud: SolicitudAdopcion, usuarioActual: Usuario?, contexto: Context? = null, alTenerExito: () -> Unit) {
        viewModelScope.launch {
            estaCargando = true
            val exito = RepositorioSolicitudesAppwrite.acceptRequest(solicitud.id, solicitud.petId)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "¡Solicitud aceptada! Mascota marcada como ADOPTADA automáticamente."
                contexto?.let { ctx ->
                    AyudanteNotificaciones.showNotification(
                        context = ctx,
                        notificationId = 1002,
                        title = "🎉 ¡Adopción Confirmada!",
                        message = "La adopción de ${solicitud.petName} fue formalizada con éxito."
                    )
                }
                cargarSolicitudes(usuarioActual)
                alTenerExito()
            } else {
                mensajeRespuesta = "Error al aceptar la solicitud"
            }
        }
    }

    fun acceptRequest(request: SolicitudAdopcion, currentUser: Usuario?, context: Context? = null, onSuccess: () -> Unit) =
        aceptarSolicitud(request, currentUser, context, onSuccess)

    fun rechazarSolicitud(idSolicitud: String, usuarioActual: Usuario?) {
        viewModelScope.launch {
            estaCargando = true
            val exito = RepositorioSolicitudesAppwrite.rejectRequest(idSolicitud)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "Solicitud rechazada"
                cargarSolicitudes(usuarioActual)
            } else {
                mensajeRespuesta = "Error al rechazar solicitud"
            }
        }
    }

    fun rejectRequest(requestId: String, currentUser: Usuario?) = rechazarSolicitud(requestId, currentUser)

    fun agendarCita(
        idSolicitud: String,
        nombreMascota: String,
        nombrePostulante: String,
        nombreRefugio: String,
        fecha: String,
        hora: String,
        modalidad: String,
        usuarioActual: Usuario?,
        contexto: Context? = null,
        alTenerExito: () -> Unit
    ) {
        if (fecha.isBlank() || hora.isBlank()) {
            mensajeRespuesta = "Por favor ingresa fecha y hora de la cita"
            return
        }
        val instante = AyudanteNotificaciones.fechaHoraMillis(fecha, hora)
        if (instante == null || instante <= System.currentTimeMillis()) {
            mensajeRespuesta = "Ingresa una fecha futura (dd/MM/aaaa) y hora (HH:mm) válidas"
            return
        }

        viewModelScope.launch {
            estaCargando = true
            val cita = Cita(
                requestId = idSolicitud,
                petName = nombreMascota,
                applicantName = nombrePostulante,
                shelterName = nombreRefugio,
                date = fecha,
                time = hora,
                modality = modalidad,
                status = EstadoCita.PROPUESTA
            )

            val exito = RepositorioSolicitudesAppwrite.createAppointment(cita)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "Cita agendada con éxito"
                contexto?.let { ctx ->
                    AyudanteNotificaciones.showNotification(
                        context = ctx,
                        notificationId = 1003,
                        title = "📅 Cita Agendada",
                        message = "Cita con $nombreRefugio para $nombreMascota agendada para el $fecha a las $hora."
                    )
                    AyudanteNotificaciones.scheduleAppointmentAlarm(
                        context = ctx,
                        appointmentId = cita.id.ifEmpty { System.currentTimeMillis().toString() },
                        petName = nombreMascota,
                        shelterName = nombreRefugio,
                        date = fecha,
                        time = hora
                    )
                }
                cargarCitas(usuarioActual)
                alTenerExito()
            } else {
                mensajeRespuesta = "Error al guardar la cita"
            }
        }
    }

    fun scheduleAppointment(
        requestId: String,
        petName: String,
        applicantName: String,
        shelterName: String,
        date: String,
        time: String,
        modality: String,
        currentUser: Usuario?,
        context: Context? = null,
        onSuccess: () -> Unit
    ) = agendarCita(
        idSolicitud = requestId,
        nombreMascota = petName,
        nombrePostulante = applicantName,
        nombreRefugio = shelterName,
        fecha = date,
        hora = time,
        modalidad = modality,
        usuarioActual = currentUser,
        contexto = context,
        alTenerExito = onSuccess
    )

    fun confirmarCita(idCita: String, usuarioActual: Usuario?) {
        viewModelScope.launch {
            RepositorioSolicitudesAppwrite.updateAppointmentStatus(idCita, EstadoCita.CONFIRMADA)
            cargarCitas(usuarioActual)
        }
    }

    fun confirmAppointment(appointmentId: String, currentUser: Usuario?) = confirmarCita(appointmentId, currentUser)
}

typealias RequestsViewModel = ModeloSolicitudes
