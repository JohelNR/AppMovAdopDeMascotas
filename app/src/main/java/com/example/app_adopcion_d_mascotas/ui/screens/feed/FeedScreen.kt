package com.example.app_adopcion_d_mascotas.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.ui.components.PetCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUser: User?,
    pets: List<Pet>,
    onPetClick: (String) -> Unit,
    onAddPetClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Filtramos las mascotas marcadas como urgentes (RF5)
    val urgentPets = remember(pets) { pets.filter { it.isUrgent } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🐾 Adopta Huellitas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    // Botón para cerrar sesión
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        // Botón flotante (+) solo si el usuario es Refugio (RF3)
        floatingActionButton = {
            if (currentUser?.role == UserRole.REFUGIO) {
                FloatingActionButton(
                    onClick = onAddPetClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Publicar Mascota"
                    )
                }
            }
        }
    ) { paddingValues ->
        // LazyColumn: Lista eficiente que no congela la UI (RF4)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sección Destacada de Mascotas Urgentes (RF5)
            if (urgentPets.isNotEmpty()) {
                item {
                    Text(
                        text = "🚨 Casos Urgentes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Carrusel horizontal para urgentes
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(urgentPets) { urgentPet ->
                            PetCard(
                                pet = urgentPet,
                                onClick = { onPetClick(urgentPet.id) },
                                modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                }
            }

            // 2. Título de Catálogo General
            item {
                Text(
                    text = "Mascotas Disponibles (${pets.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. Tarjetas del Catálogo Principal
            items(pets) { pet ->
                PetCard(
                    pet = pet,
                    onClick = { onPetClick(pet.id) }
                )
            }
        }
    }
}