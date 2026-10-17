package com.example.app_adopcion_d_mascotas.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetStatus
import com.example.app_adopcion_d_mascotas.data.repository.AppwritePetRepository
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteStorageRepository
import kotlinx.coroutines.launch

class PublishViewModel : ViewModel() {

    var isSaving by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    // RF3: Lista de URIs de fotos seleccionadas desde la galería del celular (hasta 5)
    var selectedPhotoUris by mutableStateOf<List<Uri>>(emptyList())

    fun onPhotosSelected(uris: List<Uri>) {
        // Limitamos a un máximo de 5 fotos según el criterio de aceptación del RF3
        selectedPhotoUris = uris.take(5)
    }

    fun removePhoto(index: Int) {
        if (index in selectedPhotoUris.indices) {
            selectedPhotoUris = selectedPhotoUris.toMutableList().also { it.removeAt(index) }
        }
    }

    fun savePet(
        context: Context,
        existingPet: Pet?,
        name: String,
        breed: String,
        species: String,
        ageYears: Int,
        description: String,
        shelterName: String,
        isUrgent: Boolean,
        gender: String,
        size: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || breed.isBlank() || description.isBlank()) {
            statusMessage = "Por favor completa el nombre, raza y descripción"
            return
        }

        viewModelScope.launch {
            isSaving = true
            statusMessage = "Subiendo fotos a la nube..."

            // RF3: Subida real de fotos al bucket de Appwrite Storage
            val uploadedFileIds = mutableListOf<String>()

            // Si es edición, conservamos las fotos que ya tenía
            if (existingPet != null) {
                uploadedFileIds.addAll(existingPet.photos)
            }

            for ((index, uri) in selectedPhotoUris.withIndex()) {
                statusMessage = "Subiendo foto ${index + 1} de ${selectedPhotoUris.size}..."
                val fileId = AppwriteStorageRepository.uploadPhoto(context, uri)
                if (fileId != null) {
                    uploadedFileIds.add(fileId)
                }
            }

            statusMessage = "Guardando mascota en la base de datos..."

            val petToSave = if (existingPet != null) {
                existingPet.copy(
                    name = name.trim(),
                    breed = breed.trim(),
                    species = species,
                    ageYears = ageYears,
                    description = description.trim(),
                    shelterName = shelterName,
                    isUrgent = isUrgent,
                    gender = gender,
                    size = size,
                    location = location,
                    photos = uploadedFileIds
                )
            } else {
                Pet(
                    name = name.trim(),
                    breed = breed.trim(),
                    species = species,
                    ageYears = ageYears,
                    description = description.trim(),
                    shelterName = shelterName,
                    isUrgent = isUrgent,
                    status = PetStatus.DISPONIBLE,
                    gender = gender,
                    size = size,
                    location = location,
                    photos = uploadedFileIds
                )
            }

            val success = if (existingPet != null) {
                AppwritePetRepository.updatePet(petToSave)
            } else {
                AppwritePetRepository.createPet(petToSave)
            }

            isSaving = false
            if (success) {
                onSuccess()
            } else {
                statusMessage = "Error al guardar en la base de datos"
            }
        }
    }
}
