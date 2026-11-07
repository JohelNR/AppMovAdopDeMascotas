package com.example.app_adopcion_d_mascotas.ui.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioChatAppwrite
import io.appwrite.ID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModeloChat(
    private val autenticacion: RepositorioAutenticacionAppwrite,
    private val chat: RepositorioChatAppwrite
) : ViewModel() {
    private val _mensajes = MutableStateFlow<List<MensajeChat>>(emptyList())
    val listaMensajes = _mensajes.asStateFlow()
    private val _texto = MutableStateFlow("")
    val textoEntrada = _texto.asStateFlow()
    private val _cargando = MutableStateFlow(false)
    val estaCargando = _cargando.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val mensajeError = _error.asStateFlow()
    private var idChat = ""
    private var nombreMascota = ""
    private var idDestinatario = ""
    private var enviando = false
    private var carga: Job? = null
    private var suscripcion: AutoCloseable? = null

    fun inicializarChat(idChat: String, nombreMascota: String, nombreDestinatario: String = "Refugio", idDestinatario: String = "") {
        carga?.cancel()
        suscripcion?.close()
        this.idChat = idChat
        this.nombreMascota = nombreMascota
        this.idDestinatario = idDestinatario
        _mensajes.value = emptyList()
        _error.value = null
        carga = viewModelScope.launch {
            _cargando.value = true
            try {
                if (this@ModeloChat.idDestinatario.isBlank()) {
                    this@ModeloChat.idDestinatario = autenticacion.buscarIdUsuario(nombreDestinatario)
                }
                suscripcion = chat.subscribeToChat(idChat) { mensaje ->
                    _mensajes.update { (it + mensaje).distinctBy { m -> m.id }.sortedBy { m -> m.timestamp } }
                }
                val historial = chat.getMessages(idChat)
                _mensajes.update { (historial + it).distinctBy { m -> m.id }.sortedBy { m -> m.timestamp } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo cargar el chat: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cambiarTextoMensaje(texto: String) { _texto.value = texto }
    fun onInputTextChanged(text: String) = cambiarTextoMensaje(text)
    fun enviarMensaje() {
        val usuario = autenticacion.usuarioActual.value ?: return
        val texto = _texto.value.trim()
        if (texto.isEmpty() || idChat.isBlank() || enviando || _cargando.value) return
        if (idDestinatario.isBlank()) {
            _error.value = "No se encontró el destinatario en Appwrite."
            return
        }
        val mensaje = MensajeChat(
            id = ID.unique(), chatId = idChat, petName = nombreMascota,
            senderId = usuario.id, senderName = usuario.name, receiverId = idDestinatario,
            text = texto, timestamp = System.currentTimeMillis(), isRead = false
        )
        enviando = true
        viewModelScope.launch {
            try {
                chat.sendMessage(mensaje)
                _mensajes.update { (it + mensaje).distinctBy { m -> m.id }.sortedBy { m -> m.timestamp } }
                if (_texto.value.trim() == texto) _texto.value = ""
                _error.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo enviar el mensaje: ${e.message}"
            } finally {
                enviando = false
            }
        }
    }

    fun sendMessage() = enviarMensaje()
    override fun onCleared() { suscripcion?.close() }
}

typealias ChatViewModel = ModeloChat
