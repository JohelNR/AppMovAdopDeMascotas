package com.example.app_adopcion_d_mascotas.data.repository

import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppwriteAuthRepository {

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
            val roleStr = doc?.data?.get("role")?.toString() ?: "ADOPTANTE"
            val userRole = if (roleStr.equals("REFUGIO", ignoreCase = true)) UserRole.REFUGIO else UserRole.ADOPTANTE

            User(
                id = accountUser.id,
                name = accountUser.name.ifBlank { doc?.data?.get("name")?.toString() ?: "Usuario" },
                email = accountUser.email,
                role = userRole
            )
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
                    "name" to name,
                    "email" to email,
                    "role" to role.name
                )
            )

            val createdUser = User(
                id = userId,
                name = name,
                email = email,
                role = role
            )
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            AppwriteClient.account.deleteSession("current")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
