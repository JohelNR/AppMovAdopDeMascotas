package com.example.app_adopcion_d_mascotas.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteAuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            isLoading = true
            currentUser = AppwriteAuthRepository.getCurrentUser()
            isLoading = false
        }
    }

    fun login(email: String, password: String, onSuccess: (User) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Por favor ingresa correo y contraseña"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = AppwriteAuthRepository.login(email.trim(), password)
            isLoading = false

            result.fold(
                onSuccess = { user ->
                    currentUser = user
                    onSuccess(user)
                },
                onFailure = { error ->
                    errorMessage = "Error al iniciar sesión: ${error.message ?: "Credenciales incorrectas"}"
                }
            )
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole, onSuccess: (User) -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Completa todos los campos"
            return
        }
        if (password.length < 8) {
            errorMessage = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = AppwriteAuthRepository.register(name.trim(), email.trim(), password, role)
            isLoading = false

            result.fold(
                onSuccess = { user ->
                    currentUser = user
                    onSuccess(user)
                },
                onFailure = { error ->
                    errorMessage = "Error al registrarse: ${error.message ?: "Intenta de nuevo"}"
                }
            )
        }
    }

    // Acceso rápido simulado para pruebas de presentación y rúbrica
    fun loginDemo(role: UserRole, onSuccess: (User) -> Unit) {
        val demoUser = if (role == UserRole.REFUGIO) {
            User(id = "demo_refugio", name = "Huellitas del Norte", email = "refugio@huellitas.org", role = UserRole.REFUGIO)
        } else {
            User(id = "demo_adoptante", name = "Nicolás Adoptante", email = "nicolas@adopta.pe", role = UserRole.ADOPTANTE)
        }
        currentUser = demoUser
        onSuccess(demoUser)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            AppwriteAuthRepository.logout()
            currentUser = null
            onLoggedOut()
        }
    }
}
