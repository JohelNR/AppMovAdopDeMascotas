package com.example.app_adopcion_d_mascotas.ui.pantallas.solicitudes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
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
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoSolicitud
import com.example.app_adopcion_d_mascotas.datos.modelos.RequestStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.modelovista.FeedViewModel
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloCatalogo
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloSolicitudes
import com.example.app_adopcion_d_mascotas.ui.modelovista.RequestsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitudes(
    usuarioActual: Usuario?,
    modeloSolicitudes: ModeloSolicitudes,
    modeloCatalogo: ModeloCatalogo,
    alVolver: () -> Unit,
    alIrACitas: () -> Unit
) = RequestsScreen(
    currentUser = usuarioActual,
    requestsViewModel = modeloSolicitudes,
    feedViewModel = modeloCatalogo,
    onBackClick = alVolver,
    onGoToAppointments = alIrACitas
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    currentUser: Usuario?,
    requestsViewModel: ModeloSolicitudes,
    feedViewModel: ModeloCatalogo,
    onBackClick: () -> Unit,
    onGoToAppointments: () -> Unit
) {
    // Al abrir la pantalla, cargar las solicitudes del usuario actual
    LaunchedEffect(currentUser) {
        requestsViewModel.cargarSolicitudes(currentUser)
    }

    val solicitudes = requestsViewModel.listaSolicitudes
    val estaCargando = requestsViewModel.estaCargando
    val estadoSnackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    // Mostrar feedback si existe
    LaunchedEffect(requestsViewModel.mensajeRespuesta) {
        requestsViewModel.mensajeRespuesta?.let { mensaje ->
            estadoSnackbar.showSnackbar(mensaje)
            requestsViewModel.mensajeRespuesta = null
        }
    }

    // Estados para diálogos
    var solicitudPorAceptar by remember { mutableStateOf<AdoptionRequest?>(null) }
    var solicitudPorAgendar by remember { mutableStateOf<AdoptionRequest?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser?.role == UserRole.REFUGIO) "Panel de Solicitudes" else "Mis Solicitudes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (currentUser?.role == UserRole.REFUGIO) "Refugio: ${currentUser.name}" else "Adoptante: ${currentUser?.name ?: ""}",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentUser?.role == UserRole.REFUGIO)
                            "No has recibido solicitudes aún"
                        else
                            "No has enviado solicitudes de adopción aún",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentUser?.role == UserRole.REFUGIO)
                            "Cuando los adoptantes postulen a tus mascotas publicadas, aparecerán aquí."
                        else
                            "Explora el catálogo y postula a la mascota que desees acoger en tu hogar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
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
                            onScheduleClick = { solicitudPorAgendar = solicitud }
                        )
                    }
                }
            }
        }
    }
}

    // DIÁLOGO RF10: Confirmación de Aceptación y Cascada Automática
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
                        text = "¿Deseas aprobar formalmente la adopción de ${req.petName} a favor de ${req.applicantName}?",
                        fontWeight = FontWeight.Medium
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡ Acciones automáticas del sistema:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("1. La mascota '${req.petName}' pasará a estado ADOPTADA.", fontSize = 12.sp)
                            Text("2. Se retirará automáticamente del catálogo público.", fontSize = 12.sp)
                            Text("3. Otras solicitudes pendientes para esta mascota pasarán a 'NO_DISPONIBLE'.", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toAccept = req
                        solicitudPorAceptar = null
                        requestsViewModel.acceptRequest(toAccept, currentUser, context = contexto) {
                            feedViewModel.loadPets()
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

    // DIÁLOGO RF20: Agendar Cita o Entrevista
    solicitudPorAgendar?.let { req ->
        DialogoAgendarCita(
            request = req,
            onDismiss = { solicitudPorAgendar = null },
            onConfirm = { fecha, hora, modalidad ->
                requestsViewModel.scheduleAppointment(
                    requestId = req.id,
                    petName = req.petName,
                    applicantName = req.applicantName,
                    shelterName = req.shelterName.ifBlank { currentUser?.name ?: "Refugio" },
                    date = fecha,
                    time = hora,
                    modality = modalidad,
                    currentUser = currentUser,
                    context = contexto,
                    onSuccess = {
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
    onScheduleClick: () -> Unit
) {
    val esRefugio = currentUser?.role == UserRole.REFUGIO

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
            // Fila superior: Nombre de mascota + Chip de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.petName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                StatusChip(status = request.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Datos del solicitante y refugio
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solicitante: ${request.applicantName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (request.applicantEmail.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = request.applicantEmail,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!esRefugio && request.shelterName.isNotBlank()) {
                    Text(
                        text = "Refugio a cargo: ${request.shelterName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    text = "\"${request.reason}\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VerificacionCuestionario(
                        etiqueta = "Patio / Jardín",
                        valor = request.hasYard
                    )
                    VerificacionCuestionario(
                        etiqueta = "Experiencia previa",
                        valor = request.hasExperience
                    )
                }
            }

            // Mensajes contextuales según estado para el adoptante
            if (!esRefugio) {
                when (request.status) {
                    RequestStatus.ACEPTADA -> {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🎉 ¡Felicidades! Tu solicitud fue aprobada. Entra a Citas para ver la coordinación de entrega.",
                                color = Color(0xFF1B5E20),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    RequestStatus.NO_DISPONIBLE -> {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ℹ️ Esta mascota ya fue adoptada por otro postulante que completó el proceso primero.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    RequestStatus.RECHAZADA -> {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Lo sentimos, el refugio no pudo aprobar esta solicitud en este momento.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    RequestStatus.PENDIENTE -> {
                        Text(
                            text = "⏳ En revisión por el refugio. Te notificaremos cualquier avance.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Botones de acción del Refugio (RF9, RF10, RF20)
            if (esRefugio && request.status == RequestStatus.PENDIENTE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Agendar Cita (RF20)
                    OutlinedButton(
                        onClick = onScheduleClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cita", fontSize = 12.sp)
                    }

                    // Botón Rechazar
                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Botón Aceptar Adopción (RF10)
                    Button(
                        onClick = onAcceptClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aceptar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        RequestStatus.ACEPTADA -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Aceptada")
        RequestStatus.RECHAZADA -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Rechazada")
        RequestStatus.NO_DISPONIBLE -> Triple(Color(0xFFEEEEEE), Color(0xFF616161), "No disponible")
    }

    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = textoEtiqueta,
            color = colorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DialogoAgendarCita(
    request: AdoptionRequest,
    onDismiss: () -> Unit,
    onConfirm: (fecha: String, hora: String, modalidad: String) -> Unit
) {
    var fecha by remember { mutableStateOf("20/10/2026") }
    var hora by remember { mutableStateOf("15:00") }
    var modalidad by remember { mutableStateOf("Presencial") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Cita / Entrevista (RF20)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Coordina una entrevista con ${request.applicantName} para la adopción de ${request.petName}.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (DD/MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (HH:MM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Modalidad:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = modalidad == "Presencial",
                        onClick = { modalidad = "Presencial" },
                        label = { Text("Presencial") }
                    )
                    FilterChip(
                        selected = modalidad == "Virtual",
                        onClick = { modalidad = "Virtual" },
                        label = { Text("Virtual (Meet)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fecha, hora, modalidad) },
                enabled = fecha.isNotBlank() && hora.isNotBlank()
            ) {
                Text("Confirmar Cita")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
