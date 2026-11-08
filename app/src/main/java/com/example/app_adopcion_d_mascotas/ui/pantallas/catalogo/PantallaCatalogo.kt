package com.example.app_adopcion_d_mascotas.ui.pantallas.catalogo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.EstadoVacio
import com.example.app_adopcion_d_mascotas.ui.componentes.TarjetaMascota
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloCatalogo

/*
 * ============================================================================
 * ARCHIVO: PantallaCatalogo.kt
 * CAPA: UI -> Pantallas -> Catálogo (RF4 y RF5)
 * ¿QUÉ HACE?: Muestra todas las mascotas disponibles desde Appwrite Cloud.
 *             Permite buscar por texto y filtrar combinando Especie (Perro,
 *             Gato, Otro), Tamaño (Pequeño, Mediano, Grande), Sexo (Macho,
 *             Hembra) y Casos Urgentes.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCatalogo(
    usuarioActual: Usuario?,
    modeloCatalogo: ModeloCatalogo,
    alHacerClicMascota: (String) -> Unit,
    alPublicarMascota: () -> Unit,
    alVerSolicitudes: () -> Unit,
    alVerCitas: () -> Unit,
    alVerPerfil: () -> Unit = {},
    alCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AdoptaAmor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = if (usuarioActual?.rol == RolUsuario.REFUGIO)
                                "🏠 Refugio: ${usuarioActual.nombre}"
                            else
                                "🐾 Adoptante: ${usuarioActual?.nombre ?: "Bienvenido"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Botón para recargar datos desde Appwrite Cloud en cualquier momento
                    IconButton(onClick = { modeloCatalogo.cargarMascotas() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Actualizar catálogo")
                    }
                    IconButton(onClick = alVerPerfil) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Mi Perfil")
                    }
                    IconButton(onClick = alVerCitas) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Citas")
                    }
                    IconButton(onClick = alVerSolicitudes) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Solicitudes")
                    }
                    IconButton(onClick = alCerrarSesion) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            // RF3: Solo los usuarios con rol REFUGIO ven el botón para publicar nuevas mascotas
            if (usuarioActual?.rol == RolUsuario.REFUGIO) {
                ExtendedFloatingActionButton(
                    onClick = alPublicarMascota,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    text = { Text("Publicar Mascota", fontWeight = FontWeight.Bold) }
                )
            }
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
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
            ) {
                // Paso 1: Barra de búsqueda por nombre, raza, refugio o ciudad (RF5)
                OutlinedTextField(
                    value = modeloCatalogo.textoBusqueda,
                    onValueChange = { modeloCatalogo.textoBusqueda = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar por nombre, raza, refugio o ciudad...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (modeloCatalogo.textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = { modeloCatalogo.textoBusqueda = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Paso 2: Chips de filtros combinables (RF5: Especie, Urgencia, Tamaño y Sexo)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Fila 1: Especie (Todas, Perro, Gato, Otro) + Chip de Solo Urgentes
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Todos", "Perro", "Gato", "Otro").forEach { especie ->
                            FilterChip(
                                selected = modeloCatalogo.especieSeleccionada == especie,
                                onClick = { modeloCatalogo.especieSeleccionada = especie },
                                label = { Text(if (especie == "Todos") "🐾 Todas las especies" else especie) }
                            )
                        }
                        FilterChip(
                            selected = modeloCatalogo.soloUrgentes,
                            onClick = { modeloCatalogo.soloUrgentes = !modeloCatalogo.soloUrgentes },
                            label = { Text("🚨 Solo Urgentes") }
                        )
                    }

                    // Fila 2: Tamaño (Pequeño, Mediano, Grande) y Sexo (Macho, Hembra)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Todos", "Pequeño", "Mediano", "Grande").forEach { tamano ->
                            FilterChip(
                                selected = modeloCatalogo.tamanoSeleccionado == tamano,
                                onClick = { modeloCatalogo.tamanoSeleccionado = tamano },
                                label = { Text(if (tamano == "Todos") "📏 Cualquier tamaño" else tamano) }
                            )
                        }

                        listOf("Macho", "Hembra").forEach { sexo ->
                            FilterChip(
                                selected = modeloCatalogo.sexoSeleccionado == sexo,
                                onClick = {
                                    modeloCatalogo.sexoSeleccionado =
                                        if (modeloCatalogo.sexoSeleccionado == sexo) "Todos" else sexo
                                },
                                label = { Text(sexo) }
                            )
                        }
                    }
                }

                // Paso 3: Contador de resultados encontrados y botón para limpiar filtros
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${modeloCatalogo.cantidadResultados} mascotas disponibles en la nube",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    if (modeloCatalogo.textoBusqueda.isNotEmpty() ||
                        modeloCatalogo.especieSeleccionada != "Todos" ||
                        modeloCatalogo.tamanoSeleccionado != "Todos" ||
                        modeloCatalogo.sexoSeleccionado != "Todos" ||
                        modeloCatalogo.soloUrgentes
                    ) {
                        TextButton(
                            onClick = { modeloCatalogo.limpiarFiltros() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Limpiar filtros", fontSize = 12.sp)
                        }
                    }
                }

                // Paso 4: Mostrar listado de mascotas o estado vacío
                if (modeloCatalogo.estaCargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (modeloCatalogo.mascotasFiltradas.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Default.Search,
                        titulo = "No se encontraron mascotas con esos filtros",
                        descripcion = "Prueba cambiando la especie, el tamaño o limpiando la búsqueda."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Carrusel horizontal de casos urgentes cuando no hay búsqueda de texto activa
                        if (modeloCatalogo.mascotasUrgentes.isNotEmpty() &&
                            modeloCatalogo.textoBusqueda.isEmpty() &&
                            !modeloCatalogo.soloUrgentes
                        ) {
                            item {
                                Text(
                                    text = "🚨 Casos de Adopción Urgente",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(modeloCatalogo.mascotasUrgentes, key = { "urg_${it.id}" }) { mascotaUrgente ->
                                        TarjetaMascota(
                                            mascota = mascotaUrgente,
                                            alHacerClic = { alHacerClicMascota(mascotaUrgente.id) },
                                            modifier = Modifier.width(280.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "🐾 Catálogo Completo",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Lista principal de tarjetas de mascotas
                        items(modeloCatalogo.mascotasFiltradas, key = { it.id }) { mascota ->
                            TarjetaMascota(
                                mascota = mascota,
                                alHacerClic = { alHacerClicMascota(mascota.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedScreen(
    currentUser: Usuario?,
    feedViewModel: ModeloCatalogo,
    onPetClick: (String) -> Unit,
    onAddPetClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) = PantallaCatalogo(
    usuarioActual = currentUser,
    modeloCatalogo = feedViewModel,
    alHacerClicMascota = onPetClick,
    alPublicarMascota = onAddPetClick,
    alVerSolicitudes = onRequestsClick,
    alVerCitas = onAppointmentsClick,
    alVerPerfil = onProfileClick,
    alCerrarSesion = onLogoutClick
)
