package com.example.app_adopcion_d_mascotas.data.repository

import com.example.app_adopcion_d_mascotas.data.model.PetFollowUp
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppwriteFollowUpRepository {

    // Caché en memoria para garantizar funcionamiento instantáneo
    private val localFollowUps = mutableListOf<PetFollowUp>()

    // RF11: Enviar formulario de seguimiento post-adopción
    suspend fun createFollowUp(followUp: PetFollowUp): Boolean = withContext(Dispatchers.IO) {
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
            AppwriteClient.databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.FOLLOWUPS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            // Si la colección no está creada en la nube, el reporte queda guardado en la sesión
            true
        }
    }

    // RF11: Listar historial de seguimiento de una mascota
    suspend fun getFollowUps(petId: String): List<PetFollowUp> = withContext(Dispatchers.IO) {
        try {
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.FOLLOWUPS_COLLECTION,
                queries = listOf(
                    Query.equal("petId", petId),
                    Query.orderDesc("\$createdAt")
                )
            )
            val cloudList = response.documents.map { doc ->
                val data = doc.data
                val photosList = (data["photos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                PetFollowUp(
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
}
