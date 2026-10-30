package com.example.app_adopcion_d_mascotas.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.AdoptionRequest
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteAuthRepository
import com.example.app_adopcion_d_mascotas.data.repository.AppwritePetRepository
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteRequestsRepository
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteStorageRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)

    var selectedAvatarUri by mutableStateOf<Uri?>(null)

    // RF2: Vista personalizada según rol
    var myPets by mutableStateOf<List<Pet>>(emptyList())
        private set

    var myRequests by mutableStateOf<List<AdoptionRequest>>(emptyList())
        private set

    // Alias en español para fácil lectura y explicación
    val usuarioActual: User?
        get() = currentUser

    val estaGuardando: Boolean
        get() = isSaving

    var mensajeEstado: String?
        get() = statusMessage
        set(value) { statusMessage = value }

    val misMascotas: List<Pet>
        get() = myPets

    val misSolicitudes: List<AdoptionRequest>
        get() = myRequests

    fun cargarPerfil(usuario: User?) = loadProfile(usuario)
    fun seleccionarAvatar(uri: Uri) = onAvatarSelected(uri)

    fun loadProfile(user: User?) {
        currentUser = user
        if (user == null) return

        viewModelScope.launch {
            if (user.role == UserRole.REFUGIO) {
                // El refugio ve sus propias publicaciones activas
                val allPets = AppwritePetRepository.getPets()
                myPets = allPets.filter { 
                    it.shelterName.trim().equals(user.name.trim(), ignoreCase = true) 
                }
            } else {
                // El adoptante ve sus solicitudes enviadas
                myRequests = AppwriteRequestsRepository.getRequestsForApplicant(user.id)
            }
        }
    }

    fun onAvatarSelected(uri: Uri) {
        selectedAvatarUri = uri
    }

    // RF2: Guardar cambios del perfil con validaciones estrictas
    fun saveProfile(
        context: Context,
        name: String,
        phone: String,
        bio: String,
        onSuccess: (User) -> Unit
    ) {
        val user = currentUser ?: return

        // Validaciones estrictas nivel producción
        if (name.isBlank()) {
            statusMessage = "El nombre no puede estar vacío"
            return
        }
        if (phone.isNotBlank() && (phone.length != 9 || !phone.all { it.isDigit() })) {
            statusMessage = "El teléfono debe contener exactamente 9 dígitos numéricos"
            return
        }

        viewModelScope.launch {
            isSaving = true
            statusMessage = null

            var avatarPhotoId = user.avatarPhotoId
            // Si el usuario eligió una foto nueva, la subimos a Appwrite Storage
            selectedAvatarUri?.let { uri ->
                val uploadedId = AppwriteStorageRepository.uploadPhoto(context, uri)
                if (uploadedId != null) {
                    avatarPhotoId = uploadedId
                }
            }

            val result = AppwriteAuthRepository.updateProfile(
                userId = user.id,
                name = name.trim(),
                phone = phone.trim(),
                bio = bio.trim(),
                avatarPhotoId = avatarPhotoId
            )

            isSaving = false
            result.fold(
                onSuccess = { updated ->
                    currentUser = updated
                    statusMessage = "¡Perfil actualizado con éxito!"
                    onSuccess(updated)
                },
                onFailure = { error ->
                    statusMessage = "Error al actualizar: ${error.message ?: "Intente nuevamente"}"
                }
            )
        }
    }
}
