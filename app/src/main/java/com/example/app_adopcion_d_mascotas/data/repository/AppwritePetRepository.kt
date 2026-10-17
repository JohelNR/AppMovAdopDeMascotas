package com.example.app_adopcion_d_mascotas.data.repository

import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetStatus
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document

object AppwritePetRepository {

    suspend fun getPets(): List<Pet> {
        return try {
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.PETS_COLLECTION,
                queries = listOf(
                    Query.orderDesc("\$createdAt")
                )
            )
            response.documents.map { docToPet(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createPet(pet: Pet): Boolean {
        return try {
            val data = mapOf(
                "name" to pet.name,
                "breed" to pet.breed,
                "species" to pet.species,
                "ageYears" to pet.ageYears,
                "description" to pet.description,
                "shelterName" to pet.shelterName,
                "isUrgent" to pet.isUrgent,
                "status" to pet.status.name,
                "gender" to pet.gender,
                "size" to pet.size,
                "location" to pet.location,
                "photos" to pet.photos
            )
            AppwriteClient.databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.PETS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePet(pet: Pet): Boolean {
        return try {
            val data = mapOf(
                "name" to pet.name,
                "breed" to pet.breed,
                "species" to pet.species,
                "ageYears" to pet.ageYears,
                "description" to pet.description,
                "shelterName" to pet.shelterName,
                "isUrgent" to pet.isUrgent,
                "status" to pet.status.name,
                "gender" to pet.gender,
                "size" to pet.size,
                "location" to pet.location,
                "photos" to pet.photos
            )
            AppwriteClient.databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.PETS_COLLECTION,
                documentId = pet.id,
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletePet(petId: String): Boolean {
        return try {
            AppwriteClient.databases.deleteDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.PETS_COLLECTION,
                documentId = petId
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun markAsAdopted(petId: String): Boolean {
        return try {
            AppwriteClient.databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.PETS_COLLECTION,
                documentId = petId,
                data = mapOf("status" to PetStatus.ADOPTADA.name)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun docToPet(doc: Document<Map<String, Any>>): Pet {
        val data = doc.data
        val photosList = (data["photos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val statusString = data["status"]?.toString() ?: "DISPONIBLE"
        val petStatus = try {
            PetStatus.valueOf(statusString)
        } catch (e: Exception) {
            PetStatus.DISPONIBLE
        }

        return Pet(
            id = doc.id,
            name = data["name"]?.toString() ?: "",
            breed = data["breed"]?.toString() ?: "",
            species = data["species"]?.toString() ?: "Perro",
            ageYears = (data["ageYears"] as? Number)?.toInt() ?: 1,
            description = data["description"]?.toString() ?: "",
            shelterName = data["shelterName"]?.toString() ?: "",
            isUrgent = data["isUrgent"] as? Boolean ?: false,
            status = petStatus,
            gender = data["gender"]?.toString() ?: "Macho",
            size = data["size"]?.toString() ?: "Mediano",
            location = data["location"]?.toString() ?: "Lima",
            photos = photosList
        )
    }
}
