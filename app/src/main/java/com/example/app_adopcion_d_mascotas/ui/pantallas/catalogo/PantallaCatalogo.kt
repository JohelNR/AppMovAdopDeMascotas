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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.TarjetaMascota
import com.example.app_adopcion_d_mascotas.ui.modelovista.FeedViewModel
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloCatalogo

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
                            text = if (usuarioActual?.role == RolUsuario.REFUGIO) "Modo Refugio: ${usuarioActual.name}" else "Bienvenido, ${usuarioActual?.name ?: "Adoptante"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = alVerPerfil) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil")
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
            if (usuarioActual?.role == RolUsuario.REFUGIO) {
                FloatingActionButton(
                    onClick = alPublicarMascota,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Publicar Mascota")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // RF6: Barra de Búsqueda por texto (Nombre, Raza o Refugio)
            OutlinedTextField(
                value = modeloCatalogo.textoBusqueda,
                onValueChange = { modeloCatalogo.textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nombre, raza o refugio...") },
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

            // RF6: Filtros Rápidos Combinables
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Filtro 1: Especie (Perro / Gato / Todos)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Perro", "Gato").forEach { item ->
                        FilterChip(
                            selected = modeloCatalogo.especieSeleccionada == item,
                            onClick = { modeloCatalogo.especieSeleccionada = item },
                            label = { Text(if (item == "Todos") "🐾 Todas" else item) }
                        )
                    }
                }

                // Filtro 2: Tamaño y Sexo combinados
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Pequeño", "Mediano", "Grande").forEach { size ->
                        FilterChip(
                            selected = modeloCatalogo.tamanoSeleccionado == size,
                            onClick = { modeloCatalogo.tamanoSeleccionado = size },
                            label = { Text(if (size == "Todos") "📏 Cualquier tamaño" else size) }
                        )
                    }

                    listOf("Macho", "Hembra").forEach { gender ->
                        FilterChip(
                            selected = modeloCatalogo.sexoSeleccionado == gender,
                            onClick = {
                                modeloCatalogo.sexoSeleccionado = if (modeloCatalogo.sexoSeleccionado == gender) "Todos" else gender
                            },
                            label = { Text(gender) }
                        )
                    }
                }
            }

            // RF6: Contador coherente de resultados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${modeloCatalogo.cantidadResultados} mascotas disponibles",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (modeloCatalogo.textoBusqueda.isNotEmpty() ||
                    modeloCatalogo.especieSeleccionada != "Todos" ||
                    modeloCatalogo.tamanoSeleccionado != "Todos" ||
                    modeloCatalogo.sexoSeleccionado != "Todos") {
                    TextButton(
                        onClick = { modeloCatalogo.limpiarFiltros() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Limpiar filtros", fontSize = 12.sp)
                    }
                }
            }

            // Lista reactiva de mascotas
            if (modeloCatalogo.estaCargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (modeloCatalogo.mascotasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron mascotas con esos filtros",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // RF5: Sección destacada de Mascotas Urgentes
                    if (modeloCatalogo.mascotasUrgentes.isNotEmpty() && modeloCatalogo.textoBusqueda.isEmpty()) {
                        item {
                            Text(
                                text = "🚨 Casos Urgentes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(modeloCatalogo.mascotasUrgentes) { mascotaUrgente ->
                                    TarjetaMascota(
                                        mascota = mascotaUrgente,
                                        alHacerClic = { alHacerClicMascota(mascotaUrgente.id) },
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🐾 Todas las Mascotas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Lista general de mascotas filtradas
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

// Wrapper para compatibilidad
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
