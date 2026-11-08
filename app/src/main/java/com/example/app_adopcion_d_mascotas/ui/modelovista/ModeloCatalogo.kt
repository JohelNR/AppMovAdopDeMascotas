package com.example.app_adopcion_d_mascotas.ui.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import kotlinx.coroutines.launch

/*
 * ============================================================================
 * ARCHIVO: ModeloCatalogo.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF4 y RF5: Catálogo, Búsqueda y Filtros)
 * ¿QUÉ HACE?: Descarga las mascotas reales desde la tabla "mascotas" en
 *             Appwrite Cloud y aplica los filtros combinados en tiempo real:
 *             - Búsqueda por texto (nombre, raza, refugio o ciudad)
 *             - Especie (Todos, Perro, Gato, Otro)
 *             - Tamaño (Todos, Pequeño, Mediano, Grande)
 *             - Sexo (Todos, Macho, Hembra)
 *             - Solo Casos Urgentes (interruptor opcional)
 * ============================================================================
 */
class ModeloCatalogo : ViewModel() {

    // 1. Lista completa descargada desde Appwrite Cloud
    var todasLasMascotas by mutableStateOf<List<Mascota>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set

    // 2. Estados de los filtros seleccionados por el usuario (RF5)
    var textoBusqueda by mutableStateOf("")
    var especieSeleccionada by mutableStateOf("Todos")
    var tamanoSeleccionado by mutableStateOf("Todos")
    var sexoSeleccionado by mutableStateOf("Todos")
    var soloUrgentes by mutableStateOf(false)

    // Alias de compatibilidad
    var allPets: List<Mascota>
        get() = todasLasMascotas
        private set(value) { todasLasMascotas = value }
    val isLoading: Boolean get() = estaCargando
    var searchQuery: String get() = textoBusqueda; set(value) { textoBusqueda = value }
    var selectedSpecies: String get() = especieSeleccionada; set(value) { especieSeleccionada = value }
    var selectedSize: String get() = tamanoSeleccionado; set(value) { tamanoSeleccionado = value }
    var selectedGender: String get() = sexoSeleccionado; set(value) { sexoSeleccionado = value }

    init {
        cargarMascotas()
    }

    /*
     * RF4: Descarga todas las mascotas desde la tabla "mascotas" de Appwrite Cloud
     */
    fun cargarMascotas() {
        viewModelScope.launch {
            estaCargando = true
            todasLasMascotas = RepositorioMascotasAppwrite.obtenerMascotas()
            estaCargando = false
        }
    }

    fun loadPets() = cargarMascotas()

    /*
     * RF5: Calcula las mascotas que cumplen TODOS los filtros activos al mismo tiempo
     */
    val mascotasFiltradas: List<Mascota>
        get() = todasLasMascotas.filter { mascota ->
            // Filtro 1: Que no esté ya adoptada
            val estaDisponible = mascota.estado != EstadoMascota.ADOPTADA

            // Filtro 2: Coincidencia con el texto del buscador
            val coincideTexto = textoBusqueda.isBlank() ||
                mascota.nombre.contains(textoBusqueda, ignoreCase = true) ||
                mascota.raza.contains(textoBusqueda, ignoreCase = true) ||
                mascota.nombreRefugio.contains(textoBusqueda, ignoreCase = true) ||
                mascota.ubicacion.contains(textoBusqueda, ignoreCase = true)

            // Filtro 3: Coincidencia por Especie (Perro, Gato, Otro)
            val coincideEspecie = especieSeleccionada == "Todos" ||
                mascota.especie.equals(especieSeleccionada, ignoreCase = true)

            // Filtro 4: Coincidencia por Tamaño (Pequeño, Mediano, Grande)
            val coincideTamano = tamanoSeleccionado == "Todos" ||
                mascota.tamano.equals(tamanoSeleccionado, ignoreCase = true)

            // Filtro 5: Coincidencia por Sexo (Macho, Hembra)
            val coincideSexo = sexoSeleccionado == "Todos" ||
                mascota.sexo.equals(sexoSeleccionado, ignoreCase = true)

            // Filtro 6: Si activó el chip "Solo Urgentes"
            val coincideUrgencia = !soloUrgentes || mascota.esUrgente

            estaDisponible && coincideTexto && coincideEspecie && coincideTamano && coincideSexo && coincideUrgencia
        }

    val filteredPets: List<Mascota> get() = mascotasFiltradas

    /*
     * RF5: Lista de mascotas marcadas como caso urgente para el carrusel superior
     */
    val mascotasUrgentes: List<Mascota>
        get() = todasLasMascotas.filter { it.esUrgente && it.estado != EstadoMascota.ADOPTADA }

    val urgentPets: List<Mascota> get() = mascotasUrgentes

    val cantidadResultados: Int
        get() = mascotasFiltradas.size

    val resultCount: Int get() = cantidadResultados

    /*
     * Restablece todos los filtros a su estado inicial ("Todos")
     */
    fun limpiarFiltros() {
        textoBusqueda = ""
        especieSeleccionada = "Todos"
        tamanoSeleccionado = "Todos"
        sexoSeleccionado = "Todos"
        soloUrgentes = false
    }

    fun resetFilters() = limpiarFiltros()
}

typealias FeedViewModel = ModeloCatalogo
