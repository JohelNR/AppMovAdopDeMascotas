package com.example.app_adopcion_d_mascotas.datos.modelos

// RF13: Modelo de mensaje de chat en tiempo real
data class MensajeChat(
    val id: String = "",
    val chatId: String = "",          // Identificador del canal (ej: petId_adoptanteId)
    val petName: String = "",         // Nombre de la mascota de referencia
    val senderId: String = "",        // ID del usuario que envía
    val senderName: String = "",      // Nombre de quien envía
    val receiverId: String = "",      // ID del destinatario
    val text: String = "",            // Contenido del mensaje
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false       // Indicador de leído
) {
    val idChat: String get() = chatId
    val nombreMascota: String get() = petName
    val idRemitente: String get() = senderId
    val nombreRemitente: String get() = senderName
    val idDestinatario: String get() = receiverId
    val texto: String get() = text
    val estaLeido: Boolean get() = isRead
}

typealias ChatMessage = MensajeChat
