package com.example.app_adopcion_d_mascotas.ui.pantallas.solicitudes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_adopcion_d_mascotas.datos.modelos.AdoptionRequest
import com.example.app_adopcion_d_mascotas.datos.modelos.RequestStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.EstadoVacio
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloCatalogo
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloSolicitudes
import java.util.Calendar
import java.util.Locale

/*
 * ============================================================================
 * ARCHIVO: PantallaSolicitudes.kt
 * CAPA: UI -> Pantallas -> Solicitudes (RF9, RF10, RF13 y RF20)
 * ¿QUÉ HACE?: Muestra las solicitudes de adopción guardadas en Appwrite Cloud:
 *             - Si es ADOPTANTE: Ve el estado de sus solicitudes y puede abrir
 *               el chat en vivo con el refugio.
 *             - Si es REFUGIO: Puede Chatear con el postulante, Agendar Cita,
 *               Aprobar en cascada (RF10) o Rechazar.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitudes(
    usuarioActual: Usuario?,
    modeloSolicitudes: ModeloSolicitudes,
    modeloCatalogo: ModeloCatalogo,
    alVolver: () -> Unit,
    alIrACitas: () -> Unit,
    alAbrirChat: (String, String, String) -> Unit = { _, _, _ -> }
) = RequestsScreen(
    currentUser = usuarioActual,
    requestsViewModel = modeloSolicitudes,
    feedViewModel = modeloCatalogo,
    onBackClick = alVolver,
    onGoToAppointments = alIrACitas,
    onOpenChat = alAbrirChat
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    currentUser: Usuario?,
    requestsViewModel: ModeloSolicitudes,
    feedViewModel: ModeloCatalogo,
    onBackClick: () -> Unit,
    onGoToAppointments: () -> Unit,
    onOpenChat: (String, String, String) -> Unit = { _, _, _ -> }
) {
    // Paso 1: Cargar las solicitudes desde la tabla "solicitudes" al abrir la pantalla
    LaunchedEffect(currentUser) {
        requestsViewModel.cargarSolicitudes(currentUser)
    }

    val solicitudes = requestsViewModel.listaSolicitudes
    val estaCargando = requestsViewModel.estaCargando
    val estadoSnackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    LaunchedEffect(requestsViewModel.mensajeRespuesta) {
        requestsViewModel.mensajeRespuesta?.let { mensaje ->
            estadoSnackbar.showSnackbar(mensaje)
            requestsViewModel.mensajeRespuesta = null
        }
    }

    var solicitudPorAceptar by remember { mutableStateOf<AdoptionRequest?>(null) }
    var solicitudPorAgendar by remember { mutableStateOf<AdoptionRequest?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser?.rol == UserRole.REFUGIO) "Panel de Solicitudes" else "Mis Solicitudes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (currentUser?.rol == UserRole.REFUGIO)
                                "Refugio: ${currentUser.nombre}"
                            else
                                "Adoptante: ${currentUser?.nombre ?: ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onGoToAppointments) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Ver Citas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
            ) {
                if (estaCargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (solicitudes.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Default.Email,
                        titulo = if (currentUser?.rol == UserRole.REFUGIO)
                            "No has recibido solicitudes aún"
                        else
                            "No has enviado solicitudes de adopción aún",
                        descripcion = if (currentUser?.rol == UserRole.REFUGIO)
                            "Cuando los adoptantes postulen a tus mascotas publicadas, aparecerán aquí."
                        else
                            "Explora el catálogo y postula a la mascota que desees acoger en tu hogar."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(solicitudes, key = { it.id }) { solicitud ->
                            TarjetaSolicitud(
                                request = solicitud,
                                currentUser = currentUser,
                                onAcceptClick = { solicitudPorAceptar = solicitud },
                                onRejectClick = {
                                    requestsViewModel.rechazarSolicitud(solicitud.id, currentUser)
                                },
                                onScheduleClick = { solicitudPorAgendar = solicitud },
                                onChatClick = {
                                    // Canal unificado entre Refugio y Adoptante (RF13)
                                    val idChatUnificado = "chat_${solicitud.idMascota}_${solicitud.idSolicitante}"
                                    val destinatario = if (currentUser?.rol == UserRole.REFUGIO)
                                        solicitud.nombreSolicitante
                                    else
                                        solicitud.nombreRefugio
                                    onOpenChat(idChatUnificado, solicitud.nombreMascota, destinatario)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO RF10: Confirmar aprobación con efecto en cascada
    solicitudPorAceptar?.let { req ->
        AlertDialog(
            onDismissRequest = { solicitudPorAceptar = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Adopción (RF10)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Deseas aprobar la adopción de ${req.nombreMascota} a favor de ${req.nombreSolicitante}?",
                        fontWeight = FontWeight.Medium
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡ Acciones automáticas en Appwrite Cloud:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("1. '${req.nombreMascota}' pasará a estado ADOPTADA.", fontSize = 12.sp)
                            Text("2. Se retirará automáticamente del catálogo disponible.", fontSize = 12.sp)
                            Text("3. Otras solicitudes pendientes pasarán a 'NO_DISPONIBLE'.", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val aAceptar = req
                        solicitudPorAceptar = null
                        requestsViewModel.aceptarSolicitud(aAceptar, currentUser, contexto = contexto) {
                            feedViewModel.cargarMascotas()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Aprobar Adopción")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { solicitudPorAceptar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // DIÁLOGO RF20: Agendar Cita con Selector de Fecha y Hora Real
    solicitudPorAgendar?.let { req ->
        DialogoAgendarCita(
            request = req,
            onDismiss = { solicitudPorAgendar = null },
            onConfirm = { fecha, hora, modalidad ->
                requestsViewModel.agendarCita(
                    idSolicitud = req.id,
                    nombreMascota = req.nombreMascota,
                    nombrePostulante = req.nombreSolicitante,
                    nombreRefugio = req.nombreRefugio.ifBlank { currentUser?.nombre ?: "Refugio" },
                    fecha = fecha,
                    hora = hora,
                    modalidad = modalidad,
                    usuarioActual = currentUser,
                    contexto = contexto,
                    alTenerExito = {
                        solicitudPorAgendar = null
                        onGoToAppointments()
                    }
                )
            }
        )
    }
}

@Composable
fun TarjetaSolicitud(
    request: AdoptionRequest,
    currentUser: User?,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onChatClick: () -> Unit = {}
) {
    val esRefugio = currentUser?.rol == UserRole.REFUGIO

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabecera: Mascota + Botón de Chat + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.nombreMascota,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                StatusChip(status = request.estado)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Datos del solicitante y refugio + botón directo de Chat en Tiempo Real (RF13)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Solicitante: ${request.nombreSolicitante}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (request.correoSolicitante.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = request.correoSolicitante,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!esRefugio && request.nombreRefugio.isNotBlank()) {
                        Text(
                            text = "Refugio: ${request.nombreRefugio}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Botón para abrir el Chat en Tiempo Real entre Adoptante y Refugio (RF13)
                FilledTonalButton(
                    onClick = onChatClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chatear",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chatear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Respuestas al cuestionario (RF8)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Motivo de adopción:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "\"${request.motivo}\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VerificacionCuestionario(etiqueta = "Patio / Jardín", valor = request.tienePatio)
                    VerificacionCuestionario(etiqueta = "Experiencia previa", valor = request.tieneExperiencia)
                }
            }

            // Mensajes según estado para el adoptante
            if (!esRefugio) {
                when (request.estado) {
                    RequestStatus.ACEPTADA -> {
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "🎉 ¡Felicidades! Tu solicitud fue aprobada. Coordina por el chat o revisa tus Citas.",
                                color = Color(0xFF1B5E20),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    RequestStatus.NO_DISPONIBLE -> {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "ℹ️ Esta mascota ya fue adoptada por otro postulante.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    RequestStatus.RECHAZADA -> {
                        Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "El refugio no pudo aprobar esta solicitud en esta ocasión.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    RequestStatus.PENDIENTE -> {
                        Text(
                            text = "⏳ En evaluación por el refugio. Puedes escribirles usando el botón 'Chatear'.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Botones de acción para el Refugio (RF10 y RF20)
            if (esRefugio && request.estado == RequestStatus.PENDIENTE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onScheduleClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agendar Cita", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Rechazar", modifier = Modifier.size(16.dp))
                    }

                    Button(
                        onClick = onAcceptClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VerificacionCuestionario(etiqueta: String, valor: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (valor) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (valor) Color(0xFF2E7D32) else Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$etiqueta: ${if (valor) "Sí" else "No"}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatusChip(status: RequestStatus) {
    val (colorFondo, colorTexto, textoEtiqueta) = when (status) {
        RequestStatus.PENDIENTE -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Pendiente")
        RequestStatus.ACEPTADA -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Aprobada")
        RequestStatus.RECHAZADA -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Rechazada")
        RequestStatus.NO_DISPONIBLE -> Triple(Color(0xFFEEEEEE), Color(0xFF616161), "No disponible")
    }

    Surface(color = colorFondo, shape = RoundedCornerShape(12.dp)) {
        Text(
            text = textoEtiqueta,
            color = colorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/*
 * Diálogo para agendar cita usando los selectores oficiales de Fecha y Hora de Android (RF20)
 */
