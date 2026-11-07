package com.example.app_adopcion_d_mascotas.ui.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.PetStatus
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class ModeloCatalogo(private val RepositorioMascotasAppwrite: RepositorioMascotasAppwrite) : ViewModel() {

    var todasLasMascotas by mutableStateOf<List<Mascota>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set
    var mensajeError by mutableStateOf<String?>(null)
        private set

    // RF6: Filtros y Búsqueda combinables
    var textoBusqueda by mutableStateOf("")
    var especieSeleccionada by mutableStateOf("Todos")
    var tamanoSeleccionado by mutableStateOf("Todos")
    var sexoSeleccionado by mutableStateOf("Todos")

    // Compatibilidad en inglés
    var allPets: List<Mascota>
        get() = todasLasMascotas
        private set(value) { todasLasMascotas = value }

    val isLoading: Boolean get() = estaCargando
    var searchQuery: String get() = textoBusqueda; set(value) { textoBusqueda = value }
    var selectedSpecies: String get() = especieSeleccionada; set(value) { especieSeleccionada = value }
    var selectedSize: String get() = tamanoSeleccionado; set(value) { tamanoSeleccionado = value }
    var selectedGender: String get() = sexoSeleccionado; set(value) { sexoSeleccionado = value }

    val mascotasFiltradas: List<Mascota>
        get() = filteredPets

    val mascotasUrgentes: List<Mascota>
        get() = urgentPets

    val cantidadResultados: Int
        get() = resultCount

    init {
        cargarMascotas()
    }

    fun cargarMascotas() {
        if (estaCargando) return
        viewModelScope.launch {
            estaCargando = true
            mensajeError = null
            try {
                todasLasMascotas = RepositorioMascotasAppwrite.getPets()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mensajeError = "No se pudo cargar el catálogo: ${e.message}"
            } finally {
                estaCargando = false
            }
        }
    }

    fun loadPets() = cargarMascotas()

    // RF6: Lista filtrada reactiva que combina todos los filtros con AND lógico
    val filteredPets: List<Mascota>
        get() = todasLasMascotas.filter { pet ->
            val matchesStatus = pet.status == EstadoMascota.DISPONIBLE
            val matchesQuery = textoBusqueda.isBlank() ||
                    pet.name.contains(textoBusqueda, ignoreCase = true) ||
                    pet.breed.contains(textoBusqueda, ignoreCase = true) ||
                    pet.shelterName.contains(textoBusqueda, ignoreCase = true)

            val matchesSpecies = especieSeleccionada == "Todos" || pet.species.equals(especieSeleccionada, ignoreCase = true)
            val matchesSize = tamanoSeleccionado == "Todos" || pet.size.equals(tamanoSeleccionado, ignoreCase = true)
            val matchesGender = sexoSeleccionado == "Todos" || pet.gender.equals(sexoSeleccionado, ignoreCase = true)

            matchesStatus && matchesQuery && matchesSpecies && matchesSize && matchesGender
        }

    // RF5: Mascotas marcadas como caso urgente
    val urgentPets: List<Mascota>
        get() = todasLasMascotas.filter { it.isUrgent && it.status == EstadoMascota.DISPONIBLE }

    val resultCount: Int
        get() = filteredPets.size

    fun limpiarFiltros() {
        textoBusqueda = ""
        especieSeleccionada = "Todos"
        tamanoSeleccionado = "Todos"
        sexoSeleccionado = "Todos"
    }

    fun resetFilters() = limpiarFiltros()

    private suspend fun sembrarMascotasIniciales() {
        val samplePets = listOf(
            Mascota(
                name = "Max",
                breed = "Golden Retriever",
                species = "Perro",
                ageYears = 2,
                description = "Max es súper juguetón, sociable con niños y adora salir a pasear al parque.",
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
                shelterName = "Huellitas del Norte",
                isUrgent = true,
                gender = "Macho",
                size = "Grande",
                location = "Lima Norte"
            ),
            Mascota(
                name = "Luna",
                breed = "Siamés",
                species = "Gato",
                ageYears = 1,
                description = "Tranquila, cariñosa y ronronea cuando la acaricias. Esterilizada y con vacunas.",
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
                shelterName = "Amor Animal",
                isUrgent = true,
                gender = "Hembra",
                size = "Pequeño",
                location = "Miraflores"
            ),
            Mascota(
                name = "Rocky",
                breed = "Mestizo",
                species = "Perro",
                ageYears = 3,
                description = "Rescatado hace un mes, súper noble, obediente y protector del hogar.",
                imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600",
                shelterName = "Huellitas del Norte",
                isUrgent = false,
                gender = "Macho",
                size = "Mediano",
                location = "San Isidro"
            ),
            Mascota(
                name = "Mimi",
                breed = "Persa",
                species = "Gato",
                ageYears = 4,
                description = "Gatita muy dulce y tranquila. Necesita adopción urgente por mudanza de su dueño anterior.",
                imageUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=600",
                shelterName = "Refugio Esperanza",
                isUrgent = true,
                gender = "Hembra",
                size = "Pequeño",
                location = "Surco"
            )
        )

        for (pet in samplePets) {
            RepositorioMascotasAppwrite.createPet(pet)
        }
        todasLasMascotas = RepositorioMascotasAppwrite.getPets()
    }
}

typealias FeedViewModel = ModeloCatalogo
