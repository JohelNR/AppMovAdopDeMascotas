package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/*
 * ============================================================================
 * ARCHIVO: RepositorioChatAppwrite.kt
 * CAPA: Datos -> Repositorios (RF13 y RF14)
 * ¿QUÉ HACE?: Envía, descarga y escucha mensajes en tiempo real desde la
 *             tabla "mensajes" de Appwrite Cloud (100% real entre dos
 *             celulares distintos, sin bots simulados ni listas en RAM).
 *             Columnas en español: idChat, nombreMascota, idEmisor,
 *             nombreEmisor, idReceptor, texto, fechaHora, leido.
 * ============================================================================
 */
object RepositorioChatAppwrite {

    /*
     * RF13: Enviar un mensaje nuevo y guardarlo en la tabla "mensajes" de Appwrite Cloud.
     */
    suspend fun enviarMensaje(mensaje: MensajeChat): Boolean = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Crear el mapa con las columnas en español de la tabla "mensajes"
            val datosMensaje = mapOf(
                "idChat" to mensaje.idChat,
                "nombreMascota" to mensaje.nombreMascota,
                "idEmisor" to mensaje.idEmisor,
                "nombreEmisor" to mensaje.nombreEmisor,
                "idReceptor" to mensaje.idReceptor,
                "texto" to mensaje.texto.trim(),
                "fechaHora" to mensaje.fechaHora,
                "leido" to mensaje.leido
            )

            // Paso 2: Guardar el mensaje en Appwrite Cloud
            ClienteAppwrite.baseDeDatos.createDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MENSAJES,
                documentId = ID.unique(),
                data = datosMensaje
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun sendMessage(message: MensajeChat): Boolean = enviarMensaje(message)

    /*
     * RF13: Obtener el historial real de mensajes de una conversación desde Appwrite Cloud.
     */
    suspend fun obtenerMensajes(idChat: String, nombreMascota: String = ""): List<MensajeChat> = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Consultar todos los mensajes de este canal ordenados por fechaHora ascendente
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MENSAJES,
                queries = listOf(
                    Query.equal("idChat", idChat),
                    Query.orderAsc("fechaHora")
                )
            )

            // Paso 2: Convertir cada documento de la nube en un objeto MensajeChat
            respuesta.documents.map { doc ->
                val d = doc.data
                MensajeChat(
                    id = doc.id,
                    chatId = d["idChat"]?.toString() ?: idChat,
                    petName = d["nombreMascota"]?.toString() ?: nombreMascota,
                    senderId = d["idEmisor"]?.toString() ?: "",
                    senderName = d["nombreEmisor"]?.toString() ?: "",
                    receiverId = d["idReceptor"]?.toString() ?: "",
                    text = d["texto"]?.toString() ?: "",
                    timestamp = (d["fechaHora"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    isRead = (d["leido"] as? Boolean) ?: false
                )
            }
        } catch (error: Exception) {
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMessages(chatId: String, petName: String = ""): List<MensajeChat> =
        obtenerMensajes(chatId, petName)

    /*
     * RF13 y RF14: Suscribirse en tiempo real (WebSockets de Appwrite Realtime)
     * para que cuando la otra persona escriba desde su celular, el mensaje
     * aparezca instantáneamente en pantalla.
     */
    fun suscribirseAlChat(idChat: String, alRecibirMensaje: (MensajeChat) -> Unit): AutoCloseable {
        return try {
            val canal = "databases.${ClienteAppwrite.ID_BASE_DATOS}.collections.${ClienteAppwrite.TABLA_MENSAJES}.documents"
            val suscripcion = ClienteAppwrite.tiempoReal.subscribe(canal) { evento ->
                try {
                    val carga = evento.payload
                    val json = if (carga is Map<*, *>) JSONObject(carga) else JSONObject(carga.toString())
                    val idChatMensaje = json.optString("idChat")

                    // Si el mensaje entrante pertenece a esta conversación, lo entregamos a la vista
                    if (idChatMensaje == idChat) {
                        val nuevoMensaje = MensajeChat(
                            id = json.optString("\$id", ID.unique()),
                            chatId = idChatMensaje,
                            petName = json.optString("nombreMascota"),
                            senderId = json.optString("idEmisor"),
                            senderName = json.optString("nombreEmisor"),
                            receiverId = json.optString("idReceptor"),
                            text = json.optString("texto"),
                            timestamp = json.optLong("fechaHora", System.currentTimeMillis()),
                            isRead = json.optBoolean("leido", false)
                        )
                        alRecibirMensaje(nuevoMensaje)
                    }
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            AutoCloseable { suscripcion.close() }
        } catch (_: Exception) {
            AutoCloseable { }
        }
    }

    fun subscribeToChat(chatId: String, onNewMessage: (MensajeChat) -> Unit): AutoCloseable =
        suscribirseAlChat(chatId, onNewMessage)
}

typealias AppwriteChatRepository = RepositorioChatAppwrite
