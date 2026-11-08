package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.Cita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoCita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoSolicitud
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioSolicitudesAppwrite
import com.example.app_adopcion_d_mascotas.notificaciones.AyudanteNotificaciones
import kotlinx.coroutines.launch

/*
 * ============================================================================
 * ARCHIVO: ModeloSolicitudes.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF8, RF9, RF10, RF14 y RF20)
 * ¿QUÉ HACE?: Gestiona el envío y consulta de solicitudes de adopción,
 *             la aprobación con efecto en cascada y la agenda de citas
 *             presenciales o por videollamada en Appwrite Cloud.
 * ============================================================================
 */
class ModeloSolicitudes : ViewModel() {

    var listaSolicitudes by mutableStateOf<List<SolicitudAdopcion>>(emptyList())
        private set

    var listaCitas by mutableStateOf<List<Cita>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeRespuesta by mutableStateOf<String?>(null)

    // Alias de compatibilidad
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

    /*
     * RF9: Carga las solicitudes desde la tabla "solicitudes" según el rol del usuario conectado
     */
    fun cargarSolicitudes(usuarioActual: Usuario?) {
        if (usuarioActual == null) return

        viewModelScope.launch {
            estaCargando = true
            listaSolicitudes = if (usuarioActual.rol == RolUsuario.REFUGIO) {
                RepositorioSolicitudesAppwrite.obtenerSolicitudesRefugio(usuarioActual.nombre)
            } else {
                RepositorioSolicitudesAppwrite.obtenerSolicitudesAdoptante(usuarioActual.id)
            }
            cargarCitas(usuarioActual)
            estaCargando = false
        }
    }

    fun loadRequests(currentUser: Usuario?) = cargarSolicitudes(currentUser)

    /*
     * RF20: Carga las citas desde la tabla "citas" de Appwrite Cloud
     */
    fun cargarCitas(usuarioActual: Usuario?) {
        if (usuarioActual == null) return
        viewModelScope.launch {
            listaCitas = RepositorioSolicitudesAppwrite.obtenerCitas(usuarioActual.nombre)
        }
    }

    fun loadAppointments(currentUser: Usuario?) = cargarCitas(currentUser)

    /*
     * RF8: Valida y envía una nueva solicitud de adopción a Appwrite Cloud
     */
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
        val motivoLimpio = motivo.trim()

        // Paso 1: Validar motivo con longitud mínima coherente
        if (motivoLimpio.length < 10) {
            mensajeRespuesta = "Por favor, explica con más detalle por qué deseas adoptar (mínimo 10 caracteres)."
            return
        }

        // Paso 2: Validar aceptación del compromiso responsable
        if (!terminosAceptados) {
            mensajeRespuesta = "Debes aceptar el compromiso de tenencia responsable y seguimiento."
            return
        }

        viewModelScope.launch {
            estaCargando = true
            mensajeRespuesta = null

            // Paso 3: Verificar que no haya enviado ya una solicitud para esta misma mascota
            val duplicada = RepositorioSolicitudesAppwrite.yaExisteSolicitud(idMascota, postulante.id)
            if (duplicada) {
                estaCargando = false
                mensajeRespuesta = "Ya enviaste una solicitud anteriormente para adoptar a $nombreMascota."
                return@launch
            }

            // Paso 4: Construir la solicitud y guardarla en la tabla "solicitudes"
            val solicitud = SolicitudAdopcion(
                petId = idMascota,
                petName = nombreMascota,
                applicantId = postulante.id,
                applicantName = postulante.nombre,
                applicantEmail = postulante.correo,
                shelterName = nombreRefugio,
                reason = motivoLimpio,
                hasYard = tienePatio,
                hasExperience = tieneExperiencia,
                termsAccepted = terminosAceptados,
                status = EstadoSolicitud.PENDIENTE
            )

            val exito = RepositorioSolicitudesAppwrite.crearSolicitud(solicitud)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "¡Solicitud enviada con éxito al refugio $nombreRefugio!"
                contexto?.let { ctx ->
                    AyudanteNotificaciones.showNotification(
                        context = ctx,
                        notificationId = 1001,
                        title = "🐾 Solicitud Registrada",
                        message = "Tu solicitud para adoptar a $nombreMascota fue enviada a $nombreRefugio."
                    )
                }
                alTenerExito()
            } else {
                mensajeRespuesta = "Error al guardar la solicitud en Appwrite Cloud."
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

    /*
     * RF10: Aprueba la solicitud, marca la mascota como ADOPTADA y descarta las demás en cascada
     */
    fun aceptarSolicitud(
        solicitud: SolicitudAdopcion,
        usuarioActual: Usuario?,
        contexto: Context? = null,
        alTenerExito: () -> Unit
    ) {
        viewModelScope.launch {
            estaCargando = true
            val exito = RepositorioSolicitudesAppwrite.aceptarSolicitud(solicitud.id, solicitud.idMascota)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "¡Adopción aprobada! ${solicitud.nombreMascota} pasó a estado ADOPTADA."
                contexto?.let { ctx ->
                    AyudanteNotificaciones.showNotification(
                        context = ctx,
                        notificationId = 1002,
                        title = "🎉 ¡Adopción Aprobada!",
                        message = "Se aprobó la adopción de ${solicitud.nombreMascota} para ${solicitud.nombreSolicitante}."
                    )
                }
                cargarSolicitudes(usuarioActual)
                alTenerExito()
            } else {
                mensajeRespuesta = "No se pudo aprobar la solicitud en la nube."
            }
        }
    }

