package com.example.app_adopcion_d_mascotas.ui.screens.publish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.ui.viewmodel.PublishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPetScreen(
    existingPet: Pet?,
    currentUser: User?,
    publishViewModel: PublishViewModel = viewModel(),
    onBackClick: () -> Unit,
    onSavedSuccessfully: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = existingPet != null

    var name by remember { mutableStateOf(existingPet?.name ?: "") }
    var breed by remember { mutableStateOf(existingPet?.breed ?: "") }
    var species by remember { mutableStateOf(existingPet?.species ?: "Perro") }
    var ageText by remember { mutableStateOf(existingPet?.ageYears?.toString() ?: "1") }
    var gender by remember { mutableStateOf(existingPet?.gender ?: "Macho") }
    var size by remember { mutableStateOf(existingPet?.size ?: "Mediano") }
    var location by remember { mutableStateOf(existingPet?.location ?: "Lima") }
    var shelterName by remember { mutableStateOf(existingPet?.shelterName ?: currentUser?.name ?: "Refugio") }
    var description by remember { mutableStateOf(existingPet?.description ?: "") }
    var isUrgent by remember { mutableStateOf(existingPet?.isUrgent ?: false) }

    // RF3: PhotoPicker oficial de Android para seleccionar hasta 5 fotos reales
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            publishViewModel.onPhotosSelected(uris)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Mascota" else "Publicar Mascota") },
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
            Spacer(modifier = Modifier.height(4.dp))

            // RF3: SECCIÓN DE FOTOS REALES DE GALERÍA
            Text(
                text = "Fotos de la Mascota (Hasta 5 fotos reales)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar Fotos de la Galería")
            }

            // Vista previa de fotos seleccionadas
            if (publishViewModel.selectedPhotoUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(publishViewModel.selectedPhotoUris) { index, uri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Foto $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Botón para eliminar foto individual
                            IconButton(
                                onClick = { publishViewModel.removePhoto(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else if (isEditing && existingPet?.photos?.isNotEmpty() == true) {
                Text(
                    text = "Conserva las ${existingPet.photos.size} fotos ya subidas a la nube, o selecciona nuevas arriba.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // CAMPOS DE FORMULARIO
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la mascota *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Selector Especie
            Column {
                Text("Especie *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Perro", "Gato").forEach { item ->
                        FilterChip(
                            selected = species == item,
                            onClick = { species = item },
                            label = { Text(item) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it },
                label = { Text("Edad aproximada (en años) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Selector Sexo
            Column {
                Text("Sexo *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Macho", "Hembra").forEach { item ->
                        FilterChip(
                            selected = gender == item,
                            onClick = { gender = item },
                            label = { Text(item) }
                        )
                    }
                }
            }

            // Selector Tamaño
            Column {
                Text("Tamaño *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Pequeño", "Mediano", "Grande").forEach { item ->
                        FilterChip(
                            selected = size == item,
                            onClick = { size = item },
                            label = { Text(item) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación / Ciudad *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = shelterName,
                onValueChange = { shelterName = it },
                label = { Text("Nombre del Refugio responsable *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Historia y Personalidad *") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Switch Caso Urgente (RF5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("¿Es un caso urgente?", fontWeight = FontWeight.Bold)
                    Text("Se destacará al inicio del feed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isUrgent, onCheckedChange = { isUrgent = it })
            }

            // Mensajes de estado
            publishViewModel.statusMessage?.let { msg ->
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

            // BOTÓN GUARDAR
            Button(
                onClick = {
                    val age = ageText.toIntOrNull() ?: 1
                    publishViewModel.savePet(
                        context = context,
                        existingPet = existingPet,
                        name = name,
                        breed = breed,
                        species = species,
                        ageYears = age,
                        description = description,
                        shelterName = shelterName,
                        isUrgent = isUrgent,
                        gender = gender,
                        size = size,
                        location = location,
                        onSuccess = onSavedSuccessfully
                    )
                },
                enabled = !publishViewModel.isSaving,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (publishViewModel.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Subiendo a la nube...")
                } else {
                    Text(
                        text = if (isEditing) "Actualizar en la Nube" else "Publicar en la Nube",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}