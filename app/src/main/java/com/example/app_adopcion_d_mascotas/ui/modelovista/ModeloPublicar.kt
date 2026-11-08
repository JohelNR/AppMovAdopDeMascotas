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
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAlmacenamientoAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import kotlinx.coroutines.launch

/*
 * ============================================================================
 * ARCHIVO: ModeloPublicar.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF3, RF6 y RF7: Publicar/Editar Mascota)
 * ¿QUÉ HACE?: Valida el formulario de publicación de una mascota, sube sus
 *             fotos recortadas al bucket "fotos_mascotas" de Appwrite Storage
 *             y guarda la ficha completa con coordenadas reales en la tabla
 *             "mascotas" de Appwrite Cloud.
 * ============================================================================
 */
class ModeloPublicar : ViewModel() {

    var estaGuardando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)
        private set

    // RF3: Lista de fotos seleccionadas y ajustadas por el usuario (máximo 5)
    var fotosSeleccionadas by mutableStateOf<List<Uri>>(emptyList())

    // Alias para compatibilidad
    var isSaving: Boolean
        get() = estaGuardando
        private set(value) { estaGuardando = value }
    var statusMessage: String?
        get() = mensajeEstado
        private set(value) { mensajeEstado = value }
    var selectedPhotoUris: List<Uri>
        get() = fotosSeleccionadas
        set(value) { fotosSeleccionadas = value }

    /*
     * Agrega una nueva foto (ya recortada/ajustada) a la lista de fotos por subir
     */
    fun agregarFotoAjustada(uriRecortada: Uri) {
        if (fotosSeleccionadas.size < 5) {
            fotosSeleccionadas = fotosSeleccionadas + uriRecortada
            mensajeEstado = null
        } else {
            mensajeEstado = "Puedes subir como máximo 5 fotos por mascota."
        }
    }

    fun seleccionarFotos(uris: List<Uri>) {
        fotosSeleccionadas = (fotosSeleccionadas + uris).take(5)
    }

    fun onPhotosSelected(uris: List<Uri>) = seleccionarFotos(uris)
    fun alSeleccionarFotos(uris: List<Uri>) = seleccionarFotos(uris)

    /*
     * Quita una foto de la vista previa según su posición
     */
    fun quitarFoto(indice: Int) {
        if (indice in fotosSeleccionadas.indices) {
            fotosSeleccionadas = fotosSeleccionadas.toMutableList().also { it.removeAt(indice) }
        }
    }

    fun removePhoto(index: Int) = quitarFoto(index)

    /*
     * RF3, RF6 y RF7: Valida todos los datos y guarda o actualiza la mascota en Appwrite Cloud
     */
    fun guardarMascota(
        contexto: Context,
        mascotaExistente: Mascota?,
        nombre: String,
        raza: String,
        especie: String,
        edadAnos: Int,
        descripcion: String,
        nombreRefugio: String,
        idRefugio: String = "",
        esUrgente: Boolean,
        sexo: String,
        tamano: String,
        ubicacion: String,
        latitud: Double = -7.1617,
        longitud: Double = -78.5128,
        alTenerExito: () -> Unit
    ) {
        val nombreLimpio = nombre.trim()
        val razaLimpia = raza.trim()
        val descripcionLimpia = descripcion.trim()
        val ubicacionLimpia = ubicacion.trim()
        val refugioLimpio = nombreRefugio.trim()

        // Paso 1: Validaciones profesionales de cada campo
        if (nombreLimpio.length < 2) {
            mensajeEstado = "Ingresa un nombre válido para la mascota (mínimo 2 letras)."
            return
        }
        if (razaLimpia.length < 2) {
            mensajeEstado = "Ingresa la raza de la mascota (ej. Mestizo, Golden, Siamés)."
            return
        }
        if (edadAnos < 0 || edadAnos > 25) {
            mensajeEstado = "La edad debe ser un número entre 0 y 25 años."
            return
        }
        if (ubicacionLimpia.length < 3) {
            mensajeEstado = "Ingresa la dirección o ciudad donde se encuentra la mascota."
            return
        }
        if (refugioLimpio.length < 3) {
            mensajeEstado = "Ingresa el nombre del refugio responsable."
            return
        }
        if (descripcionLimpia.length < 10) {
            mensajeEstado = "Escribe una historia o descripción de al menos 10 caracteres."
            return
        }
        if (mascotaExistente == null && fotosSeleccionadas.isEmpty()) {
            mensajeEstado = "Por favor, selecciona y ajusta al menos 1 foto de la mascota."
            return
        }

        // Paso 2: Subir las fotos nuevas a Appwrite Storage y luego guardar en la tabla "mascotas"
        viewModelScope.launch {
            estaGuardando = true
            mensajeEstado = "Preparando subida a la nube..."

            val idsFotosFinales = mutableListOf<String>()

            // Si estamos editando, conservamos las fotos anteriores que ya tenía en la nube
            if (mascotaExistente != null) {
                idsFotosFinales.addAll(mascotaExistente.fotos)
            }

            // Subir cada foto nueva al bucket "fotos_mascotas"
            for ((indice, uri) in fotosSeleccionadas.withIndex()) {
                mensajeEstado = "Subiendo foto ${indice + 1} de ${fotosSeleccionadas.size}..."
                val idSubido = RepositorioAlmacenamientoAppwrite.subirFoto(contexto, uri)
                if (idSubido != null) {
                    idsFotosFinales.add(idSubido)
                }
            }

            mensajeEstado = "Guardando ficha de $nombreLimpio en Appwrite Cloud..."

            // Paso 3: Construir el objeto Mascota con sus coordenadas reales para el mapa (RF7)
            val mascotaAGuardar = Mascota(
                id = mascotaExistente?.id ?: "",
                name = nombreLimpio,
                breed = razaLimpia,
                species = especie,
                ageYears = edadAnos,
                description = descripcionLimpia,
                shelterName = refugioLimpio,
                shelterId = idRefugio.ifBlank { mascotaExistente?.idRefugio ?: "" },
                isUrgent = esUrgente,
                status = mascotaExistente?.estado ?: EstadoMascota.DISPONIBLE,
                gender = sexo,
                size = tamano,
                location = ubicacionLimpia,
                photos = idsFotosFinales,
                latitude = latitud,
                longitude = longitud
            )

            // Paso 4: Guardar o actualizar en la tabla "mascotas"
            val exito = if (mascotaExistente != null) {
                RepositorioMascotasAppwrite.actualizarMascota(mascotaAGuardar)
            } else {
                RepositorioMascotasAppwrite.crearMascota(mascotaAGuardar)
            }

            estaGuardando = false
            if (exito) {
                fotosSeleccionadas = emptyList()
                mensajeEstado = null
                alTenerExito()
            } else {
                mensajeEstado = "No se pudo guardar en la base de datos. Verifica tu conexión."
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
