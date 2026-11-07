package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAlmacenamientoAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import kotlinx.coroutines.launch

class ModeloPublicar(
    private val RepositorioAlmacenamientoAppwrite: RepositorioAlmacenamientoAppwrite,
    private val RepositorioMascotasAppwrite: RepositorioMascotasAppwrite
) : ViewModel() {

    var estaGuardando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)
        private set

    // RF3: Lista de URIs de fotos seleccionadas desde la galería del celular (hasta 5)
    var fotosSeleccionadas by mutableStateOf<List<Uri>>(emptyList())

    // Alias en inglés para compatibilidad
    var isSaving: Boolean
        get() = estaGuardando
        private set(value) { estaGuardando = value }

    var statusMessage: String?
        get() = mensajeEstado
        private set(value) { mensajeEstado = value }

    var selectedPhotoUris: List<Uri>
        get() = fotosSeleccionadas
        set(value) { fotosSeleccionadas = value }

    fun seleccionarFotos(uris: List<Uri>) {
        fotosSeleccionadas = uris.take(5)
    }

    fun onPhotosSelected(uris: List<Uri>) = seleccionarFotos(uris)
    fun alSeleccionarFotos(uris: List<Uri>) = seleccionarFotos(uris)

    fun quitarFoto(indice: Int) {
        if (indice in fotosSeleccionadas.indices) {
            fotosSeleccionadas = fotosSeleccionadas.toMutableList().also { it.removeAt(indice) }
        }
    }

    fun removePhoto(index: Int) = quitarFoto(index)

    fun guardarMascota(
        contexto: Context,
        mascotaExistente: Mascota?,
        nombre: String,
        raza: String,
        especie: String,
        edadAnos: Int,
        descripcion: String,
        nombreRefugio: String,
        esUrgente: Boolean,
        sexo: String,
        tamano: String,
        ubicacion: String,
        alTenerExito: () -> Unit,
        latitud: Double = mascotaExistente?.latitude ?: -12.0464,
        longitud: Double = mascotaExistente?.longitude ?: -77.0428
    ) {
        if (nombre.isBlank() || raza.isBlank() || descripcion.isBlank()) {
            mensajeEstado = "Por favor completa el nombre, raza y descripción"
            return
        }
        if (edadAnos < 1 || edadAnos > 25) {
            mensajeEstado = "La edad debe estar entre 1 y 25 años"
            return
        }

        if (!latitud.isFinite() || !longitud.isFinite() || latitud !in -90.0..90.0 || longitud !in -180.0..180.0) {
            mensajeEstado = "Ingresa coordenadas válidas para el refugio"
            return
        }
        if ((mascotaExistente?.photos?.size ?: 0) + fotosSeleccionadas.size > 5) {
            mensajeEstado = "El total de fotos existentes y nuevas no puede superar 5"
            return
        }
        if (estaGuardando) return
        viewModelScope.launch {
            estaGuardando = true
            mensajeEstado = "Subiendo fotos a la nube..."

            val idsArchivosSubidos = mutableListOf<String>()

            if (mascotaExistente != null) {
                idsArchivosSubidos.addAll(mascotaExistente.photos)
            }

            for ((index, uri) in fotosSeleccionadas.withIndex()) {
                mensajeEstado = "Subiendo foto ${index + 1} de ${fotosSeleccionadas.size}..."
                val idArchivo = RepositorioAlmacenamientoAppwrite.uploadPhoto(contexto, uri)
                if (idArchivo != null) {
                    idsArchivosSubidos.add(idArchivo)
                } else {
                    estaGuardando = false
                    mensajeEstado = "No se pudo subir una foto; la publicación no se guardó"
                    return@launch
                }
            }

            mensajeEstado = "Guardando mascota en la base de datos..."

            val mascotaAGuardar = if (mascotaExistente != null) {
                mascotaExistente.copy(
                    name = nombre.trim(),
                    breed = raza.trim(),
                    species = especie,
                    ageYears = edadAnos,
                    description = descripcion.trim(),
                    shelterName = nombreRefugio,
                    isUrgent = esUrgente,
                    gender = sexo,
                    size = tamano,
                    location = ubicacion,
                    photos = idsArchivosSubidos,
                    latitude = latitud,
                    longitude = longitud
                )
            } else {
                Mascota(
                    name = nombre.trim(),
                    breed = raza.trim(),
                    species = especie,
                    ageYears = edadAnos,
                    description = descripcion.trim(),
                    shelterName = nombreRefugio,
                    isUrgent = esUrgente,
                    status = EstadoMascota.DISPONIBLE,
                    gender = sexo,
                    size = tamano,
                    location = ubicacion,
                    photos = idsArchivosSubidos,
                    latitude = latitud,
                    longitude = longitud
                )
            }

            val exito = if (mascotaExistente != null) {
                RepositorioMascotasAppwrite.updatePet(mascotaAGuardar)
            } else {
                RepositorioMascotasAppwrite.createPet(mascotaAGuardar)
            }

            estaGuardando = false
            if (exito) {
                alTenerExito()
            } else {
                mensajeEstado = "Error al guardar en la base de datos"
            }
        }
    }

    fun savePet(
        context: Context,
        existingPet: Mascota?,
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
    ) = guardarMascota(
        contexto = context,
        mascotaExistente = existingPet,
        nombre = name,
        raza = breed,
        especie = species,
        edadAnos = ageYears,
        descripcion = description,
        nombreRefugio = shelterName,
        esUrgente = isUrgent,
        sexo = gender,
        tamano = size,
        ubicacion = location,
        alTenerExito = onSuccess
    )
}

typealias PublishViewModel = ModeloPublicar
