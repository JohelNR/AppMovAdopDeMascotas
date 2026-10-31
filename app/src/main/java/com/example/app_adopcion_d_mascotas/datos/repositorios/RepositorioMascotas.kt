package com.example.app_adopcion_d_mascotas.datos.repositorios

import androidx.compose.runtime.mutableStateListOf
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.PetStatus

// Repositorio en memoria local (Singleton)
object RepositorioMascotas {

    // Lista reactiva de Compose
    val mascotas = mutableStateListOf<Mascota>(
        Mascota(
            id = "1",
            name = "Max",
            breed = "Golden Retriever",
            species = "Perro",
            ageYears = 2,
            description = "Max es un perro muy juguetón, cariñoso y se lleva excelente con niños.",
            imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=500",
            shelterName = "Huellitas del Norte",
            isUrgent = true,
            status = EstadoMascota.DISPONIBLE
        ),
        Mascota(
            id = "2",
            name = "Luna",
            breed = "Siamés",
            species = "Gato",
            ageYears = 1,
            description = "Tranquila, curiosa y le encanta dormir en lugares cálidos.",
            imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
            shelterName = "Amor Animal",
            isUrgent = true,
            status = EstadoMascota.DISPONIBLE
        ),
        Mascota(
            id = "3",
            name = "Rocky",
            breed = "Mestizo",
            species = "Perro",
            ageYears = 3,
            description = "Rescatado hace un mes, súper noble y protector.",
            imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=500",
            shelterName = "Huellitas del Norte",
            isUrgent = false,
            status = EstadoMascota.DISPONIBLE
        ),
        Mascota(
            id = "4",
            name = "Mimi",
            breed = "Persa",
            species = "Gato",
            ageYears = 4,
            description = "Necesita adopción urgente por mudanza de su dueño anterior.",
            imageUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=500",
            shelterName = "Refugio Esperanza",
            isUrgent = true,
            status = EstadoMascota.DISPONIBLE
        )
    )

    val pets get() = mascotas

    fun obtenerMascotaPorId(id: String): Mascota? = mascotas.find { it.id == id }
    fun getPetById(id: String): Mascota? = obtenerMascotaPorId(id)

    fun agregarMascota(mascota: Mascota) { mascotas.add(mascota) }
    fun addPet(pet: Mascota) = agregarMascota(pet)

    fun actualizarMascota(mascotaActualizada: Mascota) {
        val index = mascotas.indexOfFirst { it.id == mascotaActualizada.id }
        if (index != -1) {
            mascotas[index] = mascotaActualizada
        }
    }
    fun updatePet(updatedPet: Mascota) = actualizarMascota(updatedPet)

    fun eliminarMascota(id: String) { mascotas.removeAll { it.id == id } }
    fun deletePet(id: String) = eliminarMascota(id)

    fun marcarComoAdoptada(id: String) {
        val index = mascotas.indexOfFirst { it.id == id }
        if (index != -1) {
            mascotas[index] = mascotas[index].copy(status = EstadoMascota.ADOPTADA)
        }
    }
    fun markAsAdopted(id: String) = marcarComoAdoptada(id)
}

typealias PetRepository = RepositorioMascotas
