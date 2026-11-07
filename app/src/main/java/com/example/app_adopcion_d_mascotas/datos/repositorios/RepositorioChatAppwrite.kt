package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.Query
import io.appwrite.Permission
import io.appwrite.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Solo confirma mensajes persistidos. El ID es el mismo en escritura, lectura y Realtime. */
class RepositorioChatAppwrite(private val cliente: ClienteAppwrite) {
    suspend fun sendMessage(message: MensajeChat): Unit = withContext(Dispatchers.IO) {
        cliente.databases.createDocument(
            databaseId = cliente.DATABASE_ID,
            collectionId = cliente.CHATS_COLLECTION,
            documentId = message.id,
            permissions = listOf(
                Permission.read(Role.user(message.senderId)),
                Permission.read(Role.user(message.receiverId))
            ),
            data = mapOf(
                "chatId" to message.chatId, "petName" to message.petName,
                "senderId" to message.senderId, "senderName" to message.senderName,
                "receiverId" to message.receiverId, "text" to message.text,
                "timestamp" to message.timestamp, "isRead" to message.isRead
            )
        )
        Unit
    }

    suspend fun getMessages(chatId: String, petName: String = ""): List<MensajeChat> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<MensajeChat>()
            var cursor: String? = null
            do {
                val queries = mutableListOf(Query.equal("chatId", chatId), Query.orderAsc("timestamp"), Query.limit(100))
                cursor?.let { queries.add(Query.cursorAfter(it)) }
                val page = cliente.databases.listDocuments(cliente.DATABASE_ID, cliente.CHATS_COLLECTION, queries)
                result += page.documents.map { doc -> decode(JSONObject(doc.data).put("\$id", doc.id)) }
                cursor = page.documents.lastOrNull()?.id
            } while (page.documents.size == 100)
            result
        }

    fun subscribeToChat(chatId: String, onNewMessage: (MensajeChat) -> Unit): AutoCloseable {
        val channel = "databases.${cliente.DATABASE_ID}.collections.${cliente.CHATS_COLLECTION}.documents"
        val subscription = cliente.realtime.subscribe(channel) { event ->
            if (event.events.any { it.endsWith(".create") }) {
                val payload = event.payload
                val json = if (payload is Map<*, *>) JSONObject(payload) else JSONObject(payload.toString())
                if (json.optString("chatId") == chatId) onNewMessage(decode(json))
            }
        }
        return AutoCloseable { subscription.close() }
    }

    private fun decode(json: JSONObject) = MensajeChat(
        id = json.getString("\$id"), chatId = json.optString("chatId"),
        petName = json.optString("petName"), senderId = json.optString("senderId"),
        senderName = json.optString("senderName"), receiverId = json.optString("receiverId"),
        text = json.optString("text"), timestamp = json.optLong("timestamp"),
        isRead = json.optBoolean("isRead")
    )
}

typealias AppwriteChatRepository = RepositorioChatAppwrite
