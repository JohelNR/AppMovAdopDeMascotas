package com.example.app_adopcion_d_mascotas.data.repository

import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

object AppwriteAuthRepository {

    val currentUser = MutableStateFlow<User?>(null)

    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        try {
            val accountUser = AppwriteClient.account.get()
            // Buscamos su perfil y rol en la tabla de usuarios
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.USERS_COLLECTION,
                queries = listOf(Query.equal("userId", accountUser.id))
            )
            val doc = response.documents.firstOrNull()
            val data = doc?.data ?: emptyMap()
            val roleStr = data["role"]?.toString() ?: "ADOPTANTE"
            val userRole = if (roleStr.equals("REFUGIO", ignoreCase = true)) UserRole.REFUGIO else UserRole.ADOPTANTE

            val user = User(
                id = accountUser.id,
                name = accountUser.name.ifBlank { data["name"]?.toString() ?: "Usuario" },
                email = accountUser.email,
                role = userRole,
                phone = data["phone"]?.toString() ?: "",
                bio = data["bio"]?.toString() ?: "",
                avatarPhotoId = data["avatarPhotoId"]?.toString() ?: ""
            )
            currentUser.value = user
            user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            try { AppwriteClient.account.deleteSession("current") } catch (_: Exception) {}

            AppwriteClient.account.createEmailPasswordSession(email, password)
            val user = getCurrentUser()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("No se pudo obtener el perfil del usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = ID.unique()
            AppwriteClient.account.create(
                userId = userId,
                email = email,
                password = password,
                name = name
            )

            // Iniciamos sesión
            AppwriteClient.account.createEmailPasswordSession(email, password)

            // Guardamos el perfil con su rol en la tabla de usuarios
            AppwriteClient.databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.USERS_COLLECTION,
                documentId = ID.unique(),
                data = mapOf(
                    "userId" to userId,
                    "name" to name.trim(),
                    "email" to email.trim(),
                    "role" to role.name,
                    "phone" to "",
                    "bio" to "",
                    "avatarPhotoId" to ""
                )
            )

            val createdUser = User(
                id = userId,
                name = name.trim(),
                email = email.trim(),
                role = role
            )
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // RF2: Actualización de datos de perfil (nombre, teléfono, biografía, foto avatar)
    suspend fun updateProfile(
        userId: String,
        name: String,
        phone: String,
        bio: String,
        avatarPhotoId: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Actualizamos en la cuenta de Appwrite
            try {
                AppwriteClient.account.updateName(name.trim())
            } catch (_: Exception) {}

            // Buscamos el documento en la colección de usuarios
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.USERS_COLLECTION,
                queries = listOf(Query.equal("userId", userId))
            )
            val doc = response.documents.firstOrNull()
            val updateData = mutableMapOf<String, Any>(
                "name" to name.trim(),
                "phone" to phone.trim(),
                "bio" to bio.trim()
            )
            if (avatarPhotoId.isNotBlank()) {
                updateData["avatarPhotoId"] = avatarPhotoId
            }

            if (doc != null) {
                AppwriteClient.databases.updateDocument(
                    databaseId = AppwriteClient.DATABASE_ID,
                    collectionId = AppwriteClient.USERS_COLLECTION,
                    documentId = doc.id,
                    data = updateData
                )
            } else {
                updateData["userId"] = userId
                AppwriteClient.databases.createDocument(
                    databaseId = AppwriteClient.DATABASE_ID,
                    collectionId = AppwriteClient.USERS_COLLECTION,
                    documentId = ID.unique(),
                    data = updateData
                )
            }

            val updatedUser = getCurrentUser() ?: User(
                id = userId,
                name = name.trim(),
                email = "",
                phone = phone.trim(),
                bio = bio.trim(),
                avatarPhotoId = avatarPhotoId
            )
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        currentUser.value = null
        try {
            AppwriteClient.account.deleteSession("current")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
