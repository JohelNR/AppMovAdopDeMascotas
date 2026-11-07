package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.PetFollowUp
import com.example.app_adopcion_d_mascotas.datos.modelos.SeguimientoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAlmacenamientoAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioSeguimientoAppwrite
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ModeloSeguimiento : ViewModel() {

    var listaSeguimientos by mutableStateOf<List<SeguimientoMascota>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var estaEnviando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)

    var fotosSeleccionadas by mutableStateOf<List<Uri>>(emptyList())

    // Alias en inglés para compatibilidad
    var followUpsList: List<SeguimientoMascota>
        get() = listaSeguimientos
        private set(value) { listaSeguimientos = value }

    val isLoading: Boolean get() = estaCargando
    val isSubmitting: Boolean get() = estaEnviando
    var statusMessage: String?
        get() = mensajeEstado
        set(value) { mensajeEstado = value }

    var selectedPhotos: List<Uri>
        get() = fotosSeleccionadas
        set(value) { fotosSeleccionadas = value }

    fun seleccionarFotos(uris: List<Uri>) {
        fotosSeleccionadas = uris.take(3)
    }

    fun onPhotosSelected(uris: List<Uri>) = seleccionarFotos(uris)

    fun eliminarFoto(indice: Int) {
        if (indice in fotosSeleccionadas.indices) {
            fotosSeleccionadas = fotosSeleccionadas.toMutableList().also { it.removeAt(indice) }
        }
    }

    fun removePhoto(index: Int) = eliminarFoto(index)

    fun cargarSeguimientos(idMascota: String) {
        viewModelScope.launch {
            estaCargando = true
            listaSeguimientos = RepositorioSeguimientoAppwrite.getFollowUps(idMascota)
            estaCargando = false
        }
    }

    fun loadFollowUps(petId: String) = cargarSeguimientos(petId)

    // RF11: Guardar reporte con fotos y notas de adaptación
    fun enviarReporte(
        contexto: Context,
        mascota: Mascota,
        usuarioActual: Usuario,
        notasSalud: String,
        notasAdaptacion: String,
        alTenerExito: () -> Unit
    ) {
        if (notasSalud.isBlank() && notasAdaptacion.isBlank()) {
            mensajeEstado = "Por favor ingresa notas sobre la salud o adaptación de la mascota"
            return
        }

        viewModelScope.launch {
            estaEnviando = true
            mensajeEstado = null

            val idsFotosSubidas = mutableListOf<String>()
            for (uri in fotosSeleccionadas) {
                val idArchivo = RepositorioAlmacenamientoAppwrite.uploadPhoto(contexto, uri)
                if (idArchivo != null) {
                    idsFotosSubidas.add(idArchivo)
                }
            }

            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoy = formatoFecha.format(Date())

            val nuevoReporte = SeguimientoMascota(
                petId = mascota.id,
                petName = mascota.name,
                applicantId = usuarioActual.id,
                applicantName = usuarioActual.name,
                shelterName = mascota.shelterName,
                date = fechaHoy,
                healthNotes = notasSalud.trim(),
                adaptationNotes = notasAdaptacion.trim(),
                photos = idsFotosSubidas,
                status = "REVISADO"
            )

            val exito = RepositorioSeguimientoAppwrite.createFollowUp(nuevoReporte)
            estaEnviando = false

            if (exito) {
                fotosSeleccionadas = emptyList()
                mensajeEstado = "¡Reporte de seguimiento registrado con éxito!"
                cargarSeguimientos(mascota.id)
                alTenerExito()
            } else {
                mensajeEstado = "Error al guardar reporte en la nube"
            }
        }
    }

    fun submitReport(
        context: Context,
        pet: Mascota,
        currentUser: Usuario,
        healthNotes: String,
        adaptationNotes: String,
        onSuccess: () -> Unit
    ) = enviarReporte(context, pet, currentUser, healthNotes, adaptationNotes, onSuccess)
}

typealias FollowUpViewModel = ModeloSeguimiento
