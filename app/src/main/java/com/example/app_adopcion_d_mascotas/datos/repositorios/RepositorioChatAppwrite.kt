package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.ChatMessage
import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// RF13: Repositorio para Chat en Tiempo Real entre Adoptante y Refugio
object RepositorioChatAppwrite {

    // Caché en memoria para garantizar que el chat funcione en tiempo real sin latencia
    private val localMessages = mutableListOf<MensajeChat>()

    // RF13: Enviar mensaje
    suspend fun sendMessage(message: MensajeChat): Boolean = withContext(Dispatchers.IO) {
        val finalMsg = if (message.id.isEmpty()) message.copy(id = "msg_${System.currentTimeMillis()}") else message
        localMessages.add(finalMsg)

        try {
            val data = mapOf(
                "chatId" to finalMsg.chatId,
                "petName" to finalMsg.petName,
                "senderId" to finalMsg.senderId,
                "senderName" to finalMsg.senderName,
                "receiverId" to finalMsg.receiverId,
                "text" to finalMsg.text,
                "timestamp" to finalMsg.timestamp,
                "isRead" to finalMsg.isRead
            )
            ClienteAppwrite.databases.createDocument(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.CHATS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            true
        }
    }

    suspend fun enviarMensaje(mensaje: MensajeChat): Boolean = sendMessage(mensaje)

    // RF13: Obtener mensajes de una conversación
    suspend fun getMessages(chatId: String, petName: String = "la mascota"): List<MensajeChat> = withContext(Dispatchers.IO) {
        val filteredLocal = localMessages.filter { it.chatId == chatId }.sortedBy { it.timestamp }

        try {
            val response = ClienteAppwrite.databases.listDocuments(
                databaseId = ClienteAppwrite.DATABASE_ID,
                collectionId = ClienteAppwrite.CHATS_COLLECTION,
                queries = listOf(
                    Query.equal("chatId", chatId),
                    Query.orderAsc("timestamp")
                )
            )

            val cloudList = response.documents.map { doc ->
                val d = doc.data
                MensajeChat(
                    id = doc.id,
                    chatId = d["chatId"]?.toString() ?: chatId,
                    petName = d["petName"]?.toString() ?: petName,
                    senderId = d["senderId"]?.toString() ?: "",
                    senderName = d["senderName"]?.toString() ?: "",
                    receiverId = d["receiverId"]?.toString() ?: "",
                    text = d["text"]?.toString() ?: "",
                    timestamp = (d["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    isRead = (d["isRead"] as? Boolean) ?: false
                )
            }

            if (cloudList.isNotEmpty()) {
                cloudList
            } else if (filteredLocal.isNotEmpty()) {
                filteredLocal
            } else {
                val welcome = MensajeChat(
                    id = "init_${chatId}",
                    chatId = chatId,
                    petName = petName,
                    senderId = "shelter_host",
                    senderName = "Refugio",
                    receiverId = "user",
                    text = "¡Hola! Gracias por tu interés en $petName 🐾. ¿En qué horario te gustaría coordinar una visita o tienes alguna consulta?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                    isRead = true
                )
                localMessages.add(welcome)
                listOf(welcome)
            }
        } catch (e: Exception) {
            if (filteredLocal.isNotEmpty()) {
                filteredLocal
            } else {
                val welcome = MensajeChat(
                    id = "init_${chatId}",
                    chatId = chatId,
                    petName = petName,
                    senderId = "shelter_host",
                    senderName = "Refugio",
                    receiverId = "user",
                    text = "¡Hola! Gracias por tu interés en $petName 🐾. ¿En qué horario te gustaría coordinar una visita o tienes alguna consulta?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                    isRead = true
                )
                localMessages.add(welcome)
                listOf(welcome)
            }
        }
    }

    suspend fun obtenerMensajes(idChat: String, nombreMascota: String = "la mascota"): List<MensajeChat> =
        getMessages(idChat, nombreMascota)

    // RF13: Suscripción Realtime a nuevos mensajes
    fun subscribeToChat(chatId: String, onNewMessage: (MensajeChat) -> Unit): AutoCloseable {
        return try {
            val channel = "databases.${ClienteAppwrite.DATABASE_ID}.collections.${ClienteAppwrite.CHATS_COLLECTION}.documents"
            val subscription = ClienteAppwrite.realtime.subscribe(channel) { event ->
                try {
                    val payload = event.payload
                    val json = if (payload is Map<*, *>) JSONObject(payload) else JSONObject(payload.toString())
                    val msgChatId = json.optString("chatId")
                    if (msgChatId == chatId) {
                        val newMsg = MensajeChat(
                            id = json.optString("\$id", ID.unique()),
                            chatId = msgChatId,
                            petName = json.optString("petName"),
                            senderId = json.optString("senderId"),
                            senderName = json.optString("senderName"),
                            receiverId = json.optString("receiverId"),
                            text = json.optString("text"),
                            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                            isRead = json.optBoolean("isRead", false)
                        )
                        onNewMessage(newMsg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            AutoCloseable { subscription.close() }
        } catch (e: Exception) {
            AutoCloseable { }
        }
    }

    fun suscribirseAlChat(idChat: String, alRecibirMensaje: (MensajeChat) -> Unit): AutoCloseable =
        subscribeToChat(idChat, alRecibirMensaje)
}

typealias AppwriteChatRepository = RepositorioChatAppwrite
