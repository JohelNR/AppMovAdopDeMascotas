package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.AdoptionRequest
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAlmacenamientoAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioSolicitudesAppwrite
import kotlinx.coroutines.launch

class ModeloPerfil : ViewModel() {

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var estaGuardando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)

    var uriAvatarSeleccionado by mutableStateOf<Uri?>(null)

    // RF2: Vista personalizada según rol
    var misMascotas by mutableStateOf<List<Mascota>>(emptyList())
        private set

    var misSolicitudes by mutableStateOf<List<SolicitudAdopcion>>(emptyList())
        private set

    // Alias en inglés para compatibilidad
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

    fun cargarPerfil(usuario: Usuario?) {
        usuarioActual = usuario
        if (usuario == null) return

        viewModelScope.launch {
            if (usuario.role == RolUsuario.REFUGIO) {
                val todasMascotas = RepositorioMascotasAppwrite.getPets()
                misMascotas = todasMascotas.filter { 
                    it.shelterName.trim().equals(usuario.name.trim(), ignoreCase = true) 
                }
            } else {
                misSolicitudes = RepositorioSolicitudesAppwrite.getRequestsForApplicant(usuario.id)
            }
        }
    }

    fun loadProfile(user: Usuario?) = cargarPerfil(user)

    fun seleccionarAvatar(uri: Uri) {
        uriAvatarSeleccionado = uri
    }

    fun onAvatarSelected(uri: Uri) = seleccionarAvatar(uri)

    fun guardarPerfil(
        contexto: Context,
        nombre: String,
        telefono: String,
        biografia: String,
        alTenerExito: (Usuario) -> Unit
    ) {
        val usuario = usuarioActual ?: return

        if (nombre.isBlank()) {
            mensajeEstado = "El nombre no puede estar vacío"
            return
        }
        if (telefono.isNotBlank() && (telefono.length != 9 || !telefono.all { it.isDigit() })) {
            mensajeEstado = "El teléfono debe contener exactamente 9 dígitos numéricos"
            return
        }

        viewModelScope.launch {
            estaGuardando = true
            mensajeEstado = null

            var idFotoAvatar = usuario.avatarPhotoId
            uriAvatarSeleccionado?.let { uri ->
                val idSubido = RepositorioAlmacenamientoAppwrite.uploadPhoto(contexto, uri)
                if (idSubido != null) {
                    idFotoAvatar = idSubido
                }
            }

            val resultado = RepositorioAutenticacionAppwrite.updateProfile(
                userId = usuario.id,
                name = nombre.trim(),
                phone = telefono.trim(),
                bio = biografia.trim(),
                avatarPhotoId = idFotoAvatar
            )

            estaGuardando = false
            resultado.fold(
                onSuccess = { actualizado ->
                    usuarioActual = actualizado
                    mensajeEstado = "¡Perfil actualizado con éxito!"
                    alTenerExito(actualizado)
                },
                onFailure = { error ->
                    mensajeEstado = "Error al actualizar: ${error.message ?: "Intente nuevamente"}"
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