    fun acceptRequest(request: SolicitudAdopcion, currentUser: Usuario?, context: Context? = null, onSuccess: () -> Unit) =
        aceptarSolicitud(request, currentUser, context, onSuccess)

    /*
     * RF10: Rechaza una solicitud de adopción
     */
    fun rechazarSolicitud(idSolicitud: String, usuarioActual: Usuario?) {
        viewModelScope.launch {
            estaCargando = true
            val exito = RepositorioSolicitudesAppwrite.rechazarSolicitud(idSolicitud)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "Solicitud rechazada correctamente."
                cargarSolicitudes(usuarioActual)
            } else {
                mensajeRespuesta = "Error al rechazar la solicitud."
            }
        }
    }

    fun rejectRequest(requestId: String, currentUser: Usuario?) = rechazarSolicitud(requestId, currentUser)

    /*
     * RF20: Agenda una cita presencial o virtual en la tabla "citas" de Appwrite Cloud
     */
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
        val fechaLimpia = fecha.trim()
        val horaLimpia = hora.trim()

        if (fechaLimpia.isEmpty() || horaLimpia.isEmpty()) {
            mensajeRespuesta = "Por favor, selecciona la fecha y la hora de la entrevista."
            return
        }

        viewModelScope.launch {
            estaCargando = true
            val nuevaCita = Cita(
                requestId = idSolicitud,
                petName = nombreMascota,
                applicantName = nombrePostulante,
                shelterName = nombreRefugio,
                date = fechaLimpia,
                time = horaLimpia,
                modality = modalidad,
                status = EstadoCita.PROPUESTA
            )

            val exito = RepositorioSolicitudesAppwrite.crearCita(nuevaCita)
            estaCargando = false

            if (exito) {
                mensajeRespuesta = "Cita agendada con éxito para el $fechaLimpia a las $horaLimpia."
                contexto?.let { ctx ->
                    AyudanteNotificaciones.showNotification(
                        context = ctx,
                        notificationId = 1003,
                        title = "📅 Cita Programada",
                        message = "Entrevista ($modalidad) para $nombreMascota el $fechaLimpia a las $horaLimpia."
                    )
                    AyudanteNotificaciones.scheduleAppointmentAlarm(
                        context = ctx,
                        appointmentId = System.currentTimeMillis().toString(),
                        petName = nombreMascota,
                        shelterName = nombreRefugio,
                        date = fechaLimpia,
                        time = horaLimpia
                    )
                }
                cargarCitas(usuarioActual)
                alTenerExito()
            } else {
                mensajeRespuesta = "Error al guardar la cita en Appwrite Cloud."
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

    /*
     * RF20: El adoptante confirma su asistencia a la cita propuesta
     */
    fun confirmarCita(idCita: String, usuarioActual: Usuario?) {
        viewModelScope.launch {
            val exito = RepositorioSolicitudesAppwrite.actualizarEstadoCita(idCita, EstadoCita.CONFIRMADA)
            if (exito) {
                mensajeRespuesta = "¡Cita confirmada con éxito!"
            }
            cargarCitas(usuarioActual)
        }
    }

    fun confirmAppointment(appointmentId: String, currentUser: Usuario?) = confirmarCita(appointmentId, currentUser)

    /*
     * RF20: Permite solicitar reprogramación de una cita proponiendo nueva fecha y hora
     */
    fun reprogramarCita(
        idCita: String,
        nuevaFecha: String,
        nuevaHora: String,
        usuarioActual: Usuario?
    ) {
        viewModelScope.launch {
            val exito = RepositorioSolicitudesAppwrite.actualizarEstadoCita(
                idCita = idCita,
                nuevoEstado = EstadoCita.REPROGRAMAR,
                nuevaFecha = nuevaFecha,
                nuevaHora = nuevaHora
            )
            if (exito) {
                mensajeRespuesta = "Se actualizó la cita a nueva fecha: $nuevaFecha ($nuevaHora)."
            }
            cargarCitas(usuarioActual)
        }
    }
}

typealias RequestsViewModel = ModeloSolicitudes
