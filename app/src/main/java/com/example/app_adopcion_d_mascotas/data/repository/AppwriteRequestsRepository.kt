package com.example.app_adopcion_d_mascotas.data.repository

import com.example.app_adopcion_d_mascotas.data.model.AdoptionRequest
import com.example.app_adopcion_d_mascotas.data.model.Appointment
import com.example.app_adopcion_d_mascotas.data.model.AppointmentStatus
import com.example.app_adopcion_d_mascotas.data.model.PetStatus
import com.example.app_adopcion_d_mascotas.data.model.RequestStatus
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppwriteRequestsRepository {

    // RF8: Enviar formulario de solicitud
    suspend fun createRequest(request: AdoptionRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "petId" to request.petId,
                "petName" to request.petName,
                "applicantId" to request.applicantId,
                "applicantName" to request.applicantName,
                "applicantEmail" to request.applicantEmail,
                "shelterNames" to request.shelterName, // El campo en Appwrite se nombró shelterNames
                "reason" to request.reason,
                "hasYard" to request.hasYard,
                "hasExperience" to request.hasExperience,
                "termsAccepted" to request.termsAccepted,
                "status" to request.status.name
            )
            AppwriteClient.databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.REQUESTS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // RF9: Listar solicitudes recibidas para el refugio
    suspend fun getRequestsForShelter(shelterName: String): List<AdoptionRequest> = withContext(Dispatchers.IO) {
        try {
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.REQUESTS_COLLECTION,
                queries = listOf(
                    Query.equal("shelterNames", shelterName),
                    Query.orderDesc("\$createdAt")
                )
            )
            response.documents.map { docToRequest(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Listar solicitudes del adoptante
    suspend fun getRequestsForApplicant(applicantId: String): List<AdoptionRequest> = withContext(Dispatchers.IO) {
        try {
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.REQUESTS_COLLECTION,
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

    // RF10: Aceptar solicitud -> Cierre automático de la mascota y descarte de pendientes
    suspend fun acceptRequest(requestId: String, petId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Marcar la solicitud elegida como ACEPTADA
            AppwriteClient.databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.REQUESTS_COLLECTION,
                documentId = requestId,
                data = mapOf("status" to RequestStatus.ACEPTADA.name)
            )

            // 2. Marcar la mascota como ADOPTADA (sale del catálogo)
            AppwritePetRepository.markAsAdopted(petId)

            // 3. Buscar todas las demás solicitudes de esta misma mascota y ponerlas NO_DISPONIBLE
            val otherRequests = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.REQUESTS_COLLECTION,
                queries = listOf(
                    Query.equal("petId", petId),
                    Query.equal("status", RequestStatus.PENDIENTE.name)
                )
            )

            for (doc in otherRequests.documents) {
                if (doc.id != requestId) {
                    AppwriteClient.databases.updateDocument(
                        databaseId = AppwriteClient.DATABASE_ID,
                        collectionId = AppwriteClient.REQUESTS_COLLECTION,
                        documentId = doc.id,
                        data = mapOf("status" to RequestStatus.NO_DISPONIBLE.name)
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Rechazar solicitud
    suspend fun rejectRequest(requestId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            AppwriteClient.databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.REQUESTS_COLLECTION,
                documentId = requestId,
                data = mapOf("status" to RequestStatus.RECHAZADA.name)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // RF20: Crear cita / entrevista
    suspend fun createAppointment(appointment: Appointment): Boolean = withContext(Dispatchers.IO) {
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
            AppwriteClient.databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.APPOINTMENTS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Listar citas para un refugio o adoptante
    suspend fun getAppointments(shelterOrApplicantName: String): List<Appointment> = withContext(Dispatchers.IO) {
        try {
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.APPOINTMENTS_COLLECTION,
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

    // Actualizar estado de la cita
    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus): Boolean = withContext(Dispatchers.IO) {
        try {
            AppwriteClient.databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.APPOINTMENTS_COLLECTION,
                documentId = appointmentId,
                data = mapOf("status" to newStatus.name)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun docToRequest(doc: Document<Map<String, Any>>): AdoptionRequest {
        val data = doc.data
        val statusStr = data["status"]?.toString() ?: "PENDIENTE"
        val status = try { RequestStatus.valueOf(statusStr) } catch (_: Exception) { RequestStatus.PENDIENTE }
        val shelter = data["shelterNames"]?.toString() ?: data["shelterName"]?.toString() ?: ""

        return AdoptionRequest(
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

    private fun docToAppointment(doc: Document<Map<String, Any>>): Appointment {
        val data = doc.data
        val statusStr = data["status"]?.toString() ?: "PROPUESTA"
        val status = try { AppointmentStatus.valueOf(statusStr) } catch (_: Exception) { AppointmentStatus.PROPUESTA }

        return Appointment(
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
