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

    var reason by remember { mutableStateOf("") }
    var hasYard by remember { mutableStateOf(false) }
    var hasExperience by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

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
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Refugio: ${pet.shelterName} • ${pet.breed}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Datos del Adoptante
            Text(
                text = "Datos del Adoptante",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = currentUser.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre Completo") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentUser.email,
                onValueChange = {},
                readOnly = true,
                label = { Text("Correo de Contacto") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Preguntas Clave del Cuestionario de Adopción (RF8)
            Text(
                text = "Cuestionario de Adopción Responsable",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("¿Por qué deseas adoptar a ${pet.name}? *") },
                placeholder = { Text("Cuéntanos sobre tu rutina, tu familia y por qué es el compañero ideal...") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Switch Patio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¿Cuentas con patio o espacio adecuado?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Casa o departamento con área para que juegue", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = hasYard, onCheckedChange = { hasYard = it })
            }

            // Switch Experiencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¿Tienes experiencia previa con mascotas?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Has cuidado perros o gatos anteriormente", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = hasExperience, onCheckedChange = { hasExperience = it })
            }

            // Checkbox Términos y Condiciones Obligatorios (RF8)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acepto el compromiso de tenencia responsable, visitas de seguimiento y cuidados veterinarios requeridos.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            // Feedback Message
            requestsViewModel.feedbackMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
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
                        reason = reason,
                        hasYard = hasYard,
                        hasExperience = hasExperience,
                        termsAccepted = termsAccepted,
                        onSuccess = onRequestSubmitted
                    )
                },
                enabled = !requestsViewModel.isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (requestsViewModel.isLoading) {
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
