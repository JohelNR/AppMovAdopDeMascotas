package com.example.app_adopcion_d_mascotas.ui.screens.publish

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetStatus
import com.example.app_adopcion_d_mascotas.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPetScreen(
    existingPet: Pet?,
    currentUser: User?,
    onBackClick: () -> Unit,
    onSavePet: (Pet) -> Unit
) {
    val isEditing = existingPet != null

    // Galería de fotos simuladas para elegir (sin almacenamiento en la nube)
    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500",
        "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=500",
        "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
        "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=500"
    )

    // Estados para cada campo del formulario
    var name by remember { mutableStateOf(existingPet?.name ?: "") }
    var breed by remember { mutableStateOf(existingPet?.breed ?: "") }
    var species by remember { mutableStateOf(existingPet?.species ?: "Perro") }
    var ageText by remember { mutableStateOf(existingPet?.ageYears?.toString() ?: "") }
    var description by remember { mutableStateOf(existingPet?.description ?: "") }
    var isUrgent by remember { mutableStateOf(existingPet?.isUrgent ?: false) }
    var selectedPhoto by remember { mutableStateOf(existingPet?.imageUrl ?: samplePhotos.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Mascota" else "Nueva Publicación") },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selector de Foto Simulada (RF3)
            Text("Selecciona una foto (Simulación):", fontWeight = FontWeight.SemiBold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(samplePhotos) { photoUrl ->
                    val isSelected = photoUrl == selectedPhoto
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Opción de foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPhoto = photoUrl }
                    )
                }
            }

            // 2. Campo Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Nombre de la mascota") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Selector de Especie (Perro / Gato)
            Text("Especie:", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = species == "Perro",
                    onClick = { species = "Perro" },
                    label = { Text("🐶 Perro") }
                )
                FilterChip(
                    selected = species == "Gato",
                    onClick = { species = "Gato" },
                    label = { Text("🐱 Gato") }
                )
            }

            // 4. Campo Raza
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it; errorMessage = null },
                label = { Text("Raza (ej: Mestizo, Labrador)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Campo Edad
            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it; errorMessage = null },
                label = { Text("Edad en años") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 6. Campo Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; errorMessage = null },
                label = { Text("Descripción / Historia") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // 7. Switch de Caso Urgente (RF5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("¿Es un caso urgente?", fontWeight = FontWeight.Bold)
                    Text("Aparecerá destacado en la parte superior", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
                Switch(
                    checked = isUrgent,
                    onCheckedChange = { isUrgent = it }
                )
            }

            // Mensaje de Error
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 8. Botón Guardar Publicación
            Button(
                onClick = {
                    val age = ageText.toIntOrNull()
                    if (name.isBlank() || breed.isBlank() || description.isBlank()) {
                        errorMessage = "Por favor completa todos los campos"
                    } else if (name.length < 3) {
                        errorMessage = "El nombre debe tener al menos 3 caracteres"
                    } else if (age == null || age < 0) {
                        errorMessage = "Ingresa una edad válida en números"
                    } else if (age > 20) {
                        errorMessage = "la edad debe ser menor a 20"
                    } else
                     {
                        // Creamos o actualizamos el objeto Mascota
                        val petToSave = Pet(
                            id = existingPet?.id ?: "pet_${System.currentTimeMillis()}",
                            name = name.trim(),
                            breed = breed.trim(),
                            species = species,
                            ageYears = age,
                            description = description.trim(),
                            imageUrl = selectedPhoto,
                            shelterName = currentUser?.name ?: "Refugio Amigo",
                            isUrgent = isUrgent,
                            status = existingPet?.status ?: PetStatus.DISPONIBLE
                        )
                        onSavePet(petToSave)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Publicar Mascota")
            }
        }
    }
}