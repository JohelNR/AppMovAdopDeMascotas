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

/*
 * ============================================================================
 * ARCHIVO: RepositorioAutenticacionAppwrite.kt
 * CAPA: Datos -> Repositorios (RF1 y RF2)
 * ¿QUÉ HACE?: Se encarga de hablar con la tabla "usuarios" y el servicio de
 *             cuentas de Appwrite Cloud para:
 *             1. Registrar un usuario nuevo con su rol (ADOPTANTE o REFUGIO).
 *             2. Iniciar sesión con correo y contraseña.
 *             3. Recuperar la sesión activa al abrir la app.
 *             4. Actualizar los datos y foto del perfil en la nube.
 * ============================================================================
 */
object RepositorioAutenticacionAppwrite {

    // Guarda el usuario conectado actualmente para que toda la app lo conozca
    val usuarioActual = MutableStateFlow<Usuario?>(null)
    val currentUser get() = usuarioActual

    /*
     * Obtiene el usuario que tiene sesión iniciada en este momento desde Appwrite Cloud.
     */
    suspend fun obtenerUsuarioActual(): Usuario? = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Preguntar a Appwrite Auth quién tiene sesión abierta en el teléfono
            val cuentaActiva = ClienteAppwrite.cuenta.get()

            // Paso 2: Buscar su fila en la tabla "usuarios" usando su "idUsuario"
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_USUARIOS,
                queries = listOf(Query.equal("idUsuario", cuentaActiva.id))
            )

            // Paso 3: Extraer los datos en español de la tabla "usuarios"
            val documento = respuesta.documents.firstOrNull()
            val datos = documento?.data ?: emptyMap()

            val textoRol = datos["rol"]?.toString() ?: "ADOPTANTE"
            val rolAsignado = if (textoRol.equals("REFUGIO", ignoreCase = true)) {
                RolUsuario.REFUGIO
            } else {
                RolUsuario.ADOPTANTE
            }

            // Paso 4: Construir el objeto Usuario listo para las pantallas
            val usuario = Usuario(
                id = cuentaActiva.id,
                name = (datos["nombre"]?.toString() ?: cuentaActiva.name).ifBlank { "Usuario" },
                email = cuentaActiva.email,
                role = rolAsignado,
                phone = datos["telefono"]?.toString() ?: "",
                bio = datos["biografia"]?.toString() ?: "",
                avatarPhotoId = datos["fotoPerfilId"]?.toString() ?: ""
            )

            usuarioActual.value = usuario
            usuario
        } catch (_: Exception) {
            // Si no hay sesión abierta en el teléfono, devolvemos null
            null
        }
    }

    suspend fun getCurrentUser(): Usuario? = obtenerUsuarioActual()

    /*
     * RF1: Iniciar sesión con correo y contraseña en Appwrite Cloud.
     */
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Si había alguna sesión anterior abierta, la cerramos primero para evitar choques
            try {
                ClienteAppwrite.cuenta.deleteSession("current")
            } catch (_: Exception) {}

            // Paso 2: Crear la nueva sesión con correo y contraseña en Appwrite
            ClienteAppwrite.cuenta.createEmailPasswordSession(
                email = correo.trim(),
                password = contrasena
            )

            // Paso 3: Leer el perfil completo (con su rol ADOPTANTE o REFUGIO) desde la tabla "usuarios"
            val usuario = obtenerUsuarioActual()
            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("No se encontró el perfil del usuario en la base de datos"))
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun login(email: String, password: String): Result<Usuario> = iniciarSesion(email, password)

    /*
     * RF1: Registrar una cuenta nueva en Appwrite Cloud guardando su rol (ADOPTANTE o REFUGIO).
     */
    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: RolUsuario
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim()

            // Paso 1: Cerrar cualquier sesión previa en el teléfono para que Appwrite NO bloquee el registro
            try {
                ClienteAppwrite.cuenta.deleteSession("current")
            } catch (_: Exception) {}

            // Paso 2: Crear la cuenta en Appwrite Auth (o recuperar su ID si ya se había creado en Auth)
            val nuevoIdUsuario = ID.unique()
            ClienteAppwrite.cuenta.create(
                userId = nuevoIdUsuario,
                email = correoLimpio,
                password = contrasena,
                name = nombreLimpio
            )

            // Paso 3: Iniciar sesión inmediatamente con la cuenta recién creada
            ClienteAppwrite.cuenta.createEmailPasswordSession(
                email = correoLimpio,
                password = contrasena
            )

            // Paso 4: Guardar los datos y el ROL elegido (ADOPTANTE o REFUGIO) en la tabla "usuarios"
            val datosNuevoUsuario = mapOf(
                "idUsuario" to nuevoIdUsuario,
                "nombre" to nombreLimpio,
                "correo" to correoLimpio,
                "rol" to rol.name,
                "telefono" to "",
                "biografia" to "",
                "fotoPerfilId" to ""
            )

            ClienteAppwrite.baseDeDatos.createDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_USUARIOS,
                documentId = ID.unique(),
                data = datosNuevoUsuario
            )

            // Paso 5: Actualizar el usuario en memoria y devolver éxito
            val usuarioCreado = Usuario(
                id = nuevoIdUsuario,
                name = nombreLimpio,
                email = correoLimpio,
                role = rol
            )
            usuarioActual.value = usuarioCreado
            Result.success(usuarioCreado)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun register(name: String, email: String, password: String, role: RolUsuario): Result<Usuario> =
        registrarUsuario(name, email, password, role)

    /*
     * RF2: Actualizar nombre, teléfono, biografía y foto de perfil en la tabla "usuarios".
     */
    suspend fun actualizarPerfil(
        idUsuario: String,
        nombre: String,
        telefono: String,
        biografia: String,
        idFotoAvatar: String
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val nombreLimpio = nombre.trim()
            val telefonoLimpio = telefono.trim()
            val biografiaLimpia = biografia.trim()

            // Paso 1: Actualizar el nombre también en Appwrite Auth
            try {
                ClienteAppwrite.cuenta.updateName(nombreLimpio)
            } catch (_: Exception) {}

            // Paso 2: Buscar el documento del usuario en la tabla "usuarios"
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_USUARIOS,
                queries = listOf(Query.equal("idUsuario", idUsuario))
            )
            val documentoExistente = respuesta.documents.firstOrNull()

            // Paso 3: Preparar el mapa con las columnas en español a actualizar
            val datosActualizados = mutableMapOf<String, Any>(
                "nombre" to nombreLimpio,
                "telefono" to telefonoLimpio,
                "biografia" to biografiaLimpia
            )
            if (idFotoAvatar.isNotBlank()) {
                datosActualizados["fotoPerfilId"] = idFotoAvatar
            }

            // Paso 4: Guardar los cambios en la tabla "usuarios" de Appwrite
            if (documentoExistente != null) {
                ClienteAppwrite.baseDeDatos.updateDocument(
                    databaseId = ClienteAppwrite.ID_BASE_DATOS,
                    collectionId = ClienteAppwrite.TABLA_USUARIOS,
                    documentId = documentoExistente.id,
                    data = datosActualizados
                )
            } else {
                val rolActual = usuarioActual.value?.rol?.name ?: RolUsuario.ADOPTANTE.name
                val correoActual = usuarioActual.value?.correo ?: ""
                datosActualizados["idUsuario"] = idUsuario
                datosActualizados["correo"] = correoActual
                datosActualizados["rol"] = rolActual
                if (!datosActualizados.containsKey("fotoPerfilId")) {
                    datosActualizados["fotoPerfilId"] = ""
                }
                ClienteAppwrite.baseDeDatos.createDocument(
                    databaseId = ClienteAppwrite.ID_BASE_DATOS,
                    collectionId = ClienteAppwrite.TABLA_USUARIOS,
                    documentId = ID.unique(),
                    data = datosActualizados
                )
            }

            // Paso 5: Volver a leer el usuario actualizado desde la nube
            val usuarioFresco = obtenerUsuarioActual() ?: Usuario(
                id = idUsuario,
                name = nombreLimpio,
                email = usuarioActual.value?.correo ?: "",
                role = usuarioActual.value?.rol ?: RolUsuario.ADOPTANTE,
                phone = telefonoLimpio,
                bio = biografiaLimpia,
                avatarPhotoId = idFotoAvatar
            )
            usuarioActual.value = usuarioFresco
            Result.success(usuarioFresco)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun updateProfile(
        userId: String,
        name: String,
        phone: String,
        bio: String,
        avatarPhotoId: String
    ): Result<Usuario> = actualizarPerfil(userId, name, phone, bio, avatarPhotoId)

    /*
     * Cerrar la sesión actual en el servidor y limpiar el usuario activo.
     */
    suspend fun cerrarSesion() = withContext(Dispatchers.IO) {
        usuarioActual.value = null
        try {
            ClienteAppwrite.cuenta.deleteSession("current")
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    suspend fun logout() = cerrarSesion()
}

typealias AppwriteAuthRepository = RepositorioAutenticacionAppwrite
