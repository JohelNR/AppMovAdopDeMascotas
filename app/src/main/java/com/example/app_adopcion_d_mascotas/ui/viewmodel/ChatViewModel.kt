package com.example.app_adopcion_d_mascotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.data.model.ChatMessage
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteAuthRepository
import com.example.app_adopcion_d_mascotas.data.repository.AppwriteChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// RF13: ViewModel para gestión del Chat en tiempo real
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Alias en español para fácil lectura y explicación
    val listaMensajes: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    val textoEntrada: StateFlow<String> = _inputText.asStateFlow()
    val estaCargando: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun cambiarTextoMensaje(nuevoTexto: String) = onInputTextChanged(nuevoTexto)
    fun enviarMensaje() = sendMessage()
    fun inicializarChat(idChat: String, nombreMascota: String, nombreDestinatario: String = "Refugio") =
        initChat(idChat, nombreMascota, nombreDestinatario)

    private var currentChatId: String = ""
    private var currentPetName: String = ""
    private var currentReceiverName: String = "Refugio"
    private var currentReceiverId: String = ""
    private var realtimeSubscription: AutoCloseable? = null

    fun initChat(chatId: String, petName: String, receiverName: String = "Refugio", receiverId: String = "") {
        currentChatId = chatId
        currentPetName = petName
        currentReceiverName = receiverName
        currentReceiverId = receiverId

        loadMessages()
        setupRealtime()
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = AppwriteChatRepository.getMessages(currentChatId, currentPetName)
            _messages.value = list
            _isLoading.value = false
        }
    }

    private fun setupRealtime() {
        realtimeSubscription?.close()
        realtimeSubscription = AppwriteChatRepository.subscribeToChat(currentChatId) { newMsg ->
            // Añadir mensaje recibido en tiempo real si no existe
            val current = _messages.value
            if (current.none { it.id == newMsg.id }) {
                _messages.value = current + newMsg
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || currentChatId.isEmpty()) return

        val currentUser = AppwriteAuthRepository.currentUser.value
        val senderId = currentUser?.id ?: "user_me"
        val senderName = currentUser?.name ?: "Adoptante"

        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            chatId = currentChatId,
            petName = currentPetName,
            senderId = senderId,
            senderName = senderName,
            receiverId = currentReceiverId.ifEmpty { "shelter_host" },
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        _inputText.value = ""
        _messages.value = _messages.value + newMsg

        viewModelScope.launch {
            AppwriteChatRepository.sendMessage(newMsg)

            // Si es una conversación con el refugio y el usuario envía mensaje,
            // simular respuesta automática amigable si pasa un momento (ideal para sustentar en vivo)
            if (senderId != "shelter_host") {
                delay(2000)
                val reply = ChatMessage(
                    id = "reply_${System.currentTimeMillis()}",
                    chatId = currentChatId,
                    petName = currentPetName,
                    senderId = "shelter_host",
                    senderName = currentReceiverName,
                    receiverId = senderId,
                    text = "¡Recibido! Estamos encantados de coordinar contigo. Por favor agenda también una cita desde la app para tener tu espacio reservado 🐶.",
                    timestamp = System.currentTimeMillis(),
                    isRead = true
                )
                AppwriteChatRepository.sendMessage(reply)
                _messages.value = _messages.value + reply
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeSubscription?.close()
    }
}
