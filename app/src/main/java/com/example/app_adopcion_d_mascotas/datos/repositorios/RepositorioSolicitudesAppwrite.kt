package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.Cita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoCita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoSolicitud
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 * ARCHIVO: RepositorioSolicitudesAppwrite.kt
 * CAPA: Datos -> Repositorios (RF8, RF9, RF10 y RF20)
 * ¿QUÉ HACE?: Gestiona las tablas "solicitudes" y "citas" en Appwrite Cloud
 *             con todas las columnas en español:
 *             - Tabla "solicitudes": idMascota, nombreMascota, idSolicitante,
 *               nombreSolicitante, correoSolicitante, nombreRefugio, motivo,
 *               tienePatio, tieneExperiencia, aceptaTerminos, estado.
 *             - Tabla "citas": idSolicitud, nombreMascota, nombreSolicitante,
 *               nombreRefugio, fecha, hora, modalidad, estado.
 * ============================================================================
 */
object RepositorioSolicitudesAppwrite {

    /*
     * RF8: Verifica si el adoptante ya envió antes una solicitud para la misma mascota
     * para evitar solicitudes duplicadas.
     */
    suspend fun yaExisteSolicitud(idMascota: String, idSolicitante: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                queries = listOf(
                    Query.equal("idMascota", idMascota),
                    Query.equal("idSolicitante", idSolicitante)
                )
            )
            respuesta.documents.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    /*
     * RF8: Enviar una nueva solicitud de adopción a la tabla "solicitudes".
     */
    suspend fun crearSolicitud(solicitud: SolicitudAdopcion): Boolean = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Preparar los datos con los nombres de columna en español
            val datosSolicitud = mapOf(
                "idMascota" to solicitud.idMascota,
                "nombreMascota" to solicitud.nombreMascota,
                "idSolicitante" to solicitud.idSolicitante,
                "nombreSolicitante" to solicitud.nombreSolicitante,
                "correoSolicitante" to solicitud.correoSolicitante,
                "nombreRefugio" to solicitud.nombreRefugio,
                "motivo" to solicitud.motivo.trim(),
                "tienePatio" to solicitud.tienePatio,
                "tieneExperiencia" to solicitud.tieneExperiencia,
                "aceptaTerminos" to solicitud.aceptaTerminos,
                "estado" to solicitud.estado.name
            )

            // Paso 2: Crear el documento en Appwrite Cloud
            ClienteAppwrite.baseDeDatos.createDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                documentId = ID.unique(),
                data = datosSolicitud
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun createRequest(request: SolicitudAdopcion): Boolean = crearSolicitud(request)

