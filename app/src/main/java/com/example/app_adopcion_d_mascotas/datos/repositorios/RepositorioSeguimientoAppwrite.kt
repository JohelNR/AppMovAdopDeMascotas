package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.PetFollowUp
import com.example.app_adopcion_d_mascotas.datos.modelos.SeguimientoMascota
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RepositorioSeguimientoAppwrite {

    // Caché en memoria para garantizar funcionamiento instantáneo
    private val localFollowUps = mutableListOf<SeguimientoMascota>()

    // RF11: Enviar formulario de seguimiento post-adopción
    suspend fun createFollowUp(followUp: SeguimientoMascota): Boolean = withContext(Dispatchers.IO) {
        localFollowUps.add(0, followUp)
        try {
            val data = mapOf(
                "petId" to followUp.petId,
                "petName" to followUp.petName,
                "applicantId" to followUp.applicantId,
                "applicantName" to followUp.applicantName,
                "shelterName" to followUp.shelterName,
                "date" to followUp.date,
                "healthNotes" to followUp.healthNotes,
                "adaptationNotes" to followUp.adaptationNotes,
                "photos" to followUp.photos,
                "status" to followUp.status
            )
            ClienteAppwrite.databases.createDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.FOLLOWUPS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            true
        }
    }

    suspend fun crearSeguimiento(seguimiento: SeguimientoMascota): Boolean = createFollowUp(seguimiento)

    // RF11: Listar historial de seguimiento de una mascota
    suspend fun getFollowUps(petId: String): List<SeguimientoMascota> = withContext(Dispatchers.IO) {
        try {
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.FOLLOWUPS_COLLECTION,
                queries = listOf(
                    Query.equal("petId", petId),
                    Query.orderDesc("\$createdAt")
                )
            )
            val cloudList = response.documents.map { doc ->
                val data = doc.data
                val photosList = (data["photos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                SeguimientoMascota(
                    id = doc.id,
                    petId = data["petId"]?.toString() ?: "",
                    petName = data["petName"]?.toString() ?: "",
                    applicantName = data["applicantName"]?.toString() ?: "",
                    shelterName = data["shelterName"]?.toString() ?: "",
                    date = data["date"]?.toString() ?: "",
                    healthNotes = data["healthNotes"]?.toString() ?: "",
                    adaptationNotes = data["adaptationNotes"]?.toString() ?: "",
                    photos = photosList,
                    status = data["status"]?.toString() ?: "ENVIADO"
                )
            }
            if (cloudList.isNotEmpty()) cloudList else localFollowUps.filter { it.petId == petId }
        } catch (e: Exception) {
            localFollowUps.filter { it.petId == petId }
        }
    }

    suspend fun obtenerSeguimientos(idMascota: String): List<SeguimientoMascota> = getFollowUps(idMascota)
}

typealias AppwriteFollowUpRepository = RepositorioSeguimientoAppwrite
