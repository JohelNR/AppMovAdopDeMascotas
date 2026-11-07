package com.example.app_adopcion_d_mascotas.ui.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import kotlinx.coroutines.launch

/** La sesión vive en el repositorio compartido; aquí solo se conserva el estado del formulario. */
class ModeloAutenticacion(private val autenticacion: RepositorioAutenticacionAppwrite) : ViewModel() {
    var estaCargando by mutableStateOf(false)
        private set
    var sesionVerificada by mutableStateOf(false)
        private set
    var mensajeError by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            estaCargando = true
            autenticacion.getCurrentUser()
            estaCargando = false
            sesionVerificada = true
        }
    }

    fun iniciarSesion(correo: String, contrasena: String, alTenerExito: (Usuario) -> Unit) {
        if (estaCargando) return
        if (correo.isBlank() || contrasena.isBlank()) {
            mensajeError = "Por favor ingresa correo y contraseña"
            return
        }
        viewModelScope.launch {
            estaCargando = true
            mensajeError = null
            autenticacion.login(correo.trim(), contrasena).fold(
                onSuccess = alTenerExito,
                onFailure = { mensajeError = "Error al iniciar sesión: ${it.message}" }
            )
            estaCargando = false
        }
    }

    fun registrarse(nombre: String, correo: String, contrasena: String, rol: RolUsuario, alTenerExito: (Usuario) -> Unit) {
        if (estaCargando) return
        if (nombre.isBlank() || correo.isBlank() || contrasena.length < 8) {
            mensajeError = "Completa los campos y utiliza una contraseña de al menos 8 caracteres"
            return
        }
        viewModelScope.launch {
            estaCargando = true
            mensajeError = null
            autenticacion.register(nombre.trim(), correo.trim(), contrasena, rol).fold(
                onSuccess = alTenerExito,
                onFailure = { mensajeError = "Error al registrarse: ${it.message}" }
            )
            estaCargando = false
        }
    }
}

typealias AuthViewModel = ModeloAutenticacion
