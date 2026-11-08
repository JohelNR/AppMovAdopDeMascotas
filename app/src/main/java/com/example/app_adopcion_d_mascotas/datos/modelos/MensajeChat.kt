package com.example.app_adopcion_d_mascotas.datos.modelos

/*
 * ============================================================================
 * ARCHIVO: MensajeChat.kt
 * CAPA: Datos -> Modelos (RF13)
 * ¿QUÉ HACE?: Representa un mensaje enviado en tiempo real entre el Adoptante
 *             y el Refugio dentro de una conversación en Appwrite Cloud.
 * ============================================================================
 */
data class MensajeChat(
    val id: String = "",
    val chatId: String = "",                            // ID de la sala de chat (ej. chat_idMascota_idAdoptante)
    val petName: String = "",                           // Nombre de la mascota sobre la que conversan
    val senderId: String = "",                          // ID del usuario que escribió el mensaje
    val senderName: String = "",                        // Nombre visible de quien envió el mensaje
    val receiverId: String = "",                        // ID o nombre del destinatario
    val text: String = "",                              // Texto del mensaje
    val timestamp: Long = System.currentTimeMillis(),   // Fecha y hora exacta en milisegundos
    val isRead: Boolean = false                         // ¿Fue leído?
) {
    // Propiedades en español claro
    val idChat: String get() = chatId
    val nombreMascota: String get() = petName
    val idEmisor: String get() = senderId
    val idRemitente: String get() = senderId
    val nombreEmisor: String get() = senderName
    val nombreRemitente: String get() = senderName
    val idReceptor: String get() = receiverId
    val idDestinatario: String get() = receiverId
    val texto: String get() = text
    val fechaHora: Long get() = timestamp
    val leido: Boolean get() = isRead
    val estaLeido: Boolean get() = isRead
}

typealias ChatMessage = MensajeChat