    /*
     * RF9: Obtener las solicitudes recibidas por un Refugio desde la nube.
     */
    suspend fun obtenerSolicitudesRefugio(nombreRefugio: String): List<SolicitudAdopcion> = withContext(Dispatchers.IO) {
        try {
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                queries = listOf(Query.orderDesc("\$createdAt"))
            )
            val todas = respuesta.documents.map { documentoASolicitud(it) }
            val refugioLimpio = nombreRefugio.trim()
            if (refugioLimpio.isNotBlank()) {
                val filtradas = todas.filter {
                    it.nombreRefugio.trim().equals(refugioLimpio, ignoreCase = true) ||
                        refugioLimpio.contains(it.nombreRefugio.trim(), ignoreCase = true) ||
                        it.nombreRefugio.trim().contains(refugioLimpio, ignoreCase = true)
                }
                if (filtradas.isNotEmpty()) filtradas else todas
            } else {
                todas
            }
        } catch (error: Exception) {
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRequestsForShelter(shelterName: String): List<SolicitudAdopcion> =
        obtenerSolicitudesRefugio(shelterName)

    /*
     * RF9: Obtener las solicitudes enviadas por un Adoptante específico.
     */
    suspend fun obtenerSolicitudesAdoptante(idAdoptante: String): List<SolicitudAdopcion> = withContext(Dispatchers.IO) {
        try {
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                queries = listOf(
                    Query.equal("idSolicitante", idAdoptante),
                    Query.orderDesc("\$createdAt")
                )
            )
            respuesta.documents.map { documentoASolicitud(it) }
        } catch (error: Exception) {
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRequestsForApplicant(applicantId: String): List<SolicitudAdopcion> =
        obtenerSolicitudesAdoptante(applicantId)

    /*
     * RF10: Aprobar solicitud con EFECTO EN CASCADA AUTOMÁTICO:
     * 1. Marca la solicitud elegida como ACEPTADA.
     * 2. Cambia el estado de la mascota a ADOPTADA en la tabla "mascotas".
     * 3. Cambia todas las demás solicitudes pendientes de esa misma mascota a NO_DISPONIBLE.
     */
    suspend fun aceptarSolicitud(idSolicitud: String, idMascota: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Marcar esta solicitud como ACEPTADA
            ClienteAppwrite.baseDeDatos.updateDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                documentId = idSolicitud,
                data = mapOf("estado" to EstadoSolicitud.ACEPTADA.name)
            )

            // Paso 2: Marcar la mascota como ADOPTADA para que ya no aparezca disponible
            RepositorioMascotasAppwrite.marcarComoAdoptada(idMascota)

            // Paso 3: Buscar otras solicitudes pendientes de esta misma mascota y cerrarlas en cascada
            val otrasPendientes = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                queries = listOf(
                    Query.equal("idMascota", idMascota),
                    Query.equal("estado", EstadoSolicitud.PENDIENTE.name)
                )
            )

            for (doc in otrasPendientes.documents) {
                if (doc.id != idSolicitud) {
                    ClienteAppwrite.baseDeDatos.updateDocument(
                        databaseId = ClienteAppwrite.ID_BASE_DATOS,
                        collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                        documentId = doc.id,
                        data = mapOf("estado" to EstadoSolicitud.NO_DISPONIBLE.name)
                    )
                }
            }
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun acceptRequest(requestId: String, petId: String): Boolean =
        aceptarSolicitud(requestId, petId)

    /*
     * RF10: Rechazar una solicitud de adopción manualmente.
     */
    suspend fun rechazarSolicitud(idSolicitud: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.baseDeDatos.updateDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SOLICITUDES,
                documentId = idSolicitud,
                data = mapOf("estado" to EstadoSolicitud.RECHAZADA.name)
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun rejectRequest(requestId: String): Boolean = rechazarSolicitud(requestId)

    /*
     * RF20: Agendar una cita o entrevista en la tabla "citas" de Appwrite Cloud.
     */
    suspend fun crearCita(cita: Cita): Boolean = withContext(Dispatchers.IO) {
        try {
            val datosCita = mapOf(
                "idSolicitud" to cita.idSolicitud,
                "nombreMascota" to cita.nombreMascota,
                "nombreSolicitante" to cita.nombreSolicitante,
                "nombreRefugio" to cita.nombreRefugio,
                "fecha" to cita.fecha,
                "hora" to cita.hora,
                "modalidad" to cita.modalidad,
                "estado" to cita.estado.name
            )
            ClienteAppwrite.baseDeDatos.createDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_CITAS,
                documentId = ID.unique(),
                data = datosCita
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun createAppointment(appointment: Cita): Boolean = crearCita(appointment)

    /*
     * RF20: Obtener las citas programadas de un refugio o de un adoptante.
     */
    suspend fun obtenerCitas(nombreUsuario: String): List<Cita> = withContext(Dispatchers.IO) {
        try {
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_CITAS,
                queries = listOf(Query.orderDesc("\$createdAt"))
            )
            val todas = respuesta.documents.map { documentoACita(it) }
            val nombreLimpio = nombreUsuario.trim()
            if (nombreLimpio.isNotBlank()) {
                val filtradas = todas.filter {
                    it.nombreRefugio.trim().equals(nombreLimpio, ignoreCase = true) ||
                        it.nombreSolicitante.trim().equals(nombreLimpio, ignoreCase = true)
                }
                if (filtradas.isNotEmpty()) filtradas else todas
            } else {
                todas
            }
        } catch (error: Exception) {
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAppointments(shelterOrApplicantName: String): List<Cita> =
        obtenerCitas(shelterOrApplicantName)

    /*
     * RF20: Cambiar el estado de una cita (CONFIRMADA, REPROGRAMAR o RECHAZADA) y opcionalmente su fecha/hora.
     */
    suspend fun actualizarEstadoCita(
        idCita: String,
        nuevoEstado: EstadoCita,
        nuevaFecha: String? = null,
        nuevaHora: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val datos = mutableMapOf<String, Any>("estado" to nuevoEstado.name)
            if (!nuevaFecha.isNullOrBlank()) datos["fecha"] = nuevaFecha.trim()
            if (!nuevaHora.isNullOrBlank()) datos["hora"] = nuevaHora.trim()

            ClienteAppwrite.baseDeDatos.updateDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_CITAS,
                documentId = idCita,
                data = datos
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: EstadoCita): Boolean =
        actualizarEstadoCita(appointmentId, newStatus)

    /*
     * Traduce un documento de la tabla "solicitudes" al modelo SolicitudAdopcion.
     */
    private fun documentoASolicitud(documento: Document<Map<String, Any>>): SolicitudAdopcion {
        val datos = documento.data
        val textoEstado = datos["estado"]?.toString() ?: "PENDIENTE"
        val estado = try {
            EstadoSolicitud.valueOf(textoEstado)
        } catch (_: Exception) {
            EstadoSolicitud.PENDIENTE
        }

        return SolicitudAdopcion(
            id = documento.id,
            petId = datos["idMascota"]?.toString() ?: "",
            petName = datos["nombreMascota"]?.toString() ?: "",
            applicantId = datos["idSolicitante"]?.toString() ?: "",
            applicantName = datos["nombreSolicitante"]?.toString() ?: "",
            applicantEmail = datos["correoSolicitante"]?.toString() ?: "",
            shelterName = datos["nombreRefugio"]?.toString() ?: "",
            reason = datos["motivo"]?.toString() ?: "",
            hasYard = datos["tienePatio"] as? Boolean ?: false,
            hasExperience = datos["tieneExperiencia"] as? Boolean ?: false,
            termsAccepted = datos["aceptaTerminos"] as? Boolean ?: false,
            status = estado
        )
    }

    /*
     * Traduce un documento de la tabla "citas" al modelo Cita.
     */
    private fun documentoACita(documento: Document<Map<String, Any>>): Cita {
        val datos = documento.data
        val textoEstado = datos["estado"]?.toString() ?: "PROPUESTA"
        val estado = try {
            EstadoCita.valueOf(textoEstado)
        } catch (_: Exception) {
            EstadoCita.PROPUESTA
        }

        return Cita(
            id = documento.id,
            requestId = datos["idSolicitud"]?.toString() ?: "",
            petName = datos["nombreMascota"]?.toString() ?: "",
            applicantName = datos["nombreSolicitante"]?.toString() ?: "",
            shelterName = datos["nombreRefugio"]?.toString() ?: "",
            date = datos["fecha"]?.toString() ?: "",
            time = datos["hora"]?.toString() ?: "",
            modality = datos["modalidad"]?.toString() ?: "Presencial",
            status = estado
        )
    }
}

typealias AppwriteRequestsRepository = RepositorioSolicitudesAppwrite
