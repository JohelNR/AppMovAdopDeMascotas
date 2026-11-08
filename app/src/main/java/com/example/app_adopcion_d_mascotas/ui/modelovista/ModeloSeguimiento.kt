package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.SeguimientoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAlmacenamientoAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioSeguimientoAppwrite
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * ============================================================================
 * ARCHIVO: ModeloSeguimiento.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF11: Seguimiento Post-Adopción)
 * ¿QUÉ HACE?: Permite al adoptante subir reportes de salud, adaptación y
 *             fotos recortadas a la tabla "seguimientos" de Appwrite Cloud,
 *             y permite al refugio consultarlos y marcarlos como revisados.
 * ============================================================================
 */
class ModeloSeguimiento : ViewModel() {

    var listaSeguimientos by mutableStateOf<List<SeguimientoMascota>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var estaEnviando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)

    var fotosSeleccionadas by mutableStateOf<List<Uri>>(emptyList())

    // Alias de compatibilidad
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

    /*
     * Agrega una foto recortada a la lista de evidencias del reporte (máximo 3)
     */
    fun agregarFotoAjustada(uri: Uri) {
        if (fotosSeleccionadas.size < 3) {
            fotosSeleccionadas = fotosSeleccionadas + uri
        } else {
            mensajeEstado = "Puedes adjuntar hasta 3 fotos por reporte."
        }
    }

    fun seleccionarFotos(uris: List<Uri>) {
        fotosSeleccionadas = (fotosSeleccionadas + uris).take(3)
    }

    fun onPhotosSelected(uris: List<Uri>) = seleccionarFotos(uris)

    fun eliminarFoto(indice: Int) {
        if (indice in fotosSeleccionadas.indices) {
            fotosSeleccionadas = fotosSeleccionadas.toMutableList().also { it.removeAt(indice) }
        }
    }

    fun removePhoto(index: Int) = eliminarFoto(index)

    /*
     * RF11: Descarga todos los reportes de seguimiento de la mascota desde Appwrite Cloud
     */
    fun cargarSeguimientos(idMascota: String) {
        viewModelScope.launch {
            estaCargando = true
            listaSeguimientos = RepositorioSeguimientoAppwrite.obtenerSeguimientos(idMascota)
            estaCargando = false
        }
    }

    fun loadFollowUps(petId: String) = cargarSeguimientos(petId)

    /*
     * RF11: Valida y guarda un nuevo reporte fotográfico en la tabla "seguimientos"
     */
    fun enviarReporte(
        contexto: Context,
        mascota: Mascota,
        usuarioActual: Usuario,
        notasSalud: String,
        notasAdaptacion: String,
        alTenerExito: () -> Unit
    ) {
        val saludLimpia = notasSalud.trim()
        val adaptacionLimpia = notasAdaptacion.trim()

        // Paso 1: Validar que haya escrito información real sobre salud y adaptación
        if (saludLimpia.length < 5 && adaptacionLimpia.length < 5) {
            mensajeEstado = "Escribe al menos un detalle sobre la salud o adaptación de ${mascota.nombre}."
            return
        }

        viewModelScope.launch {
            estaEnviando = true
            mensajeEstado = null

            // Paso 2: Subir las fotos adjuntas al bucket "fotos_mascotas" de Appwrite Storage
            val idsFotosSubidas = mutableListOf<String>()
            for (uri in fotosSeleccionadas) {
                val idArchivo = RepositorioAlmacenamientoAppwrite.subirFoto(contexto, uri)
                if (idArchivo != null) {
                    idsFotosSubidas.add(idArchivo)
                }
            }

            // Paso 3: Obtener la fecha actual en formato DD/MM/AAAA
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoy = formatoFecha.format(Date())

            // Paso 4: Construir el reporte y guardarlo en la tabla "seguimientos"
            val nuevoReporte = SeguimientoMascota(
                petId = mascota.id,
                petName = mascota.nombre,
                applicantId = usuarioActual.id,
                applicantName = usuarioActual.nombre,
                shelterName = mascota.nombreRefugio,
                date = fechaHoy,
                healthNotes = saludLimpia.ifEmpty { "Sin observaciones adicionales" },
                adaptationNotes = adaptacionLimpia.ifEmpty { "En buena adaptación al hogar" },
                photos = idsFotosSubidas,
                status = "ENVIADO"
            )

            val exito = RepositorioSeguimientoAppwrite.crearSeguimiento(nuevoReporte)
            estaEnviando = false

            if (exito) {
                fotosSeleccionadas = emptyList()
                mensajeEstado = "¡Reporte enviado a la nube con éxito!"
                cargarSeguimientos(mascota.id)
                alTenerExito()
            } else {
                mensajeEstado = "Error al guardar el reporte en Appwrite Cloud."
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

    /*
     * Permite al Refugio confirmar que revisó el reporte de seguimiento
     */
    fun marcarReporteComoRevisado(idReporte: String, idMascota: String) {
        viewModelScope.launch {
            val exito = RepositorioSeguimientoAppwrite.marcarComoRevisado(idReporte)
            if (exito) {
                mensajeEstado = "Reporte marcado como REVISADO."
                cargarSeguimientos(idMascota)
            }
        }
    }
}

typealias FollowUpViewModel = ModeloSeguimiento
