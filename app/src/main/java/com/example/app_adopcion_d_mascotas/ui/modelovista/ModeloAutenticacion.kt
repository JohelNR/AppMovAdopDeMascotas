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

/*
 * ============================================================================
 * ARCHIVO: ModeloAutenticacion.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF1: Registro e Inicio de Sesión)
 * ¿QUÉ HACE?: Es el "cerebro" de la pantalla de Login y Registro.
 *             1. Valida que el correo, nombre y contraseña sean correctos.
 *             2. Llama al Repositorio para guardar o consultar en Appwrite.
 *             3. Guarda qué usuario está conectado y su rol (ADOPTANTE o REFUGIO).
 * ============================================================================
 */
class ModeloAutenticacion : ViewModel() {

    // 1. Estados observables por la pantalla
    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Expresión regular simple y clara para validar correos electrónicos (ej. nombre@dominio.com)
    private val patronCorreoValido = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Alias de compatibilidad
    var currentUser: Usuario?
        get() = usuarioActual
        private set(value) { usuarioActual = value }
    val isLoading: Boolean get() = estaCargando
    val errorMessage: String? get() = mensajeError

    init {
        verificarSesion()
    }

    /*
     * Limpia el mensaje de error cuando el usuario empieza a escribir de nuevo
     */
    fun limpiarError() {
        mensajeError = null
    }

    /*
     * Verifica si ya existe una sesión activa en Appwrite Cloud al abrir la app
     */
    fun verificarSesion(alRecuperarSesion: ((Usuario) -> Unit)? = null) {
        viewModelScope.launch {
            estaCargando = true
            val usuarioRecuperado = RepositorioAutenticacionAppwrite.obtenerUsuarioActual()
            usuarioActual = usuarioRecuperado
            estaCargando = false
            if (usuarioRecuperado != null) {
                alRecuperarSesion?.invoke(usuarioRecuperado)
            }
        }
    }

    fun checkSession() = verificarSesion()

    /*
     * RF1: Iniciar sesión validando primero los datos escritos por el usuario
     */
    fun iniciarSesion(
        correo: String,
        contrasena: String,
        alTenerExito: (Usuario) -> Unit
    ) {
        val correoLimpio = correo.trim()

        // Paso 1: Validar que no haya campos vacíos
        if (correoLimpio.isEmpty() || contrasena.isEmpty()) {
            mensajeError = "Por favor, ingresa tu correo electrónico y contraseña."
            return
        }

        // Paso 2: Validar que el correo tenga formato válido (@ y dominio)
        if (!patronCorreoValido.matches(correoLimpio)) {
            mensajeError = "Ingresa un correo electrónico válido (ejemplo: usuario@correo.com)."
            return
        }

        // Paso 3: Validar longitud mínima de contraseña
        if (contrasena.length < 8) {
            mensajeError = "La contraseña debe tener al menos 8 caracteres."
            return
        }

        // Paso 4: Conectar con Appwrite Cloud para iniciar sesión
        viewModelScope.launch {
            estaCargando = true
            mensajeError = null

            val resultado = RepositorioAutenticacionAppwrite.iniciarSesion(correoLimpio, contrasena)
            estaCargando = false

            resultado.fold(
                onSuccess = { usuario ->
                    usuarioActual = usuario
                    alTenerExito(usuario)
                },
                onFailure = { error ->
                    mensajeError = traducirErrorAppwrite(error.message)
                }
            )
        }
    }

    fun login(email: String, password: String, onSuccess: (Usuario) -> Unit) =
        iniciarSesion(email, password, onSuccess)

    /*
     * RF1: Registrar una nueva cuenta (ADOPTANTE o REFUGIO) en Appwrite Cloud
     */
    fun registrarse(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: RolUsuario,
        alTenerExito: (Usuario) -> Unit
    ) {
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()

        // Paso 1: Validar que todos los campos estén completos
        if (nombreLimpio.isEmpty() || correoLimpio.isEmpty() || contrasena.isEmpty()) {
            mensajeError = "Por favor, completa tu nombre, correo y contraseña."
            return
        }

        // Paso 2: Validar que el nombre tenga al menos 3 letras reales
        if (nombreLimpio.length < 3) {
            mensajeError = "El nombre debe tener al menos 3 caracteres."
            return
        }

        // Paso 3: Validar formato de correo electrónico
        if (!patronCorreoValido.matches(correoLimpio)) {
            mensajeError = "El formato del correo no es válido (ejemplo: refugio@correo.com)."
            return
        }

        // Paso 4: Validar que la contraseña tenga mínimo 8 caracteres (requisito de Appwrite)
        if (contrasena.length < 8) {
            mensajeError = "La contraseña debe tener mínimo 8 caracteres."
            return
        }

        // Paso 5: Crear la cuenta y guardar su rol (ADOPTANTE o REFUGIO) en la tabla "usuarios"
        viewModelScope.launch {
            estaCargando = true
            mensajeError = null

            val resultado = RepositorioAutenticacionAppwrite.registrarUsuario(
                nombre = nombreLimpio,
                correo = correoLimpio,
                contrasena = contrasena,
                rol = rol
            )
            estaCargando = false

            resultado.fold(
                onSuccess = { usuarioCreado ->
                    usuarioActual = usuarioCreado
                    alTenerExito(usuarioCreado)
                },
                onFailure = { error ->
                    mensajeError = traducirErrorAppwrite(error.message)
                }
            )
        }
    }

    fun register(name: String, email: String, password: String, role: RolUsuario, onSuccess: (Usuario) -> Unit) =
        registrarse(name, email, password, role, onSuccess)

    /*
     * Cerrar la sesión actual en Appwrite Cloud
     */
    fun cerrarSesion(alCerrar: () -> Unit) {
        viewModelScope.launch {
            RepositorioAutenticacionAppwrite.cerrarSesion()
            usuarioActual = null
            alCerrar()
        }
    }

    fun logout(onLoggedOut: () -> Unit) = cerrarSesion(onLoggedOut)

    /*
     * Traduce los mensajes técnicos de Appwrite a mensajes amigables en español para el usuario
     */
    private fun traducirErrorAppwrite(detalle: String?): String {
        val texto = detalle?.lowercase() ?: ""
        return when {
            texto.contains("already exists") || texto.contains("409") ->
                "Este correo ya está registrado. Cambia a 'Iniciar Sesión' para entrar con tu cuenta."
            texto.contains("invalid credentials") || texto.contains("401") ->
                "Correo o contraseña incorrectos. Verifica tus datos e inténtalo de nuevo."
            texto.contains("rate limit") || texto.contains("429") ->
                "Demasiados intentos seguidos. Espera unos segundos y vuelve a intentar."
            texto.contains("network") || texto.contains("unable to resolve host") ->
                "Sin conexión a internet. Revisa tu red Wi-Fi o datos móviles."
            else -> "No se pudo completar la operación: ${detalle ?: "Verifica tus datos"}"
        }
    }
}

typealias AuthViewModel = ModeloAutenticacion
