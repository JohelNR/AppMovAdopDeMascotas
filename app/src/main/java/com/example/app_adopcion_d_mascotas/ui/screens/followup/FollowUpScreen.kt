package com.example.app_adopcion_d_mascotas.ui.screens.followup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.PetFollowUp
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.ui.viewmodel.FollowUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpScreen(
    pet: Pet?,
    currentUser: User?,
    followUpViewModel: FollowUpViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pet) {
        pet?.let { followUpViewModel.loadFollowUps(it.id) }
    }

    LaunchedEffect(followUpViewModel.statusMessage) {
        followUpViewModel.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            followUpViewModel.statusMessage = null
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seguimiento Post-Adopción (RF11)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(text = "Mascota: ${pet?.name ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(imageVector = Icons.Default.Description, contentDescription = null) },
                text = { Text("Nuevo Reporte") }
            )
        }
    ) { paddingValues ->
        if (pet == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Mascota no encontrada")
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (followUpViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (followUpViewModel.followUpsList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sin reportes de seguimiento aún",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "El adoptante puede enviar fotos y reportes periódicos para verificar el bienestar y adaptación de ${pet.name}.",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(followUpViewModel.followUpsList) { report ->
                        FollowUpCard(report = report)
                    }
                }
            }
        }
    }

    if (showCreateDialog && pet != null && currentUser != null) {
        CreateFollowUpDialog(
            pet = pet,
            currentUser = currentUser,
            followUpViewModel = followUpViewModel,
            onDismiss = { showCreateDialog = false },
            onSubmitted = { showCreateDialog = false }
        )
    }
}

@Composable
fun FollowUpCard(report: PetFollowUp) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Cabecera: Fecha y Postulante
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📅 ${report.date}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Por: ${report.applicantName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Fotos del animalito en su nuevo hogar (RF11)
            val photos = report.getPhotoUrls()
            if (photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos) { photoUrl ->
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto de seguimiento",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }

            // Notas de Salud
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Salud y vacunas:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = report.healthNotes, fontSize = 13.sp)
                }
            }

            // Notas de Adaptación
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Adaptación y comportamiento:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = report.adaptationNotes, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CreateFollowUpDialog(
    pet: Pet,
    currentUser: User,
    followUpViewModel: FollowUpViewModel,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var healthNotes by remember { mutableStateOf("") }
    var adaptationNotes by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        followUpViewModel.onPhotosSelected(uris)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Seguimiento (RF11)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reporta cómo se encuentra ${pet.name} en su nuevo hogar y sube hasta 3 fotos de evidencia.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Botón selector de fotos
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adjuntar fotos del hogar (${followUpViewModel.selectedPhotos.size}/3)")
                }

                // Miniatura de fotos seleccionadas
                if (followUpViewModel.selectedPhotos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(followUpViewModel.selectedPhotos.size) { index ->
                            Box(modifier = Modifier.size(60.dp)) {
                                AsyncImage(
                                    model = followUpViewModel.selectedPhotos[index],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { followUpViewModel.removePhoto(index) },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Quitar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = healthNotes,
                    onValueChange = { healthNotes = it },
                    label = { Text("Estado de salud, peso y vacunas") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adaptationNotes,
                    onValueChange = { adaptationNotes = it },
                    label = { Text("Comportamiento y adaptación al hogar") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    followUpViewModel.submitReport(
                        context = context,
                        pet = pet,
                        currentUser = currentUser,
                        healthNotes = healthNotes,
                        adaptationNotes = adaptationNotes,
                        onSuccess = onSubmitted
                    )
                },
                enabled = !followUpViewModel.isSubmitting
            ) {
                if (followUpViewModel.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Enviar Reporte")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !followUpViewModel.isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}
