package com.example.app_adopcion_d_mascotas.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetFollowUp
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteFollowUpRepository
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteStorageRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FollowUpViewModel : ViewModel() {

    var followUpsList by mutableStateOf<List<PetFollowUp>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)

    var selectedPhotos by mutableStateOf<List<Uri>>(emptyList())

    // Alias en español para lectura directa
    val listaSeguimientos: List<PetFollowUp>
        get() = followUpsList

    val estaCargando: Boolean
        get() = isLoading

    val estaEnviando: Boolean
        get() = isSubmitting

    var mensajeEstado: String?
        get() = statusMessage
        set(value) { statusMessage = value }

    val fotosSeleccionadas: List<Uri>
        get() = selectedPhotos

    fun cargarSeguimientos(idMascota: String) = loadFollowUps(idMascota)
    fun seleccionarFotos(uris: List<Uri>) = onPhotosSelected(uris)
    fun eliminarFoto(indice: Int) = removePhoto(indice)

    fun onPhotosSelected(uris: List<Uri>) {
        selectedPhotos = uris.take(3)
    }

    fun removePhoto(index: Int) {
        if (index in selectedPhotos.indices) {
            selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(index) }
        }
    }

    fun loadFollowUps(petId: String) {
        viewModelScope.launch {
            isLoading = true
            followUpsList = AppwriteFollowUpRepository.getFollowUps(petId)
            isLoading = false
        }
    }

    // RF11: Guardar reporte con fotos y notas de adaptación
    fun submitReport(
        context: Context,
        pet: Pet,
        currentUser: User,
        healthNotes: String,
        adaptationNotes: String,
        onSuccess: () -> Unit
    ) {
        if (healthNotes.isBlank() && adaptationNotes.isBlank()) {
            statusMessage = "Por favor ingresa notas sobre la salud o adaptación de la mascota"
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            statusMessage = null

            // Subir fotos adjuntas a Storage de Appwrite
            val uploadedPhotoUrls = mutableListOf<String>()
            for (uri in selectedPhotos) {
                val fileId = AppwriteStorageRepository.uploadPhoto(context, uri)
                if (fileId != null) {
                    uploadedPhotoUrls.add(fileId)
                }
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayDate = dateFormat.format(Date())

            val newReport = PetFollowUp(
                petId = pet.id,
                petName = pet.name,
                applicantId = currentUser.id,
                applicantName = currentUser.name,
                shelterName = pet.shelterName,
                date = todayDate,
                healthNotes = healthNotes.trim(),
                adaptationNotes = adaptationNotes.trim(),
                photos = uploadedPhotoUrls,
                status = "REVISADO"
            )

            val success = AppwriteFollowUpRepository.createFollowUp(newReport)
            isSubmitting = false

            if (success) {
                selectedPhotos = emptyList()
                statusMessage = "¡Reporte de seguimiento registrado con éxito!"
                loadFollowUps(pet.id)
                onSuccess()
            } else {
                statusMessage = "Error al guardar reporte en la nube"
            }
        }
    }
}
