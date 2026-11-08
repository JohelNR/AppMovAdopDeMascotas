package com.example.app_adopcion_d_mascotas.ui.pantallas.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_adopcion_d_mascotas.datos.modelos.ChatMessage
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloChat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * ============================================================================
 * PANTALLA DE CHAT EN TIEMPO REAL ADOPTANTE <-> REFUGIO (RF13)
 * ============================================================================
 * ¿Qué hace esta pantalla?
 * 1. Carga todos los mensajes reales de la tabla `mensajes` en Appwrite Cloud.
 * 2. Escucha eventos en vivo mediante WebSockets (Appwrite Realtime) para que
 *    cuando la otra persona escriba, el mensaje aparezca al instante.
 * 3. Diferencia claramente qué mensajes envié yo (`esMio = true`, a la derecha)
 *    y cuáles envió la otra persona (`esMio = false`, a la izquierda).
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChat(
    idChat: String,
    nombreMascota: String,
    nombreDestinatario: String = "Refugio",
    alVolver: () -> Unit,
    modeloChat: ModeloChat = viewModel()
) = ChatScreen(
    chatId = idChat,
    petName = nombreMascota,
    receiverName = nombreDestinatario,
    onBackClick = alVolver,
    viewModel = modeloChat
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    petName: String,
    receiverName: String = "Refugio",
    onBackClick: () -> Unit,
    viewModel: ModeloChat = viewModel()
) {
    val contexto = LocalContext.current
    val mensajes by viewModel.listaMensajes.collectAsState()
    val textoMensaje by viewModel.textoEntrada.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    val usuarioActual by RepositorioAutenticacionAppwrite.usuarioActual.collectAsState()

    val estadoLista = rememberLazyListState()

    // Paso 1: Inicializar canal de chat en Appwrite Cloud y suscribirse a Realtime
    LaunchedEffect(chatId) {
        viewModel.inicializarChat(
            idChat = chatId,
            nombreMascota = petName,
            nombreDestinatario = receiverName,
            contexto = contexto
        )
    }

    // Paso 2: Auto-scroll suave hacia el último mensaje cuando llega uno nuevo
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            estadoLista.animateScrollToItem(mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = receiverName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = receiverName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (petName.isNotEmpty()) "Conversación sobre $petName 🐾" else "Conectado a Appwrite Cloud",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarMensajes() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar mensajes"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Barra inferior con imePadding para adaptarse siempre al teclado
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textoMensaje,
                            onValueChange = { viewModel.cambiarTextoMensaje(it) },
                            placeholder = { Text("Escribe un mensaje real al refugio...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        IconButton(
                            onClick = { viewModel.enviarMensaje() },
                            enabled = textoMensaje.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (textoMensaje.isNotBlank()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar mensaje",
                                tint = if (textoMensaje.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
            ) {
                if (estaCargando && mensajes.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (mensajes.isEmpty()) {
                    // Estado vacío claro cuando aún no hay mensajes en este canal
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Inicia la conversación con $receiverName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Todos los mensajes se guardan en tiempo real en la tabla 'mensajes' de Appwrite Cloud.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = estadoLista,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mensajes) { mensaje ->
                            // Comparamos el idEmisor con el ID del usuario logueado
                            val esMio = mensaje.idEmisor == (usuarioActual?.id ?: "")
                            BurbujaMensaje(message = mensaje, isMe = esMio)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BurbujaMensaje(message: ChatMessage, isMe: Boolean) {
    val alineacion = if (isMe) Alignment.End else Alignment.Start
    val colorBurbuja = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val colorTexto = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val formaBurbuja = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val formatoHora = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val textoHora = remember(message.fechaHora) { formatoHora.format(Date(message.fechaHora)) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alineacion
    ) {
        Card(
            shape = formaBurbuja,
            colors = CardDefaults.cardColors(containerColor = colorBurbuja),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isMe && message.nombreEmisor.isNotEmpty()) {
                    Text(
                        text = message.nombreEmisor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = message.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = textoHora,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = colorTexto.copy(alpha = 0.7f)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Enviado",
                            modifier = Modifier.size(14.dp),
                            tint = if (message.leido) Color(0xFF1E88E5) else colorTexto.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
