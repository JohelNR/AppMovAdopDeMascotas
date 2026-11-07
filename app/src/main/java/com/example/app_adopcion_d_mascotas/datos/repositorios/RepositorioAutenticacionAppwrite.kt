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

class RepositorioAutenticacionAppwrite(private val ClienteAppwrite: ClienteAppwrite) {

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
            val doc = response.documents.firstOrNull() ?: error("No existe el perfil de esta cuenta")
            val data = doc.data
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
            usuarioActual.value = null
            null
        }
    }

    suspend fun obtenerUsuarioActual(): Usuario? = getCurrentUser()

    suspend fun buscarIdUsuario(nombre: String): String {
        val usuarios = ClienteAppwrite.databases.listDocuments(
            ClienteAppwrite.DATABASE_ID, ClienteAppwrite.USERS_COLLECTION,
            listOf(Query.equal("name", nombre.trim()))
        ).documents
        check(usuarios.size == 1) { "El destinatario no existe o su nombre es ambiguo" }
        return usuarios.single().data["userId"]?.toString().orEmpty()
    }

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
            usuarioActual.value = createdUser
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
            ClienteAppwrite.account.updateName(name.trim())

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

            val updatedUser = getCurrentUser() ?: error("No se pudo recuperar el perfil actualizado")
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

    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.account.deleteSession("current")
            usuarioActual.value = null
            true
        } catch (e: Exception) {
            if (e is io.appwrite.exceptions.AppwriteException && e.code == 401) {
                usuarioActual.value = null
                true
            } else {
                false
            }
        }
    }

    suspend fun cerrarSesion() = logout()
}

typealias AppwriteAuthRepository = RepositorioAutenticacionAppwrite
