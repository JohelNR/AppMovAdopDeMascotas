package com.example.app_adopcion_d_mascotas.ui.screens.requests

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
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.ui.viewmodel.RequestsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionFormScreen(
    pet: Pet?,
    currentUser: User?,
    requestsViewModel: RequestsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onRequestSubmitted: () -> Unit
) {
    if (pet == null || currentUser == null) {
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
                    IconButton(onClick = onBackClick) {
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
            // Tarjeta de la mascota seleccionada
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
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
                            text = "Postulando para: ${pet.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${pet.breed} • ${pet.shelterName}",
                            fontSize = 13.sp,
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
                value = currentUser.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = currentUser.email,
                onValueChange = {},
                readOnly = true,
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            HorizontalDivider()

            // Preguntas del cuestionario
            Text(
                text = "Cuestionario de Tenencia Responsable",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Motivo
            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("¿Por qué deseas adoptar a ${pet.name}? *") },
                placeholder = { Text("Describe brevemente tu motivación y cómo cuidarás a la mascota...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Checkbox: Tiene patio / espacio abierto
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tienePatio,
                        onCheckedChange = { tienePatio = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Vivienda con patio o espacio amplio", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Cuentas con lugar adecuado para el movimiento de la mascota", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Checkbox: Experiencia previa con mascotas
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tieneExperiencia,
                        onCheckedChange = { tieneExperiencia = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Experiencia previa con mascotas", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Has cuidado animales antes y conoces sus necesidades", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Compromiso de tenencia responsable
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = terminosAceptados,
                        onCheckedChange = { terminosAceptados = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Compromiso de cuidado y salud *", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Acepto brindar alimentación, veterinario y cariño permanente.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Mensaje de feedback si hay error de validación
            requestsViewModel.mensajeRespuesta?.let { mensaje ->
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
                    requestsViewModel.submitAdoptionRequest(
                        petId = pet.id,
                        petName = pet.name,
                        applicant = currentUser,
                        shelterName = pet.shelterName,
                        reason = motivo,
                        hasYard = tienePatio,
                        hasExperience = tieneExperiencia,
                        termsAccepted = terminosAceptados,
                        context = contexto,
                        onSuccess = onRequestSubmitted
                    )
                },
                enabled = !requestsViewModel.estaCargando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (requestsViewModel.estaCargando) {
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
