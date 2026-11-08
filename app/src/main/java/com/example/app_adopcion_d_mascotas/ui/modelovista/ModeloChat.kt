package com.example.app_adopcion_d_mascotas.ui.modelovista

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioChatAppwrite
import com.example.app_adopcion_d_mascotas.notificaciones.AyudanteNotificaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * ============================================================================
 * ARCHIVO: ModeloChat.kt
 * CAPA: UI -> ModeloVista (ViewModel de RF13 y RF14: Chat en Tiempo Real)
 * ¿QUÉ HACE?: Gestiona la conversación en vivo entre el Adoptante y el Refugio
 *             usando la tabla "mensajes" y los WebSockets de Appwrite Realtime.
 *             No usa bots simulados: todos los mensajes viajan por la nube.
 * ============================================================================
 */
class ModeloChat : ViewModel() {

    private val _mensajes = MutableStateFlow<List<MensajeChat>>(emptyList())
    val listaMensajes: StateFlow<List<MensajeChat>> = _mensajes.asStateFlow()

    private val _textoEntrada = MutableStateFlow("")
    val textoEntrada: StateFlow<String> = _textoEntrada.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    // Alias de compatibilidad
    val messages: StateFlow<List<MensajeChat>> get() = listaMensajes
    val inputText: StateFlow<String> get() = textoEntrada
    val isLoading: StateFlow<Boolean> get() = estaCargando

    private var idChatActual: String = ""
    private var nombreMascotaActual: String = ""
    private var nombreDestinatarioActual: String = "Refugio"
    private var idDestinatarioActual: String = ""
    private var suscripcionTiempoReal: AutoCloseable? = null

    /*
     * Paso 1: Inicializar el canal de chat y escuchar nuevos mensajes en vivo
     */
    fun inicializarChat(
        idChat: String,
        nombreMascota: String,
        nombreDestinatario: String = "Refugio",
        idDestinatario: String = "",
        contexto: Context? = null
    ) {
        idChatActual = normalizarIdChat(idChat)
        nombreMascotaActual = nombreMascota
        nombreDestinatarioActual = nombreDestinatario
        idDestinatarioActual = idDestinatario

        cargarMensajes()
        configurarTiempoReal(contexto)
    }

    fun initChat(chatId: String, petName: String, receiverName: String = "Refugio", receiverId: String = "") =
        inicializarChat(chatId, petName, receiverName, receiverId)

    /*
     * Garantiza que todas las pantallas usen siempre el mismo formato de ID de sala:
     * "chat_{idMascota}_{idAdoptante}"
     */
    private fun normalizarIdChat(idBruto: String): String {
        val limpio = idBruto.trim()
        return if (limpio.startsWith("chat_")) limpio else "chat_$limpio"
    }

    fun cambiarTextoMensaje(nuevoTexto: String) {
        _textoEntrada.value = nuevoTexto
    }

    fun onInputTextChanged(text: String) = cambiarTextoMensaje(text)

    /*
     * Paso 2: Descargar el historial de mensajes desde la tabla "mensajes" de Appwrite Cloud
     */
    fun cargarMensajes() {
        if (idChatActual.isEmpty()) return
        viewModelScope.launch {
            _estaCargando.value = true
            _mensajes.value = RepositorioChatAppwrite.obtenerMensajes(idChatActual, nombreMascotaActual)
            _estaCargando.value = false
        }
    }

    /*
     * Paso 3: Conectar con Appwrite Realtime para recibir al instante lo que escriba la otra persona
     */
    private fun configurarTiempoReal(contexto: Context?) {
        suscripcionTiempoReal?.close()
        suscripcionTiempoReal = RepositorioChatAppwrite.suscribirseAlChat(idChatActual) { mensajeEntrante ->
            val listaActual = _mensajes.value
            val yaExiste = listaActual.any {
                it.id == mensajeEntrante.id ||
                    (it.idEmisor == mensajeEntrante.idEmisor &&
                        it.texto == mensajeEntrante.texto &&
                        kotlin.math.abs(it.fechaHora - mensajeEntrante.fechaHora) < 3000)
            }
            if (!yaExiste) {
                _mensajes.value = listaActual + mensajeEntrante

                // RF14: Si el mensaje lo envió la otra persona, mostrar notificación en el celular
                val miId = RepositorioAutenticacionAppwrite.usuarioActual.value?.id ?: ""
                if (contexto != null && mensajeEntrante.idEmisor != miId) {
                    AyudanteNotificaciones.showNotification(
                        context = contexto,
                        notificationId = (System.currentTimeMillis() % 10000).toInt(),
                        title = "💬 Mensaje de ${mensajeEntrante.nombreEmisor}",
                        message = mensajeEntrante.texto
                    )
                }
            }
        }
    }

    /*
     * Paso 4: Enviar el mensaje escrito a la tabla "mensajes" de Appwrite Cloud
     */
    fun enviarMensaje() {
        val textoLimpio = _textoEntrada.value.trim()
        if (textoLimpio.isEmpty() || idChatActual.isEmpty()) return

        val usuarioConectado = RepositorioAutenticacionAppwrite.usuarioActual.value
        val idEmisor = usuarioConectado?.id ?: "usuario"
        val nombreEmisor = usuarioConectado?.nombre ?: "Usuario"

        val nuevoMensaje = MensajeChat(
            id = "temp_${System.currentTimeMillis()}",
            chatId = idChatActual,
            petName = nombreMascotaActual,
            senderId = idEmisor,
            senderName = nombreEmisor,
            receiverId = idDestinatarioActual.ifEmpty { nombreDestinatarioActual },
            text = textoLimpio,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        // Limpiar caja de texto y mostrar el mensaje en pantalla inmediatamente
        _textoEntrada.value = ""
        _mensajes.value = _mensajes.value + nuevoMensaje

        // Guardar en la tabla "mensajes" de Appwrite Cloud
        viewModelScope.launch {
            val exito = RepositorioChatAppwrite.enviarMensaje(nuevoMensaje)
            if (exito) {
                // Refrescar desde la nube para obtener el ID definitivo del documento
                _mensajes.value = RepositorioChatAppwrite.obtenerMensajes(idChatActual, nombreMascotaActual)
            }
        }
    }

    fun sendMessage() = enviarMensaje()

    override fun onCleared() {
        super.onCleared()
        suscripcionTiempoReal?.close()
    }
}

typealias ChatViewModel = ModeloChat
