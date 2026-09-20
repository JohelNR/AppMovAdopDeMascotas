package com.example.app_adopcion_d_mascotas.data.model

//Roles unicos de la mascota
enum class PetStatus{
    DISPONIBLE,
    ADOPTADA
}

//Datos de la mascota
data class Pet(
    val id: String,
    val name: String,
    val breed: String,             //Raza
    val species: String,           //Perro o Gato
    val ageYears: Int,             //Edad
    val description: String,       //historia de la mascota
    val imageUrl: String,          //Foto
    val shelterName: String,       //Nombre del refugio
    val isUrgent: Boolean = false, //RF5: ¿Es caso urgente?
    val status: PetStatus = PetStatus.DISPONIBLE //Una opcion por defecto
)