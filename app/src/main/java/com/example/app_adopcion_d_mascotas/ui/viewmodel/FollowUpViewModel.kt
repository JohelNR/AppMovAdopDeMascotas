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
        if (healthNotes.isBlank() || adaptationNotes.isBlank()) {
            statusMessage = "Por favor completa el estado de salud y notas de adaptación"
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            statusMessage = "Subiendo fotos y guardando reporte..."

            val uploadedPhotoIds = mutableListOf<String>()
            for (uri in selectedPhotos) {
                val photoId = AppwriteStorageRepository.uploadPhoto(context, uri)
                if (photoId != null) {
                    uploadedPhotoIds.add(photoId)
                }
            }

            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val report = PetFollowUp(
                petId = pet.id,
                petName = pet.name,
                applicantId = currentUser.id,
                applicantName = currentUser.name,
                shelterName = pet.shelterName,
                date = currentDate,
                healthNotes = healthNotes.trim(),
                adaptationNotes = adaptationNotes.trim(),
                photos = uploadedPhotoIds,
                status = "ENVIADO"
            )

            AppwriteFollowUpRepository.createFollowUp(report)
            isSubmitting = false
            statusMessage = "¡Reporte de seguimiento registrado con éxito!"
            selectedPhotos = emptyList()
            loadFollowUps(pet.id)
            onSuccess()
        }
    }
}
