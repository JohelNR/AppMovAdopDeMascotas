package com.example.app_adopcion_d_mascotas.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetStatus

// Usamos "object" (Singleton) para tener una única lista de datos en memoria RAM
object PetRepository {

    // Lista reactiva de Jetpack Compose: si cambia, la pantalla se redibuja sola
    val pets = mutableStateListOf<Pet>(
        Pet(
            id = "1",
            name = "Max",
            breed = "Golden Retriever",
            species = "Perro",
            ageYears = 2,
            description = "Max es un perro muy juguetón, cariñoso y se lleva excelente con niños.",
            imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=500",
            shelterName = "Huellitas del Norte",
            isUrgent = true,
            status = PetStatus.DISPONIBLE
        ),
        Pet(
            id = "2",
            name = "Luna",
            breed = "Siamés",
            species = "Gato",
            ageYears = 1,
            description = "Tranquila, curiosa y le encanta dormir en lugares cálidos.",
            imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
            shelterName = "Amor Animal",
            isUrgent = false,
            status = PetStatus.DISPONIBLE
        ),
        Pet(
            id = "3",
            name = "Rocky",
            breed = "Mestizo",
            species = "Perro",
            ageYears = 3,
            description = "Rescatado hace un mes, súper noble y protector.",
            imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=500",
            shelterName = "Huellitas del Norte",
            isUrgent = false,
            status = PetStatus.DISPONIBLE
        ),
        Pet(
            id = "4",
            name = "Mimi",
            breed = "Persa",
            species = "Gato",
            ageYears = 4,
            description = "Necesita adopción urgente por mudanza de su dueño anterior.",
            imageUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=500",
            shelterName = "Refugio Esperanza",
            isUrgent = true,
            status = PetStatus.DISPONIBLE
        )
    )

    // Funciones del CRUD (RF3):

    // Buscar una mascota por su ID para la pantalla de detalle
    fun getPetById(id: String): Pet? {
        return pets.find { it.id == id }
    }

    // Agregar una nueva mascota publicada por el refugio
    fun addPet(pet: Pet) {
        pets.add(pet)
    }

    // Editar los datos de una mascota existente
    fun updatePet(updatedPet: Pet) {
        val index = pets.indexOfFirst { it.id == updatedPet.id }
        if (index != -1) {
            pets[index] = updatedPet
        }
    }

    // Eliminar una publicación de mascota
    fun deletePet(id: String) {
        pets.removeAll { it.id == id }
    }

    // Marcar como Adoptada usando .copy()
    fun markAsAdopted(id: String) {
        val index = pets.indexOfFirst { it.id == id }
        if (index != -1) {
            pets[index] = pets[index].copy(status = PetStatus.ADOPTADA)
        }
    }
}