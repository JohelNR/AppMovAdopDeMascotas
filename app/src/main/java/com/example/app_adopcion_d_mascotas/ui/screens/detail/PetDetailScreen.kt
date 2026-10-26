package com.example.app_adopcion_d_mascotas.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetStatus
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    pet: Pet?,
    currentUser: User?,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onMarkAdoptedClick: (String) -> Unit,
    onAdoptClick: (String) -> Unit,
    onChatClick: (String, String) -> Unit = { _, _ -> },
    onFollowUpClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (pet == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mascota no encontrada")
        }
        return
    }

    val petLocation = remember(pet) { LatLng(pet.latitude, pet.longitude) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(petLocation, 14f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // CARRUSEL DE FOTOS (RF3 y RF4)
            val allPhotos = pet.getAllPhotoUrls()
            if (allPhotos.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(allPhotos) { photoUrl ->
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto de ${pet.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            } else {
                AsyncImage(
                    model = pet.displayPhoto,
                    contentDescription = "Foto de ${pet.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
            }

            // CONTENIDO DEL DETALLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Nombre y Caso Urgente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pet.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (pet.isUrgent) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CASO URGENTE",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${pet.breed} · ${pet.species}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fila de chips de características
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailFeatureChip(label = "Edad", value = "${pet.ageYears} ${if (pet.ageYears == 1) "año" else "años"}")
                    DetailFeatureChip(label = "Sexo", value = pet.gender)
                    DetailFeatureChip(label = "Tamaño", value = pet.size)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Datos del Refugio
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Refugio a cargo: ${pet.shelterName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Distrito: ${pet.location}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Historia y Personalidad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = pet.description,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🗺️ SECCIÓN RF7: MAPA DE UBICACIÓN REAL Y RUTA EN GOOGLE MAPS
                Text(
                    text = "Ubicación del Refugio en el Mapa (RF7)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = MarkerState(position = petLocation),
                                title = pet.shelterName,
                                snippet = "Ubicación de adopción: ${pet.location}"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón abrir ruta en Google Maps (RF7 Criterio oficial)
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:${pet.latitude},${pet.longitude}?q=${pet.latitude},${pet.longitude}(${Uri.encode(pet.shelterName)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            val fallbackIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${pet.latitude},${pet.longitude}")
                            )
                            context.startActivity(fallbackIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cómo llegar (Abrir ruta en Google Maps)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BOTÓN CHAT CON EL REFUGIO (RF13)
                if (currentUser != null && currentUser.role != UserRole.REFUGIO) {
                    val chatId = "chat_${pet.id}_${currentUser.id}"
                    OutlinedButton(
                        onClick = { onChatClick(chatId, pet.shelterName) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("💬 Chatear con el Refugio (RF13)")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // SI ESTÁ ADOPTADA: BOTÓN SEGUIMIENTO POST-ADOPCIÓN (RF11)
                if (pet.status == PetStatus.ADOPTADA) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🎉 ¡Mascota Felizmente Adoptada!",
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Puedes consultar o registrar los reportes de adaptación y salud del animalito.",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onFollowUpClick(pet.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ver Seguimiento Post-Adopción (RF11)")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ACCIONES DE POSTULACIÓN O ADMINISTRACIÓN SEGÚN ROL
                if (currentUser?.role != UserRole.REFUGIO && pet.status == PetStatus.DISPONIBLE) {
                    Button(
                        onClick = { onAdoptClick(pet.id) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solicitar Adopción (Formulario RF8)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Si es Refugio -> Acciones de Administración (Editar, Borrar, Marcar Adoptada)
                if (currentUser?.role == UserRole.REFUGIO) {
                    Text(
                        text = "Panel de Gestión del Refugio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEditClick(pet.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar")
                        }

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }

                    if (pet.status == PetStatus.DISPONIBLE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onMarkAdoptedClick(pet.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Marcar como Adoptada")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Esta acción retirará a ${pet.name} de la base de datos en la nube.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(pet.id)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailFeatureChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