@Composable
fun DialogoAgendarCita(
    request: AdoptionRequest,
    onDismiss: () -> Unit,
    onConfirm: (fecha: String, hora: String, modalidad: String) -> Unit
) {
    val contexto = LocalContext.current
    val calendario = remember { Calendar.getInstance() }

    var fecha by remember {
        val d = calendario.get(Calendar.DAY_OF_MONTH)
        val m = calendario.get(Calendar.MONTH) + 1
        val a = calendario.get(Calendar.YEAR)
        mutableStateOf(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m, a))
    }
    var hora by remember { mutableStateOf("10:00") }
    var modalidad by remember { mutableStateOf("Presencial") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Cita / Entrevista (RF20)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Coordina una entrevista con ${request.nombreSolicitante} para la adopción de ${request.nombreMascota}.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Botón para abrir el Calendario oficial de Android
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            contexto,
                            { _, anio, mes, dia ->
                                fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, anio)
                            },
                            calendario.get(Calendar.YEAR),
                            calendario.get(Calendar.MONTH),
                            calendario.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fecha seleccionada: $fecha", fontWeight = FontWeight.Bold)
                }

                // Botón para abrir el Reloj oficial de Android
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            contexto,
                            { _, h, min ->
                                hora = String.format(Locale.getDefault(), "%02d:%02d", h, min)
                            },
                            10,
                            0,
                            true
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hora seleccionada: $hora hrs", fontWeight = FontWeight.Bold)
                }

                Text("Modalidad de la entrevista:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = modalidad == "Presencial",
                        onClick = { modalidad = "Presencial" },
                        label = { Text("Presencial") }
                    )
                    FilterChip(
                        selected = modalidad == "Videollamada",
                        onClick = { modalidad = "Videollamada" },
                        label = { Text("Videollamada") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fecha, hora, modalidad) },
                enabled = fecha.isNotBlank() && hora.isNotBlank()
            ) {
                Text("Guardar Cita")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
