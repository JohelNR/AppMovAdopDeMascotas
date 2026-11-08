package com.example.app_adopcion_d_mascotas.ui.pantallas.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SettingsBrightness
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
import com.example.app_adopcion_d_mascotas.datos.modelos.AdoptionRequest
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.RecortadorImagenDialogo
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloPerfil
import com.example.app_adopcion_d_mascotas.ui.pantallas.solicitudes.StatusChip
import com.example.app_adopcion_d_mascotas.ui.theme.GestorTemaApp
import com.example.app_adopcion_d_mascotas.ui.theme.ModoTemaVisual

/*
 * ============================================================================
 * PANTALLA DE PERFIL DE USUARIO Y AJUSTE DE TEMA (RF2 y RF16)
 * ============================================================================
 * ¿Qué hace esta pantalla?
 * 1. Muestra los datos del usuario autenticado desde la tabla `usuarios` (RF2).
 * 2. Permite cambiar la foto de perfil pasando primero por el Recortador
 *    Interactivo (zoom, encuadre y rotación) antes de subirla a `fotos_mascotas`.
 * 3. Permite elegir entre Tema Automático, Claro u Oscuro (RF16).
 * 4. Si es REFUGIO, muestra la cuadrícula de sus mascotas publicadas.
 *    Si es ADOPTANTE, muestra sus solicitudes y botón directo al Chat (RF13).
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    usuarioActual: Usuario?,
    alActualizarUsuario: (Usuario) -> Unit,
    modeloPerfil: ModeloPerfil = viewModel(),
    alVolver: () -> Unit,
    alCerrarSesion: () -> Unit,
    alHacerClicMascota: (String) -> Unit,
    alAbrirChat: (String, String) -> Unit
) = ProfileScreen(
    currentUser = usuarioActual,
    onUserUpdated = alActualizarUsuario,
    profileViewModel = modeloPerfil,
    onBackClick = alVolver,
    onLogoutClick = alCerrarSesion,
    onPetClick = alHacerClicMascota,
    onChatClick = alAbrirChat
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: Usuario?,
    onUserUpdated: (Usuario) -> Unit,
    profileViewModel: ModeloPerfil = viewModel(),
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onChatClick: (String, String) -> Unit
) {
    val contexto = LocalContext.current
    val estadoSnackbar = remember { SnackbarHostState() }

    // Paso 1: Cargar datos del perfil y publicaciones/solicitudes al entrar
    LaunchedEffect(currentUser) {
        profileViewModel.cargarPerfil(currentUser)
    }

    // Paso 2: Mostrar avisos de éxito o validación en barra inferior
    LaunchedEffect(profileViewModel.mensajeEstado) {
        profileViewModel.mensajeEstado?.let { mensaje ->
            estadoSnackbar.showSnackbar(mensaje)
            profileViewModel.mensajeEstado = null
        }
    }

    val usuario = profileViewModel.usuarioActual ?: currentUser
    var mostrarDialogoEditar by remember { mutableStateOf(false) }

    // Estado para abrir el recortador de imagen al elegir nuevo avatar
    var uriAvatarParaRecortar by remember { mutableStateOf<Uri?>(null) }

    // Lanzador PhotoPicker para foto de perfil (RF2)
    val lanzadorSelectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uriAvatarParaRecortar = uri
        }
    }

    // Si el usuario eligió una foto, abrimos el recortador profesional
    uriAvatarParaRecortar?.let { uriOriginal ->
        RecortadorImagenDialogo(
            uriImagenOriginal = uriOriginal,
            titulo = "Ajustar Foto de Perfil",
            alConfirmarRecorte = { uriRecortada ->
                profileViewModel.seleccionarAvatar(uriRecortada)
                uriAvatarParaRecortar = null
                mostrarDialogoEditar = true
            },
            alCancelar = {
                uriAvatarParaRecortar = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
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
        if (usuario == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
            ) {
                // =========================================================
                // 1. CABECERA DE PERFIL (Avatar circular + Rol + Tema RF16)
                // =========================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar con botón de cámara flotante que abre el recortador
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clickable {
                                    lanzadorSelectorFoto.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            AsyncImage(
                                model = profileViewModel.uriAvatarSeleccionado ?: usuario.urlAvatar,
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
                                    contentDescription = "Cambiar y recortar foto",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = usuario.nombre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = usuario.correo,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Chip de Rol (Adoptante o Refugio)
                        Surface(
                            color = if (usuario.rol == RolUsuario.REFUGIO)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (usuario.rol == RolUsuario.REFUGIO) "🏠 Refugio Verificado" else "🐾 Adoptante Responsable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (usuario.rol == RolUsuario.REFUGIO)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (usuario.biografia.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = usuario.biografia,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (usuario.telefono.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = usuario.telefono, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selector de Tema Oscuro / Claro / Automático (RF16)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = GestorTemaApp.modoActual == ModoTemaVisual.AUTOMATICO,
                                onClick = { GestorTemaApp.cambiarModo(contexto, ModoTemaVisual.AUTOMATICO) },
                                label = { Text("Auto", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.SettingsBrightness, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                            FilterChip(
                                selected = GestorTemaApp.modoActual == ModoTemaVisual.CLARO,
                                onClick = { GestorTemaApp.cambiarModo(contexto, ModoTemaVisual.CLARO) },
                                label = { Text("Claro", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                            FilterChip(
                                selected = GestorTemaApp.modoActual == ModoTemaVisual.OSCURO,
                                onClick = { GestorTemaApp.cambiarModo(contexto, ModoTemaVisual.OSCURO) },
                                label = { Text("Oscuro", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { mostrarDialogoEditar = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Editar Perfil")
                        }
                    }
                }

                // =========================================================
                // 2. LISTADO INFERIOR SEGÚN EL ROL DEL USUARIO (RF2)
                // =========================================================
                if (usuario.rol == RolUsuario.REFUGIO) {
                    Text(
                        text = "Mis Publicaciones Activas (${profileViewModel.misMascotas.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    if (profileViewModel.misMascotas.isEmpty()) {
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
                            items(profileViewModel.misMascotas) { mascota ->
                                TarjetaCuadriculaMascota(pet = mascota, onClick = { onPetClick(mascota.id) })
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Mis Solicitudes de Adopción (${profileViewModel.misSolicitudes.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    if (profileViewModel.misSolicitudes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No has enviado solicitudes aún.", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(profileViewModel.misSolicitudes) { solicitud ->
                                TarjetaSolicitudAdoptante(
                                    request = solicitud,
                                    onChatClick = {
                                        onChatClick(
                                            "chat_${solicitud.idMascota}_${solicitud.idSolicitante}",
                                            solicitud.nombreRefugio
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo modal para editar nombre, teléfono y biografía
    if (mostrarDialogoEditar && usuario != null) {
        DialogoEditarPerfil(
            user = usuario,
            isSaving = profileViewModel.estaGuardando,
            onDismiss = { mostrarDialogoEditar = false },
            onSave = { nombre, telefono, biografia ->
                profileViewModel.guardarPerfil(contexto, nombre, telefono, biografia) { usuarioActualizado ->
                    mostrarDialogoEditar = false
                    onUserUpdated(usuarioActualizado)
                }
            }
        )
    }
}

@Composable
fun TarjetaCuadriculaMascota(pet: Pet, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = pet.fotoPrincipal,
                contentDescription = pet.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(pet.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${pet.especie} · ${pet.edadAnios} años", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TarjetaSolicitudAdoptante(
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
                Text(text = request.nombreMascota, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Refugio: ${request.nombreRefugio}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = request.estado)
            }

            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Chatear con el refugio",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DialogoEditarPerfil(
    user: User,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (nombre: String, telefono: String, biografia: String) -> Unit
) {
    var nombre by remember { mutableStateOf(user.nombre) }
    var telefono by remember { mutableStateOf(user.telefono) }
    var biografia by remember { mutableStateOf(user.biografia) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre o Nombre del Refugio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { nuevo ->
                        val soloDigitos = nuevo.filter { it.isDigit() }
                        if (soloDigitos.length <= 9) telefono = soloDigitos
                    },
                    label = { Text("Teléfono de contacto (9 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = biografia,
                    onValueChange = { biografia = it },
                    label = { Text("Biografía / Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nombre, telefono, biografia) },
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
