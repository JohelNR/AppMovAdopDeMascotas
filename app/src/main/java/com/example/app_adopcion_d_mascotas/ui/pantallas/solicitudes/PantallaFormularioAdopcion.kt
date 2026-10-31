package com.example.app_adopcion_d_mascotas.ui.pantallas.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloSolicitudes
import com.example.app_adopcion_d_mascotas.ui.modelovista.RequestsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioAdopcion(
    mascota: Mascota?,
    usuarioActual: Usuario?,
    modeloSolicitudes: ModeloSolicitudes = viewModel(),
    alVolver: () -> Unit,
    alEnviarSolicitud: () -> Unit
) {
    if (mascota == null || usuarioActual == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Información no disponible")
        }
        return
    }

    val contexto = LocalContext.current
    var motivo by remember { mutableStateOf("") }
    var tienePatio by remember { mutableStateOf(false) }
    var tieneExperiencia by remember { mutableStateOf(false) }
    var terminosAceptados by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitud de Adopción") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Resumen de la mascota a adoptar
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Postulando para adoptar a ${mascota.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${mascota.species} • ${mascota.breed} • Refugio: ${mascota.shelterName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Datos del Adoptante
            Text(
                text = "Tus Datos de Contacto",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = usuarioActual.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre del Postulante") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = usuarioActual.email,
                onValueChange = {},
                readOnly = true,
                label = { Text("Correo Electrónico") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Preguntas de Idoneidad (RF8)
            Text(
                text = "Preguntas de Idoneidad para el Hogar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("¿Por qué deseas adoptar a ${mascota.name}? *") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Switch: Espacio / Patio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¿Cuentas con patio o espacio abierto?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Ayuda a evaluar el espacio para mascotas grandes o activas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = tienePatio, onCheckedChange = { tienePatio = it })
            }

            // Switch: Experiencia previa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¿Tienes experiencia previa cuidando mascotas?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Si has tenido perros o gatos antes en tu familia", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = tieneExperiencia, onCheckedChange = { tieneExperiencia = it })
            }

            // Checkbox: Compromiso de adopción responsable
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = terminosAceptados,
                    onCheckedChange = { terminosAceptados = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acepto el compromiso de tenencia responsable, esterilización y permitir seguimiento post-adopción por parte del refugio.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Mensajes de error o éxito
            modeloSolicitudes.mensajeRespuesta?.let { mensaje ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensaje,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Botón de Envío
            Button(
                onClick = {
                    modeloSolicitudes.enviarSolicitudAdopcion(
                        idMascota = mascota.id,
                        nombreMascota = mascota.name,
                        postulante = usuarioActual,
                        nombreRefugio = mascota.shelterName,
                        motivo = motivo,
                        tienePatio = tienePatio,
                        tieneExperiencia = tieneExperiencia,
                        terminosAceptados = terminosAceptados,
                        contexto = contexto,
                        alTenerExito = alEnviarSolicitud
                    )
                },
                enabled = !modeloSolicitudes.estaCargando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (modeloSolicitudes.estaCargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Enviar Solicitud a la Nube",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Wrapper para compatibilidad
@Composable
fun AdoptionFormScreen(
    pet: Mascota?,
    currentUser: Usuario?,
    requestsViewModel: ModeloSolicitudes = viewModel(),
    onBackClick: () -> Unit,
    onRequestSubmitted: () -> Unit
) = PantallaFormularioAdopcion(
    mascota = pet,
    usuarioActual = currentUser,
    modeloSolicitudes = requestsViewModel,
    alVolver = onBackClick,
    alEnviarSolicitud = onRequestSubmitted
)
