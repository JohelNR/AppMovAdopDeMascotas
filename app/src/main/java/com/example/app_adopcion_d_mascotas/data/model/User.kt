package com.example.app_adopcion_d_mascotas.data.model

//Roles unicos de los usuarios
enum class UserRole {
    ADOPTANTE,
    REFUGIO
}

//Infromación del usuario
data class User(
    val id: String, //-> El id es String por que en Firebase son códigos alfanumericos
    val name: String,
    val email: String,
    val role: UserRole
)