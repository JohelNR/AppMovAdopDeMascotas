package com.example.app_adopcion_d_mascotas.ui.screens.feed

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.ui.components.PetCard
import com.example.app_adopcion_d_mascotas.ui.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUser: User?,
    feedViewModel: FeedViewModel,
    onPetClick: (String) -> Unit,
    onAddPetClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onLogoutClick: () -> Unit
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
                                text = "Adopta Huellitas",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        currentUser?.let { user ->
                            Text(
                                text = "Hola, ${user.name} (${if (user.role == UserRole.REFUGIO) "Refugio" else "Adoptante"})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Botón para ver Solicitudes (RF9)
                    IconButton(onClick = onRequestsClick) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Solicitudes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón para ver Citas / Entrevistas (RF20)
                    IconButton(onClick = onAppointmentsClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Citas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Cerrar sesión
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo los refugios pueden crear publicaciones (RF3)
            if (currentUser?.role == UserRole.REFUGIO) {
                FloatingActionButton(
                    onClick = onAddPetClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
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
            // RF6: Barra de Búsqueda
            OutlinedTextField(
                value = feedViewModel.searchQuery,
                onValueChange = { feedViewModel.searchQuery = it },
                placeholder = { Text("Buscar por nombre, raza o refugio...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (feedViewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { feedViewModel.searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // RF6: Fila de Filtros Combinables (Chips desplazables)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtro Especie
                listOf("Todos", "Perro", "Gato").forEach { species ->
                    FilterChip(
                        selected = feedViewModel.selectedSpecies == species,
                        onClick = { feedViewModel.selectedSpecies = species },
                        label = { Text(if (species == "Todos") "🐾 Especie" else species) }
                    )
                }

                // Filtro Sexo
                listOf("Macho", "Hembra").forEach { gender ->
                    FilterChip(
                        selected = feedViewModel.selectedGender == gender,
                        onClick = {
                            feedViewModel.selectedGender = if (feedViewModel.selectedGender == gender) "Todos" else gender
                        },
                        label = { Text(gender) }
                    )
                }

                // Filtro Tamaño
                listOf("Pequeño", "Mediano", "Grande").forEach { size ->
                    FilterChip(
                        selected = feedViewModel.selectedSize == size,
                        onClick = {
                            feedViewModel.selectedSize = if (feedViewModel.selectedSize == size) "Todos" else size
                        },
                        label = { Text(size) }
                    )
                }
            }

            // RF6: Contador Coherente de Resultados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${feedViewModel.resultCount} mascotas encontradas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (feedViewModel.searchQuery.isNotEmpty() || feedViewModel.selectedSpecies != "Todos" ||
                    feedViewModel.selectedGender != "Todos" || feedViewModel.selectedSize != "Todos"
                ) {
                    TextButton(onClick = { feedViewModel.resetFilters() }) {
                        Text("Limpiar filtros", fontSize = 12.sp)
                    }
                }
            }

            if (feedViewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (feedViewModel.filteredPets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron mascotas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Prueba cambiando los filtros o el texto de búsqueda.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
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
                    if (feedViewModel.urgentPets.isNotEmpty() && feedViewModel.searchQuery.isEmpty()) {
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
                                items(feedViewModel.urgentPets) { urgentPet ->
                                    PetCard(
                                        pet = urgentPet,
                                        onClick = { onPetClick(urgentPet.id) },
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
                    items(feedViewModel.filteredPets, key = { it.id }) { pet ->
                        PetCard(
                            pet = pet,
                            onClick = { onPetClick(pet.id) }
                        )
                    }
                }
            }
        }
    }
}