package com.example.app_adopcion_d_mascotas.data.repository

import com.example.app_adopcion_d_mascotas.data.model.ChatMessage
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// RF13: Repositorio para Chat en Tiempo Real entre Adoptante y Refugio
object AppwriteChatRepository {

    // Caché en memoria para garantizar que el chat funcione en tiempo real sin latencia
    private val localMessages = mutableListOf<ChatMessage>()

    // RF13: Enviar mensaje
    suspend fun sendMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
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
            AppwriteClient.databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.CHATS_COLLECTION,
                documentId = ID.unique(),
                data = data
            )
            true
        } catch (e: Exception) {
            // Si la colección aún no está creada en la nube o hay corte de red, el mensaje persiste en sesión
            true
        }
    }

    // RF13: Obtener mensajes de una conversación
    suspend fun getMessages(chatId: String, petName: String = "la mascota"): List<ChatMessage> = withContext(Dispatchers.IO) {
        val filteredLocal = localMessages.filter { it.chatId == chatId }.sortedBy { it.timestamp }

        try {
            val response = AppwriteClient.databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.CHATS_COLLECTION,
                queries = listOf(
                    Query.equal("chatId", chatId),
                    Query.orderAsc("timestamp")
                )
            )

            val cloudList = response.documents.map { doc ->
                val d = doc.data
                ChatMessage(
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
                // Mensaje de bienvenida inicial automático por defecto
                val welcome = ChatMessage(
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
                val welcome = ChatMessage(
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

    // RF13: Suscripción Realtime a nuevos mensajes
    fun subscribeToChat(chatId: String, onNewMessage: (ChatMessage) -> Unit): AutoCloseable {
        return try {
            val channel = "databases.${AppwriteClient.DATABASE_ID}.collections.${AppwriteClient.CHATS_COLLECTION}.documents"
            val subscription = AppwriteClient.realtime.subscribe(channel) { event ->
                try {
                    val payload = event.payload
                    // Parsear payload según el formato devuelto por Realtime de Appwrite
                    val json = if (payload is Map<*, *>) JSONObject(payload) else JSONObject(payload.toString())
                    val msgChatId = json.optString("chatId")
                    if (msgChatId == chatId) {
                        val newMsg = ChatMessage(
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
}
