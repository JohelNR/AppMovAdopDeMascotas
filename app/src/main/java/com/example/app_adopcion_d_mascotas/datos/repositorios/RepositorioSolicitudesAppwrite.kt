package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.AdoptionRequest
import com.example.app_adopcion_d_mascotas.datos.modelos.Appointment
import com.example.app_adopcion_d_mascotas.datos.modelos.AppointmentStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.Cita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoCita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoSolicitud
import com.example.app_adopcion_d_mascotas.datos.modelos.RequestStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioSolicitudesAppwrite(
    private val ClienteAppwrite: ClienteAppwrite,
    private val RepositorioMascotasAppwrite: RepositorioMascotasAppwrite
) {

    // RF8: Enviar formulario de solicitud
    suspend fun createRequest(request: SolicitudAdopcion): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "petId" to request.petId,
                "petName" to request.petName,
                "applicantId" to request.applicantId,
                "applicantName" to request.applicantName,
                "applicantEmail" to request.applicantEmail,
                "shelterNames" to request.shelterName, // Clave de Appwrite
                "reason" to request.reason,
                "hasYard" to request.hasYard,
                "hasExperience" to request.hasExperience,
                "termsAccepted" to request.termsAccepted,
                "status" to request.status.name
            )
            ClienteAppwrite.databases.createDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun crearSolicitud(solicitud: SolicitudAdopcion): Boolean = createRequest(solicitud)

    // RF9: Listar solicitudes recibidas para el refugio
    suspend fun getRequestsForShelter(shelterName: String): List<SolicitudAdopcion> = withContext(Dispatchers.IO) {
        try {
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                queries = listOf(
                    Query.orderDesc("\$createdAt")
                )
            )
            val cleanTarget = shelterName.trim()
            val allRequests = response.documents.map { docToRequest(it) }
            if (cleanTarget.isNotBlank()) {
                val filtered = allRequests.filter {
                    it.shelterName.trim().equals(cleanTarget, ignoreCase = true)
                }
                filtered
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerSolicitudesRefugio(nombreRefugio: String): List<SolicitudAdopcion> = getRequestsForShelter(nombreRefugio)

    // Listar solicitudes del adoptante
    suspend fun getRequestsForApplicant(applicantId: String): List<SolicitudAdopcion> = withContext(Dispatchers.IO) {
        try {
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                queries = listOf(
                    Query.equal("applicantId", applicantId),
                    Query.orderDesc("\$createdAt")
                )
            )
            response.documents.map { docToRequest(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerSolicitudesAdoptante(idAdoptante: String): List<SolicitudAdopcion> = getRequestsForApplicant(idAdoptante)

    // RF10: Aceptar solicitud -> Cierre automático de la mascota y descarte de pendientes
    suspend fun acceptRequest(requestId: String, petId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Marcar la solicitud elegida como ACEPTADA
            ClienteAppwrite.databases.updateDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                documentId = requestId,
                data = mapOf("status" to EstadoSolicitud.ACEPTADA.name)
            )

            // 2. Marcar la mascota como ADOPTADA (sale del catálogo)
            check(RepositorioMascotasAppwrite.markAsAdopted(petId)) { "No se pudo actualizar la mascota" }

            // 3. Buscar todas las demás solicitudes de esta misma mascota y ponerlas NO_DISPONIBLE
            val otherRequests = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                queries = listOf(
                    Query.equal("petId", petId),
                    Query.equal("status", EstadoSolicitud.PENDIENTE.name)
                )
            )

            for (doc in otherRequests.documents) {
                if (doc.id != requestId) {
                    ClienteAppwrite.databases.updateDocument(
                        databaseId = ClienteAppwrite.DATABASE_ID,
                        collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                        documentId = doc.id,
                        data = mapOf("status" to EstadoSolicitud.NO_DISPONIBLE.name)
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun aceptarSolicitud(idSolicitud: String, idMascota: String): Boolean = acceptRequest(idSolicitud, idMascota)

    // Rechazar solicitud
    suspend fun rejectRequest(requestId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.databases.updateDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.REQUESTS_COLLECTION,
                documentId = requestId,
                data = mapOf("status" to EstadoSolicitud.RECHAZADA.name)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun rechazarSolicitud(idSolicitud: String): Boolean = rejectRequest(idSolicitud)

    // RF20: Crear cita / entrevista
    suspend fun createAppointment(appointment: Cita): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "requestId" to appointment.requestId,
                "petName" to appointment.petName,
                "applicantName" to appointment.applicantName,
                "shelterName" to appointment.shelterName,
                "date" to appointment.date,
                "time" to appointment.time,
                "modality" to appointment.modality,
                "status" to appointment.status.name
            )
            ClienteAppwrite.databases.createDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.APPOINTMENTS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun crearCita(cita: Cita): Boolean = createAppointment(cita)

    // Listar citas para un refugio o adoptante
    suspend fun getAppointments(shelterOrApplicantName: String): List<Cita> = withContext(Dispatchers.IO) {
        try {
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.APPOINTMENTS_COLLECTION,
                queries = listOf(Query.orderDesc("\$createdAt"))
            )
            response.documents.map { docToAppointment(it) }.filter {
                it.shelterName.equals(shelterOrApplicantName, ignoreCase = true) ||
                it.applicantName.equals(shelterOrApplicantName, ignoreCase = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerCitas(nombreRefugioOPostulante: String): List<Cita> = getAppointments(nombreRefugioOPostulante)

    // Actualizar estado de la cita
    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: EstadoCita): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.databases.updateDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.APPOINTMENTS_COLLECTION,
                documentId = appointmentId,
                data = mapOf("status" to newStatus.name)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun actualizarEstadoCita(idCita: String, nuevoEstado: EstadoCita): Boolean = updateAppointmentStatus(idCita, nuevoEstado)

    private fun docToRequest(doc: Document<Map<String, Any>>): SolicitudAdopcion {
        val data = doc.data
        val statusStr = data["status"]?.toString() ?: "PENDIENTE"
        val status = try { EstadoSolicitud.valueOf(statusStr) } catch (_: Exception) { EstadoSolicitud.PENDIENTE }
        val shelter = data["shelterNames"]?.toString() ?: data["shelterName"]?.toString() ?: ""

        return SolicitudAdopcion(
            id = doc.id,
            petId = data["petId"]?.toString() ?: "",
            petName = data["petName"]?.toString() ?: "",
            applicantId = data["applicantId"]?.toString() ?: "",
            applicantName = data["applicantName"]?.toString() ?: "",
            applicantEmail = data["applicantEmail"]?.toString() ?: "",
            shelterName = shelter,
            reason = data["reason"]?.toString() ?: "",
            hasYard = data["hasYard"] as? Boolean ?: false,
            hasExperience = data["hasExperience"] as? Boolean ?: false,
            termsAccepted = data["termsAccepted"] as? Boolean ?: false,
            status = status
        )
    }

    private fun docToAppointment(doc: Document<Map<String, Any>>): Cita {
        val data = doc.data
        val statusStr = data["status"]?.toString() ?: "PROPUESTA"
        val status = try { EstadoCita.valueOf(statusStr) } catch (_: Exception) { EstadoCita.PROPUESTA }

        return Cita(
            id = doc.id,
            requestId = data["requestId"]?.toString() ?: "",
            petName = data["petName"]?.toString() ?: "",
            applicantName = data["applicantName"]?.toString() ?: "",
            shelterName = data["shelterName"]?.toString() ?: "",
            date = data["date"]?.toString() ?: "",
            time = data["time"]?.toString() ?: "",
            modality = data["modality"]?.toString() ?: "Presencial",
            status = status
        )
    }
}

typealias AppwriteRequestsRepository = RepositorioSolicitudesAppwrite
