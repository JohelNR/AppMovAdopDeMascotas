package com.example.app_adopcion_d_mascotas.ui.pantallas.detalle

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
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.PetStatus
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleMascota(
    mascota: Mascota?,
    usuarioActual: Usuario?,
    alVolver: () -> Unit,
    alEditar: (String) -> Unit,
    alEliminar: (String) -> Unit,
    alMarcarAdoptada: (String) -> Unit,
    alAdoptar: (String) -> Unit,
    alAbrirChat: (String, String) -> Unit = { _, _ -> },
    alVerSeguimiento: (String) -> Unit = {}
) {
    val contexto = LocalContext.current
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    if (mascota == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mascota no encontrada")
        }
        return
    }

    val ubicacionMascota = remember(mascota) { LatLng(mascota.latitude, mascota.longitude) }
    val estadoCamara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionMascota, 14f)
    }

    val fotosDisponibles = mascota.getAllPhotoUrls()
    val esPublicador = usuarioActual?.role == RolUsuario.REFUGIO &&
            (mascota.shelterName.trim().equals(usuarioActual.name.trim(), ignoreCase = true) ||
                    usuarioActual.name.isBlank())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mascota.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (esPublicador) {
                        IconButton(onClick = { alEditar(mascota.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { mostrarDialogoEliminar = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
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
            // RF4: Carrusel de Fotos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                if (fotosDisponibles.size > 1) {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(fotosDisponibles) { urlFoto ->
                            AsyncImage(
                                model = urlFoto,
                                contentDescription = "Foto de ${mascota.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillParentMaxWidth(0.85f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = mascota.displayPhoto,
                        contentDescription = "Foto de ${mascota.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (mascota.isUrgent) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Urgente",
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

                if (mascota.status == EstadoMascota.ADOPTADA) {
                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🎉 ¡Adoptada!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = mascota.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${mascota.species} • ${mascota.breed}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = if (mascota.gender == "Hembra") Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = mascota.gender,
                            color = if (mascota.gender == "Hembra") Color(0xFFC2185B) else Color(0xFF1976D2),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TarjetaCaracteristica(etiqueta = "Edad", valor = "${mascota.ageYears} años", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    TarjetaCaracteristica(etiqueta = "Tamaño", valor = mascota.size, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    TarjetaCaracteristica(etiqueta = "Sexo", valor = mascota.gender, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = mascota.shelterName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = mascota.location,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Historia y Personalidad",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mascota.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(22.dp))

                // RF7: Mapa interactivo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ubicación del Refugio (RF7)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("geo:${mascota.latitude},${mascota.longitude}?q=${mascota.latitude},${mascota.longitude}(${Uri.encode(mascota.shelterName)})")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            contexto.startActivity(intent)
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cómo llegar", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = estadoCamara
                    ) {
                        Marker(
                            state = MarkerState(position = ubicacionMascota),
                            title = mascota.shelterName,
                            snippet = "Ubicación del refugio para adopción de ${mascota.name}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // BOTONES DE ACCIÓN
                if (usuarioActual?.role == RolUsuario.ADOPTANTE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { alAdoptar(mascota.id) },
                            enabled = mascota.status == EstadoMascota.DISPONIBLE,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (mascota.status == EstadoMascota.DISPONIBLE) "¡Quiero Adoptar!" else "Ya adoptada",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Botón de Chat en Tiempo Real
                        IconButton(
                            onClick = {
                                val canalChat = "${mascota.id}_${usuarioActual.id}"
                                alAbrirChat(canalChat, mascota.name)
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chatear con el Refugio",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { alVerSeguimiento(mascota.id) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Seguimiento Post-Adopción (RF11)")
                    }
                } else if (esPublicador) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { alVerSeguimiento(mascota.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Revisar Seguimiento Post-Adopción (RF11)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { alMarcarAdoptada(mascota.id) },
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

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Esta acción retirará a ${mascota.name} de la base de datos en la nube.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        alEliminar(mascota.id)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TarjetaCaracteristica(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = etiqueta, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = valor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Wrapper para compatibilidad
@Composable
fun PetDetailScreen(
    pet: Mascota?,
    currentUser: Usuario?,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onMarkAdoptedClick: (String) -> Unit,
    onAdoptClick: (String) -> Unit,
    onChatClick: (String, String) -> Unit = { _, _ -> },
    onFollowUpClick: (String) -> Unit = {}
) = PantallaDetalleMascota(
    mascota = pet,
    usuarioActual = currentUser,
    alVolver = onBackClick,
    alEditar = onEditClick,
    alEliminar = onDeleteClick,
    alMarcarAdoptada = onMarkAdoptedClick,
    alAdoptar = onAdoptClick,
    alAbrirChat = onChatClick,
    alVerSeguimiento = onFollowUpClick
)
