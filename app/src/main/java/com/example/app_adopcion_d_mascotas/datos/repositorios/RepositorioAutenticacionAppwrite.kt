package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

object RepositorioAutenticacionAppwrite {

    val usuarioActual = MutableStateFlow<Usuario?>(null)
    val currentUser get() = usuarioActual

    suspend fun getCurrentUser(): Usuario? = withContext(Dispatchers.IO) {
        try {
            val accountUser = ClienteAppwrite.account.get()
            // Buscamos su perfil y rol en la tabla de usuarios
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.USERS_COLLECTION,
                queries = listOf(Query.equal("userId", accountUser.id))
            )
            val doc = response.documents.firstOrNull()
            val data = doc?.data ?: emptyMap()
            val roleStr = data["role"]?.toString() ?: "ADOPTANTE"
            val userRole = if (roleStr.equals("REFUGIO", ignoreCase = true)) RolUsuario.REFUGIO else RolUsuario.ADOPTANTE

            val user = Usuario(
                id = accountUser.id,
                name = accountUser.name.ifBlank { data["name"]?.toString() ?: "Usuario" },
                email = accountUser.email,
                role = userRole,
                phone = data["phone"]?.toString() ?: "",
                bio = data["bio"]?.toString() ?: "",
                avatarPhotoId = data["avatarPhotoId"]?.toString() ?: ""
            )
            usuarioActual.value = user
            user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerUsuarioActual(): Usuario? = getCurrentUser()

    suspend fun login(email: String, password: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            try { ClienteAppwrite.account.deleteSession("current") } catch (_: Exception) {}

            ClienteAppwrite.account.createEmailPasswordSession(email, password)
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

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> = login(correo, contrasena)

    suspend fun register(name: String, email: String, password: String, role: RolUsuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userId = ID.unique()
            ClienteAppwrite.account.create(
                userId = userId,
                email = email,
                password = password,
                name = name
            )

            // Iniciamos sesión
            ClienteAppwrite.account.createEmailPasswordSession(email, password)

            // Guardamos el perfil con su rol en la tabla de usuarios
            ClienteAppwrite.databases.createDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.USERS_COLLECTION,
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

            val createdUser = Usuario(
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

    suspend fun registrarUsuario(nombre: String, correo: String, contrasena: String, rol: RolUsuario): Result<Usuario> =
        register(nombre, correo, contrasena, rol)

    // RF2: Actualización de datos de perfil (nombre, teléfono, biografía, foto avatar)
    suspend fun updateProfile(
        userId: String,
        name: String,
        phone: String,
        bio: String,
        avatarPhotoId: String
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            // Actualizamos en la cuenta de Appwrite
            try {
                ClienteAppwrite.account.updateName(name.trim())
            } catch (_: Exception) {}

            // Buscamos el documento en la colección de usuarios
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.USERS_COLLECTION,
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
                ClienteAppwrite.databases.updateDocument(
                    databaseId = ClienteAppwrite.DATABASE_ID,
                    collectionId = ClienteAppwrite.USERS_COLLECTION,
                    documentId = doc.id,
                    data = updateData
                )
            } else {
                updateData["userId"] = userId
                ClienteAppwrite.databases.createDocument(
                    databaseId = ClienteAppwrite.DATABASE_ID,
                    collectionId = ClienteAppwrite.USERS_COLLECTION,
                    documentId = ID.unique(),
                    data = updateData
                )
            }

            val updatedUser = getCurrentUser() ?: Usuario(
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

    suspend fun actualizarPerfil(
        idUsuario: String,
        nombre: String,
        telefono: String,
        biografia: String,
        idFotoAvatar: String
    ): Result<Usuario> = updateProfile(idUsuario, nombre, telefono, biografia, idFotoAvatar)

    suspend fun logout() = withContext(Dispatchers.IO) {
        usuarioActual.value = null
        try {
            ClienteAppwrite.account.deleteSession("current")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun cerrarSesion() = logout()
}

typealias AppwriteAuthRepository = RepositorioAutenticacionAppwrite
