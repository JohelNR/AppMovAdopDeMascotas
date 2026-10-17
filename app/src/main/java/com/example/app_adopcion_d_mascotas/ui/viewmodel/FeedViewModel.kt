package com.example.app_adopcion_d_mascotas.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetStatus
import com.example.app_adopcion_d_mascotas.data.repository.AppwritePetRepository
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    var allPets by mutableStateOf<List<Pet>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // RF6: Filtros y Búsqueda combinables
    var searchQuery by mutableStateOf("")
    var selectedSpecies by mutableStateOf("Todos")
    var selectedSize by mutableStateOf("Todos")
    var selectedGender by mutableStateOf("Todos")

    init {
        loadPets()
    }

    fun loadPets() {
        viewModelScope.launch {
            isLoading = true
            val petsFromCloud = AppwritePetRepository.getPets()
            if (petsFromCloud.isEmpty()) {
                // Si la nube está vacía al iniciar por primera vez, sembramos datos de ejemplo reales
                seedInitialPetsIfEmpty()
            } else {
                allPets = petsFromCloud
            }
            isLoading = false
        }
    }

    // RF6: Lista filtrada reactiva que combina todos los filtros con AND lógico
    val filteredPets: List<Pet>
        get() = allPets.filter { pet ->
            val matchesStatus = pet.status == PetStatus.DISPONIBLE
            val matchesQuery = searchQuery.isBlank() ||
                    pet.name.contains(searchQuery, ignoreCase = true) ||
                    pet.breed.contains(searchQuery, ignoreCase = true) ||
                    pet.shelterName.contains(searchQuery, ignoreCase = true)

            val matchesSpecies = selectedSpecies == "Todos" || pet.species.equals(selectedSpecies, ignoreCase = true)
            val matchesSize = selectedSize == "Todos" || pet.size.equals(selectedSize, ignoreCase = true)
            val matchesGender = selectedGender == "Todos" || pet.gender.equals(selectedGender, ignoreCase = true)

            matchesStatus && matchesQuery && matchesSpecies && matchesSize && matchesGender
        }

    // RF5: Mascotas marcadas como caso urgente
    val urgentPets: List<Pet>
        get() = allPets.filter { it.isUrgent && it.status == PetStatus.DISPONIBLE }

    // Contador coherente exigido en el criterio de aceptación del RF6
    val resultCount: Int
        get() = filteredPets.size

    fun resetFilters() {
        searchQuery = ""
        selectedSpecies = "Todos"
        selectedSize = "Todos"
        selectedGender = "Todos"
    }

    private suspend fun seedInitialPetsIfEmpty() {
        val samplePets = listOf(
            Pet(
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
            Pet(
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
            Pet(
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
            Pet(
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
            AppwritePetRepository.createPet(pet)
        }
        allPets = AppwritePetRepository.getPets()
    }
}
