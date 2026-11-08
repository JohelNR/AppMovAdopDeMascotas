package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAlmacenamientoAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioSolicitudesAppwrite
import kotlinx.coroutines.launch

/*
 * ============================================================================
 * ARCHIVO: ModeloPerfil.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF2: Gestión de Perfil)
 * ¿QUÉ HACE?: Permite ver y editar los datos del usuario o refugio (nombre,
 *             teléfono validado a 9 dígitos, biografía y foto de perfil
 *             ajustada) guardando todo en la tabla "usuarios" de Appwrite.
 * ============================================================================
 */
class ModeloPerfil : ViewModel() {

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var estaGuardando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)

    var uriAvatarSeleccionado by mutableStateOf<Uri?>(null)

    // RF2: Listas contextuales según el rol del perfil
    var misMascotas by mutableStateOf<List<Mascota>>(emptyList())
        private set

    var misSolicitudes by mutableStateOf<List<SolicitudAdopcion>>(emptyList())
        private set

    // Alias de compatibilidad
    var currentUser: Usuario?
        get() = usuarioActual
        private set(value) { usuarioActual = value }
    val isSaving: Boolean get() = estaGuardando
    var statusMessage: String?
        get() = mensajeEstado
        set(value) { mensajeEstado = value }
    var selectedAvatarUri: Uri?
        get() = uriAvatarSeleccionado
        set(value) { uriAvatarSeleccionado = value }
    val myPets: List<Mascota> get() = misMascotas
    val myRequests: List<SolicitudAdopcion> get() = misSolicitudes

    /*
     * Carga los datos del usuario y sus publicaciones (si es REFUGIO) o solicitudes (si es ADOPTANTE)
     */
    fun cargarPerfil(usuario: Usuario?) {
        usuarioActual = usuario
        if (usuario == null) return

        viewModelScope.launch {
            if (usuario.rol == RolUsuario.REFUGIO) {
                val todasMascotas = RepositorioMascotasAppwrite.obtenerMascotas()
                misMascotas = todasMascotas.filter {
                    it.nombreRefugio.trim().equals(usuario.nombre.trim(), ignoreCase = true) ||
                        (it.idRefugio.isNotBlank() && it.idRefugio == usuario.id)
                }
            } else {
                misSolicitudes = RepositorioSolicitudesAppwrite.obtenerSolicitudesAdoptante(usuario.id)
            }
        }
    }

    fun loadProfile(user: Usuario?) = cargarPerfil(user)

    fun seleccionarAvatar(uri: Uri) {
        uriAvatarSeleccionado = uri
    }

    fun onAvatarSelected(uri: Uri) = seleccionarAvatar(uri)

    /*
     * RF2: Valida los datos del perfil y los actualiza en la tabla "usuarios" de Appwrite Cloud
     */
    fun guardarPerfil(
        contexto: Context,
        nombre: String,
        telefono: String,
        biografia: String,
        alTenerExito: (Usuario) -> Unit
    ) {
        val usuario = usuarioActual ?: return
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        val biografiaLimpia = biografia.trim()

        // Paso 1: Validar nombre mínimo de 3 caracteres
        if (nombreLimpio.length < 3) {
            mensajeEstado = "El nombre debe tener al menos 3 letras."
            return
        }

        // Paso 2: Validar que el teléfono tenga 9 dígitos numéricos si fue ingresado
        if (telefonoLimpio.isNotEmpty() && (telefonoLimpio.length != 9 || !telefonoLimpio.all { it.isDigit() })) {
            mensajeEstado = "El teléfono debe contener exactamente 9 dígitos numéricos (ej. 987654321)."
            return
        }

        viewModelScope.launch {
            estaGuardando = true
            mensajeEstado = null

            // Paso 3: Si seleccionó y recortó una nueva foto de perfil, subirla a Appwrite Storage
            var idFotoFinal = usuario.idFotoAvatar
            uriAvatarSeleccionado?.let { uriRecortada ->
                val idSubido = RepositorioAlmacenamientoAppwrite.subirFoto(contexto, uriRecortada)
                if (idSubido != null) {
                    idFotoFinal = idSubido
                }
            }

            // Paso 4: Guardar los datos en la tabla "usuarios" de Appwrite Cloud
            val resultado = RepositorioAutenticacionAppwrite.actualizarPerfil(
                idUsuario = usuario.id,
                nombre = nombreLimpio,
                telefono = telefonoLimpio,
                biografia = biografiaLimpia,
                idFotoAvatar = idFotoFinal
            )

            estaGuardando = false
            resultado.fold(
                onSuccess = { actualizado ->
                    usuarioActual = actualizado
                    uriAvatarSeleccionado = null
                    mensajeEstado = "¡Perfil actualizado en la nube con éxito!"
                    alTenerExito(actualizado)
                },
                onFailure = { error ->
                    mensajeEstado = "Error al guardar el perfil: ${error.message ?: "Intenta nuevamente"}"
                }
            )
        }
    }

    fun saveProfile(
        context: Context,
        name: String,
        phone: String,
        bio: String,
        onSuccess: (Usuario) -> Unit
    ) = guardarPerfil(context, name, phone, bio, onSuccess)
}

typealias ProfileViewModel = ModeloPerfil
