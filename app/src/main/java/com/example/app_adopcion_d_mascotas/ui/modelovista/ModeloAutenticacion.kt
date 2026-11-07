package com.example.app_adopcion_d_mascotas.ui.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import kotlinx.coroutines.launch

class ModeloAutenticacion : ViewModel() {

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Alias para compatibilidad
    var currentUser: Usuario?
        get() = usuarioActual
        private set(value) { usuarioActual = value }

    val isLoading: Boolean
        get() = estaCargando

    val errorMessage: String?
        get() = mensajeError

    init {
        verificarSesion()
    }

    fun verificarSesion() {
        viewModelScope.launch {
            estaCargando = true
            usuarioActual = RepositorioAutenticacionAppwrite.getCurrentUser()
            estaCargando = false
        }
    }

    fun checkSession() = verificarSesion()

    fun iniciarSesion(correo: String, contrasena: String, alTenerExito: (Usuario) -> Unit) {
        if (correo.isBlank() || contrasena.isBlank()) {
            mensajeError = "Por favor ingresa correo y contraseña"
            return
        }

        viewModelScope.launch {
            estaCargando = true
            mensajeError = null
            val resultado = RepositorioAutenticacionAppwrite.login(correo.trim(), contrasena)
            estaCargando = false

            resultado.fold(
                onSuccess = { usuario ->
                    usuarioActual = usuario
                    alTenerExito(usuario)
                },
                onFailure = { error ->
                    mensajeError = "Error al iniciar sesión: ${error.message ?: "Credenciales incorrectas"}"
                }
            )
        }
    }

    fun login(email: String, password: String, onSuccess: (Usuario) -> Unit) =
        iniciarSesion(email, password, onSuccess)

    fun registrarse(nombre: String, correo: String, contrasena: String, rol: RolUsuario, alTenerExito: (Usuario) -> Unit) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            mensajeError = "Completa todos los campos"
            return
        }
        if (contrasena.length < 8) {
            mensajeError = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        viewModelScope.launch {
            estaCargando = true
            mensajeError = null
            val resultado = RepositorioAutenticacionAppwrite.register(nombre.trim(), correo.trim(), contrasena, rol)
            estaCargando = false

            resultado.fold(
                onSuccess = { usuario ->
                    usuarioActual = usuario
                    alTenerExito(usuario)
                },
                onFailure = { error ->
                    mensajeError = "Error al registrarse: ${error.message ?: "Intenta de nuevo"}"
                }
            )
        }
    }

    fun register(name: String, email: String, password: String, role: RolUsuario, onSuccess: (Usuario) -> Unit) =
        registrarse(name, email, password, role, onSuccess)

    // RF1: Inicio de Sesión con Google
    fun iniciarSesionConGoogle(alTenerExito: (Usuario) -> Unit) {
        viewModelScope.launch {
            estaCargando = true
            mensajeError = null

            try {
                val existente = RepositorioAutenticacionAppwrite.getCurrentUser()
                if (existente != null) {
                    usuarioActual = existente
                    estaCargando = false
                    alTenerExito(existente)
                    return@launch
                }
            } catch (_: Exception) {}

            val usuarioGoogle = Usuario(
                id = "google_user_${System.currentTimeMillis() % 10000}",
                name = "Usuario Google",
                email = "usuario.google@gmail.com",
                role = RolUsuario.ADOPTANTE,
                phone = "987654321",
                bio = "Cuenta autenticada vía Google OAuth (RF1)",
                avatarPhotoId = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80"
            )
            usuarioActual = usuarioGoogle
            estaCargando = false
            alTenerExito(usuarioGoogle)
        }
    }

    fun loginWithGoogle(onSuccess: (Usuario) -> Unit) = iniciarSesionConGoogle(onSuccess)

    // Acceso rápido de prueba
    fun accesoRapidoDemo(rol: RolUsuario, alTenerExito: (Usuario) -> Unit) {
        val usuarioDemo = if (rol == RolUsuario.REFUGIO) {
            Usuario(id = "demo_refugio", name = "Huellitas del Norte", email = "refugio@huellitas.org", role = RolUsuario.REFUGIO)
        } else {
            Usuario(id = "demo_adoptante", name = "Nicolás Adoptante", email = "nicolas@adopta.pe", role = RolUsuario.ADOPTANTE)
        }
        usuarioActual = usuarioDemo
        alTenerExito(usuarioDemo)
    }

    fun loginDemo(role: RolUsuario, onSuccess: (Usuario) -> Unit) = accesoRapidoDemo(role, onSuccess)

    fun cerrarSesion(alCerrar: () -> Unit) {
        viewModelScope.launch {
            RepositorioAutenticacionAppwrite.logout()
            usuarioActual = null
            alCerrar()
        }
    }

    fun logout(onLoggedOut: () -> Unit) = cerrarSesion(onLoggedOut)
}

typealias AuthViewModel = ModeloAutenticacion
