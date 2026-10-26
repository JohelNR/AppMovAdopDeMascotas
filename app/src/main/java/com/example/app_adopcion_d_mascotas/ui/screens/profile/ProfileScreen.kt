package com.example.app_adopcion_d_mascotas.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
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
import com.example.app_adopcion_d_mascotas.data.model.AdoptionRequest
import com.example.app_adopcion_d_mascotas.data.model.Pet
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.ui.screens.requests.StatusChip
import com.example.app_adopcion_d_mascotas.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User?,
    onUserUpdated: (User) -> Unit,
    profileViewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onChatClick: (String, String) -> Unit // chatId, receiverName
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        profileViewModel.loadProfile(currentUser)
    }

    LaunchedEffect(profileViewModel.statusMessage) {
        profileViewModel.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            profileViewModel.statusMessage = null
        }
    }

    val user = profileViewModel.currentUser ?: currentUser
    var showEditDialog by remember { mutableStateOf(false) }

    // Lanzador PhotoPicker para foto de avatar (RF2)
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profileViewModel.onAvatarSelected(uri)
            showEditDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // CABECERA ESTILO INSTAGRAM (Avatar circular + Datos)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar con badge de cámara flotante
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        AsyncImage(
                            model = profileViewModel.selectedAvatarUri ?: user.avatarUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 3.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chip de Rol (Adoptante o Refugio)
                    Surface(
                        color = if (user.role == UserRole.REFUGIO)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (user.role == UserRole.REFUGIO) "🏠 Refugio Verificado" else "🐾 Adoptante Responsable",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (user.role == UserRole.REFUGIO)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.bio,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (user.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = user.phone, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Perfil")
                    }
                }
            }

            // CONTENIDO DE LA PESTAÑA SEGÚN ROL (RF2)
            if (user.role == UserRole.REFUGIO) {
                Text(
                    text = "Mis Publicaciones Activas (${profileViewModel.myPets.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (profileViewModel.myPets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No has publicado mascotas aún.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(profileViewModel.myPets) { pet ->
                            ShelterPetGridCard(pet = pet, onClick = { onPetClick(pet.id) })
                        }
                    }
                }
            } else {
                Text(
                    text = "Mis Solicitudes de Adopción (${profileViewModel.myRequests.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (profileViewModel.myRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No has enviado solicitudes aún.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(profileViewModel.myRequests) { request ->
                            ApplicantRequestCard(
                                request = request,
                                onChatClick = {
                                    onChatClick("chat_${request.petId}_${request.applicantId}", request.shelterName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO MODAL EDITAR PERFIL (Con soporte anti-teclado)
    if (showEditDialog && user != null) {
        EditProfileDialog(
            user = user,
            avatarUri = profileViewModel.selectedAvatarUri,
            isSaving = profileViewModel.isSaving,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, bio ->
                profileViewModel.saveProfile(context, name, phone, bio) { updatedUser ->
                    showEditDialog = false
                    onUserUpdated(updatedUser)
                }
            }
        )
    }
}

@Composable
fun ShelterPetGridCard(pet: Pet, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = pet.displayPhoto,
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${pet.species} · ${pet.ageYears} años", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ApplicantRequestCard(
    request: AdoptionRequest,
    onChatClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = request.petName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Refugio: ${request.shelterName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = request.status)
            }

            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Chatear",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    avatarUri: android.net.Uri?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var bio by remember { mutableStateOf(user.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding() // Anti-teclado: la pantalla se adapta automáticamente
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre o Nombre del Refugio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 9) phone = it },
                    label = { Text("Teléfono de contacto (9 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biografía / Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, bio) },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Guardar Cambios")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}
