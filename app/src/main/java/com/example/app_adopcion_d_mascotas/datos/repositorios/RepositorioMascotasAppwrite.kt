package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.PetStatus
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document

class RepositorioMascotasAppwrite(private val ClienteAppwrite: ClienteAppwrite) {

    suspend fun getPets(): List<Mascota> {
        return try {
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.PETS_COLLECTION,
                queries = listOf(
                    Query.orderDesc("\$createdAt")
                )
            )
            response.documents.map { docToPet(it) }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun obtenerMascotas(): List<Mascota> = getPets()

    suspend fun createPet(pet: Mascota): Boolean {
        return try {
            // Claves exactas de Appwrite Cloud en internet
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
                "photos" to pet.photos,
                "latitude" to pet.latitude,
                "longitude" to pet.longitude
            )
            ClienteAppwrite.databases.createDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.PETS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun crearMascota(mascota: Mascota): Boolean = createPet(mascota)

    suspend fun updatePet(pet: Mascota): Boolean {
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
                "photos" to pet.photos,
                "latitude" to pet.latitude,
                "longitude" to pet.longitude
            )
            ClienteAppwrite.databases.updateDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.PETS_COLLECTION,
                documentId = pet.id,
                data = data
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun actualizarMascota(mascota: Mascota): Boolean = updatePet(mascota)

    suspend fun deletePet(petId: String): Boolean {
        return try {
            ClienteAppwrite.databases.deleteDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.PETS_COLLECTION,
                documentId = petId
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun eliminarMascota(idMascota: String): Boolean = deletePet(idMascota)

    suspend fun markAsAdopted(petId: String): Boolean {
        return try {
            ClienteAppwrite.databases.updateDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.PETS_COLLECTION,
                documentId = petId,
                data = mapOf("status" to EstadoMascota.ADOPTADA.name)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun marcarComoAdoptada(idMascota: String): Boolean = markAsAdopted(idMascota)

    private fun docToPet(doc: Document<Map<String, Any>>): Mascota {
        val data = doc.data
        val photosList = (data["photos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val statusString = data["status"]?.toString() ?: "DISPONIBLE"
        val petStatus = try {
            EstadoMascota.valueOf(statusString)
        } catch (e: Exception) {
            EstadoMascota.DISPONIBLE
        }

        return Mascota(
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
            photos = photosList,
            latitude = (data["latitude"] as? Number)?.toDouble() ?: -12.0464,
            longitude = (data["longitude"] as? Number)?.toDouble() ?: -77.0428
        )
    }
}

typealias AppwritePetRepository = RepositorioMascotasAppwrite
