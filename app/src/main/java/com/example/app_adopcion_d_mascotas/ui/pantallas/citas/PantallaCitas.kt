package com.example.app_adopcion_d_mascotas.ui.pantallas.citas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_adopcion_d_mascotas.datos.modelos.Appointment
import com.example.app_adopcion_d_mascotas.datos.modelos.AppointmentStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.EstadoVacio
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloSolicitudes
import java.util.Calendar
import java.util.Locale

/*
 * ============================================================================
 * ARCHIVO: PantallaCitas.kt
 * CAPA: UI -> Pantallas -> Citas (RF20: Agenda de Citas y Entrevistas)
 * ¿QUÉ HACE?: Muestra las citas presenciales o por videollamada programadas
 *             en la tabla "citas" de Appwrite Cloud, permitiendo confirmar
 *             asistencia o proponer una reprogramación de fecha y hora.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCitas(
    usuarioActual: Usuario?,
    modeloSolicitudes: ModeloSolicitudes,
    alVolver: () -> Unit
) = AppointmentsScreen(
    currentUser = usuarioActual,
    requestsViewModel = modeloSolicitudes,
    onBackClick = alVolver
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    currentUser: Usuario?,
    requestsViewModel: ModeloSolicitudes,
    onBackClick: () -> Unit
) {
    val contexto = LocalContext.current
    val calendario = remember { Calendar.getInstance() }

    LaunchedEffect(currentUser) {
        requestsViewModel.cargarCitas(currentUser)
    }

    val listaCitas = requestsViewModel.listaCitas
    val estaCargando = requestsViewModel.estaCargando

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Citas y Entrevistas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Coordinación en tiempo real (RF20)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
                } else if (listaCitas.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Default.CalendarMonth,
                        titulo = "No hay citas programadas",
                        descripcion = if (currentUser?.rol == UserRole.REFUGIO)
                            "Puedes agendar entrevistas con los postulantes desde el Panel de Solicitudes."
                        else
                            "Cuando un refugio agende una entrevista para tu postulación, aparecerá aquí."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(listaCitas, key = { it.id }) { cita ->
                            AppointmentCard(
                                cita = cita,
                                usuarioActual = currentUser,
                                alConfirmar = {
                                    requestsViewModel.confirmarCita(cita.id, currentUser)
                                },
                                alReprogramar = {
                                    DatePickerDialog(
                                        contexto,
                                        { _, anio, mes, dia ->
                                            val nuevaFecha = String.format(
                                                Locale.getDefault(),
                                                "%02d/%02d/%04d",
                                                dia,
                                                mes + 1,
                                                anio
                                            )
                                            TimePickerDialog(
                                                contexto,
                                                { _, h, min ->
                                                    val nuevaHora = String.format(Locale.getDefault(), "%02d:%02d", h, min)
                                                    requestsViewModel.reprogramarCita(
                                                        idCita = cita.id,
                                                        nuevaFecha = nuevaFecha,
                                                        nuevaHora = nuevaHora,
                                                        usuarioActual = currentUser
                                                    )
                                                },
                                                10,
                                                0,
                                                true
                                            ).show()
                                        },
                                        calendario.get(Calendar.YEAR),
                                        calendario.get(Calendar.MONTH),
                                        calendario.get(Calendar.DAY_OF_MONTH)
                                    ).apply {
                                        datePicker.minDate = System.currentTimeMillis() - 1000
                                    }.show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    cita: Appointment,
    usuarioActual: User?,
    alConfirmar: () -> Unit,
    alReprogramar: () -> Unit = {}
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        text = "Entrevista: ${cita.nombreMascota}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                AppointmentStatusChip(estado = cita.estado)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${cita.fecha} • ${cita.hora}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (cita.modalidad.contains("Video", ignoreCase = true) ||
                                    cita.modalidad.contains("Virtual", ignoreCase = true)
                                )
                                    Icons.Default.Videocam
                                else
                                    Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cita.modalidad,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Adoptante: ${cita.nombreSolicitante}", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Refugio: ${cita.nombreRefugio}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botones para Confirmar o Reprogramar la cita (RF20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = alReprogramar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reprogramar", fontSize = 12.sp)
                }

                if (cita.estado != AppointmentStatus.CONFIRMADA) {
                    Button(
                        onClick = alConfirmar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentStatusChip(estado: AppointmentStatus) {
    val (colorFondo, colorTexto, etiqueta) = when (estado) {
        AppointmentStatus.PROPUESTA -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Propuesta")
        AppointmentStatus.CONFIRMADA -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Confirmada")
        AppointmentStatus.REPROGRAMAR -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Reprogramada")
        AppointmentStatus.RECHAZADA -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelada")
    }

    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = etiqueta,
            color = colorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
