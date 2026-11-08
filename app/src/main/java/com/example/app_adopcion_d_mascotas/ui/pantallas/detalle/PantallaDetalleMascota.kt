package com.example.app_adopcion_d_mascotas.ui.pantallas.detalle

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
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
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.VisorMapaInteractivo

/*
 * ============================================================================
 * ARCHIVO: PantallaDetalleMascota.kt
 * CAPA: UI -> Pantallas -> Detalle (RF6, RF7, RF11, RF13)
 * ¿QUÉ HACE?: Muestra toda la ficha de la mascota seleccionada:
 *             1. Carrusel deslizable con todas sus fotos en Appwrite Storage.
 *             2. Edad, tamaño, sexo, historia y estado actual.
 *             3. Mapa interactivo real con calles + botón para abrir Google Maps (RF7).
 *             4. Botones para Adoptar, Chatear con el Refugio o Ver Seguimiento.
 *             5. Gestión exclusiva para el Refugio dueño (Editar, Eliminar y Estado).
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleMascota(
    mascota: Mascota?,
    usuarioActual: Usuario?,
    alVolver: () -> Unit,
    alEditar: (String) -> Unit,
    alEliminar: (String) -> Unit,
    alMarcarAdoptada: (String) -> Unit,
    alCambiarEstado: (String, EstadoMascota) -> Unit = { _, _ -> },
    alAdoptar: (String) -> Unit,
    alAbrirChat: (String, String) -> Unit = { _, _ -> },
    alVerSeguimiento: (String) -> Unit = {}
) {
    if (mascota == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle") },
                    navigationIcon = {
                        IconButton(onClick = alVolver) {
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
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró la mascota seleccionada.")
            }
        }
        return
    }

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    // Paso 1: Verificar si el usuario actual es el Refugio responsable de esta mascota (RF6)
    val esPublicador = usuarioActual?.rol == RolUsuario.REFUGIO && (
        mascota.idRefugio.isBlank() ||
            mascota.idRefugio == usuarioActual.id ||
            mascota.nombreRefugio.trim().equals(usuarioActual.nombre.trim(), ignoreCase = true)
        )

    val urlsFotos = mascota.obtenerTodasLasFotos()
    val estadoPaginador = rememberPagerState(pageCount = { urlsFotos.size })
    val contexto = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mascota.nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // Botón para compartir ficha por WhatsApp / Redes (RF15 extra profesional)
                    IconButton(
                        onClick = {
                            val textoCompartir = "🐾 ¡Conoce a ${mascota.nombre} en AdoptaAmor!\n" +
                                "Especie: ${mascota.especie} (${mascota.raza})\n" +
                                "Edad: ${mascota.edadAnios} año(s) • Tamaño: ${mascota.tamano}\n" +
                                "Refugio: ${mascota.nombreRefugio} (${mascota.ubicacion})\n" +
                                "Historia: ${mascota.descripcion}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textoCompartir)
                            }
                            contexto.startActivity(Intent.createChooser(intent, "Compartir mascota"))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir")
                    }

                    // Botones de Editar y Eliminar exclusivos para el Refugio dueño (RF6)
                    if (esPublicador) {
                        IconButton(onClick = { alEditar(mascota.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { mostrarDialogoEliminar = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Paso 2: Carrusel deslizable de fotos reales (RF3 y RF6)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    HorizontalPager(
                        state = estadoPaginador,
                        modifier = Modifier.fillMaxSize()
                    ) { pagina ->
                        AsyncImage(
                            model = urlsFotos[pagina],
                            contentDescription = "${mascota.nombre} foto ${pagina + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Indicadores circulares de página si tiene más de 1 foto
                    if (urlsFotos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(urlsFotos.size) { indice ->
                                Box(
                                    modifier = Modifier
                                        .size(if (estadoPaginador.currentPage == indice) 10.dp else 7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (estadoPaginador.currentPage == indice) Color.White
                                            else Color.White.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    }

                    // Etiqueta flotante de Adopción Urgente
                    if (mascota.esUrgente) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(bottomEnd = 14.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "🚨 ADOPCIÓN URGENTE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Paso 3: Datos principales de la mascota
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mascota.nombre,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${mascota.especie} • ${mascota.raza}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Etiqueta de Estado (DISPONIBLE, EN_PROCESO, ADOPTADA)
                        Surface(
                            color = when (mascota.estado) {
                                EstadoMascota.DISPONIBLE -> Color(0xFFE8F5E9)
                                EstadoMascota.EN_PROCESO -> Color(0xFFFFF3E0)
                                EstadoMascota.ADOPTADA -> Color(0xFFEEEEEE)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = when (mascota.estado) {
                                    EstadoMascota.DISPONIBLE -> "Disponible"
                                    EstadoMascota.EN_PROCESO -> "En proceso"
                                    EstadoMascota.ADOPTADA -> "Adoptada"
                                },
                                color = when (mascota.estado) {
                                    EstadoMascota.DISPONIBLE -> Color(0xFF2E7D32)
                                    EstadoMascota.EN_PROCESO -> Color(0xFFE65100)
                                    EstadoMascota.ADOPTADA -> Color(0xFF616161)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tres tarjetas resumen: Edad, Tamaño y Sexo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TarjetaCaracteristica(
                            etiqueta = "Edad",
                            valor = "${mascota.edadAnios} año(s)",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TarjetaCaracteristica(
                            etiqueta = "Tamaño",
                            valor = mascota.tamano,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TarjetaCaracteristica(
                            etiqueta = "Sexo",
                            valor = mascota.sexo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Información del Refugio y Dirección
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = mascota.nombreRefugio,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = mascota.ubicacion,
                                fontSize = 13.sp,
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
                        text = mascota.descripcion,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Paso 4: Mapa Interactivo Real + Botón de Ruta GPS en Google Maps (RF7)
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
                                text = "Ubicación en el Mapa (RF7)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val uriMapa = Uri.parse(
                                    "geo:${mascota.latitud},${mascota.longitud}?q=${mascota.latitud},${mascota.longitud}(${Uri.encode(mascota.nombreRefugio)})"
                                )
                                val intentMapa = Intent(Intent.ACTION_VIEW, uriMapa)
                                contexto.startActivity(intentMapa)
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
                            .height(210.dp)
                    ) {
                        VisorMapaInteractivo(
                            latitud = mascota.latitud,
                            longitud = mascota.longitud,
                            tituloMarcador = mascota.nombreRefugio,
                            subtituloMarcador = "${mascota.nombre} • ${mascota.ubicacion}"
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Paso 5: Botones de acción según el Rol (Adoptante vs Refugio)
                    if (usuarioActual?.rol == RolUsuario.ADOPTANTE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { alAdoptar(mascota.id) },
                                enabled = mascota.estado == EstadoMascota.DISPONIBLE,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (mascota.estado == EstadoMascota.DISPONIBLE)
                                        "¡Quiero Adoptar!"
                                    else
                                        "No disponible",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // Botón de Chat con ID unificado: "chat_{idMascota}_{idAdoptante}" (RF13)
                            IconButton(
                                onClick = {
                                    val idSalaUnificado = "chat_${mascota.id}_${usuarioActual.id}"
                                    alAbrirChat(idSalaUnificado, mascota.nombre)
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
                            Text("Seguimiento Post-Adopción (RF11)")
                        }
                    } else if (esPublicador) {
                        // Controles de gestión para el Refugio dueño (RF6)
                        Text(
                            text = "Cambiar Estado de la Mascota (RF6):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = mascota.estado == EstadoMascota.DISPONIBLE,
                                onClick = { alCambiarEstado(mascota.id, EstadoMascota.DISPONIBLE) },
                                label = { Text("Disponible", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = mascota.estado == EstadoMascota.EN_PROCESO,
                                onClick = { alCambiarEstado(mascota.id, EstadoMascota.EN_PROCESO) },
                                label = { Text("En proceso", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = mascota.estado == EstadoMascota.ADOPTADA,
                                onClick = { alMarcarAdoptada(mascota.id) },
                                label = { Text("Adoptada", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { alVerSeguimiento(mascota.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Revisar Reportes de Seguimiento (RF11)")
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
            text = { Text("Esta acción retirará a ${mascota.nombre} de la tabla 'mascotas' en Appwrite Cloud.") },
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
