package com.example.app_adopcion_d_mascotas.ui.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.ChatMessage
import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioChatAppwrite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// RF13: ViewModel para gestión del Chat en tiempo real
class ModeloChat : ViewModel() {

    private val _mensajes = MutableStateFlow<List<MensajeChat>>(emptyList())
    val listaMensajes: StateFlow<List<MensajeChat>> = _mensajes.asStateFlow()

    private val _textoEntrada = MutableStateFlow("")
    val textoEntrada: StateFlow<String> = _textoEntrada.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    // Compatibilidad en inglés
    val messages: StateFlow<List<MensajeChat>> get() = listaMensajes
    val inputText: StateFlow<String> get() = textoEntrada
    val isLoading: StateFlow<Boolean> get() = estaCargando

    private var currentChatId: String = ""
    private var currentPetName: String = ""
    private var currentReceiverName: String = "Refugio"
    private var currentReceiverId: String = ""
    private var realtimeSubscription: AutoCloseable? = null

    fun inicializarChat(idChat: String, nombreMascota: String, nombreDestinatario: String = "Refugio", idDestinatario: String = "") {
        currentChatId = idChat
        currentPetName = nombreMascota
        currentReceiverName = nombreDestinatario
        currentReceiverId = idDestinatario

        cargarMensajes()
        configurarTiempoReal()
    }

    fun initChat(chatId: String, petName: String, receiverName: String = "Refugio", receiverId: String = "") =
        inicializarChat(chatId, petName, receiverName, receiverId)

    fun cambiarTextoMensaje(nuevoTexto: String) {
        _textoEntrada.value = nuevoTexto
    }

    fun onInputTextChanged(text: String) = cambiarTextoMensaje(text)

    private fun cargarMensajes() {
        viewModelScope.launch {
            _estaCargando.value = true
            val lista = RepositorioChatAppwrite.getMessages(currentChatId, currentPetName)
            _mensajes.value = lista
            _estaCargando.value = false
        }
    }

    private fun configurarTiempoReal() {
        realtimeSubscription?.close()
        realtimeSubscription = RepositorioChatAppwrite.subscribeToChat(currentChatId) { nuevoMensaje ->
            val actual = _mensajes.value
            if (actual.none { it.id == nuevoMensaje.id }) {
                _mensajes.value = actual + nuevoMensaje
            }
        }
    }

    fun enviarMensaje() {
        val texto = _textoEntrada.value.trim()
        if (texto.isEmpty() || currentChatId.isEmpty()) return

        val usuarioActual = RepositorioAutenticacionAppwrite.usuarioActual.value
        val idRemitente = usuarioActual?.id ?: "user_me"
        val nombreRemitente = usuarioActual?.name ?: "Adoptante"

        val nuevoMensaje = MensajeChat(
            id = "msg_${System.currentTimeMillis()}",
            chatId = currentChatId,
            petName = currentPetName,
            senderId = idRemitente,
            senderName = nombreRemitente,
            receiverId = currentReceiverId.ifEmpty { "shelter_host" },
            text = texto,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        _textoEntrada.value = ""
        _mensajes.value = _mensajes.value + nuevoMensaje

        viewModelScope.launch {
            RepositorioChatAppwrite.sendMessage(nuevoMensaje)

            // Simular respuesta automática amigable si responde el refugio
            if (idRemitente != "shelter_host") {
                delay(2000)
                val respuesta = MensajeChat(
                    id = "reply_${System.currentTimeMillis()}",
                    chatId = currentChatId,
                    petName = currentPetName,
                    senderId = "shelter_host",
                    senderName = currentReceiverName,
                    receiverId = idRemitente,
                    text = "¡Recibido! Estamos encantados de coordinar contigo. Por favor agenda también una cita desde la app para tener tu espacio reservado 🐶.",
                    timestamp = System.currentTimeMillis(),
                    isRead = true
                )
                RepositorioChatAppwrite.sendMessage(respuesta)
                _mensajes.value = _mensajes.value + respuesta
            }
        }
    }

    fun sendMessage() = enviarMensaje()

    override fun onCleared() {
        super.onCleared()
        realtimeSubscription?.close()
    }
}

typealias ChatViewModel = ModeloChat
