package com.example.app_adopcion_d_mascotas.data.model

// RF13: Modelo de mensaje de chat en tiempo real
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",          // Identificador del canal (ej: petId_adoptanteId)
    val petName: String = "",         // Nombre de la mascota de referencia
    val senderId: String = "",        // ID del usuario que envía
    val senderName: String = "",      // Nombre de quien envía
    val receiverId: String = "",      // ID del destinatario
    val text: String = "",            // Contenido del mensaje
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false       // Indicador de leído
)
