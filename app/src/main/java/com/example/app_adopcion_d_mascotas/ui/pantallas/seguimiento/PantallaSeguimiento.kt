package com.example.app_adopcion_d_mascotas.ui.pantallas.seguimiento

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.app_adopcion_d_mascotas.di.modeloVista as viewModel
import coil.compose.AsyncImage
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.PetFollowUp
import com.example.app_adopcion_d_mascotas.datos.modelos.SeguimientoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.modelovista.FollowUpViewModel
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloSeguimiento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeguimiento(
    mascota: Mascota?,
    usuarioActual: Usuario?,
    modeloSeguimiento: ModeloSeguimiento = viewModel(),
    alVolver: () -> Unit
) = FollowUpScreen(
    pet = mascota,
    currentUser = usuarioActual,
    followUpViewModel = modeloSeguimiento,
    onBackClick = alVolver
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpScreen(
    pet: Mascota?,
    currentUser: Usuario?,
    followUpViewModel: ModeloSeguimiento = viewModel(),
    onBackClick: () -> Unit
) {
    val contexto = LocalContext.current
    val estadoSnackbar = remember { SnackbarHostState() }

    LaunchedEffect(pet) {
        pet?.let { followUpViewModel.cargarSeguimientos(it.id) }
    }

    LaunchedEffect(followUpViewModel.mensajeEstado) {
        followUpViewModel.mensajeEstado?.let { mensaje ->
            estadoSnackbar.showSnackbar(mensaje)
            followUpViewModel.mensajeEstado = null
        }
    }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
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
                onClick = { mostrarDialogoCrear = true },
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
            if (followUpViewModel.estaCargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (followUpViewModel.listaSeguimientos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aún no hay reportes de seguimiento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sé el primero en registrar cómo se encuentra ${pet.name} en su nuevo hogar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(followUpViewModel.listaSeguimientos) { reporte ->
                        TarjetaReporteSeguimiento(followUp = reporte)
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear && pet != null && currentUser != null) {
        DialogoCrearSeguimiento(
            pet = pet,
            currentUser = currentUser,
            followUpViewModel = followUpViewModel,
            onDismiss = { mostrarDialogoCrear = false },
            onSubmitted = {
                mostrarDialogoCrear = false
            }
        )
    }
}

@Composable
fun TarjetaReporteSeguimiento(followUp: PetFollowUp) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Reporte de ${followUp.applicantName.ifBlank { "Adoptante" }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Fecha: ${followUp.date}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = followUp.status,
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fotos del reporte
            val fotos = followUp.getAllPhotoUrls()
            if (fotos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(fotos) { urlFoto ->
                        AsyncImage(
                            model = urlFoto,
                            contentDescription = "Foto de seguimiento",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Notas de Salud
            if (followUp.healthNotes.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "Salud y Bienestar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = followUp.healthNotes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Notas de Adaptación
            if (followUp.adaptationNotes.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "Adaptación al Hogar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = followUp.adaptationNotes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoCrearSeguimiento(
    pet: Pet,
    currentUser: User,
    followUpViewModel: FollowUpViewModel,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit
) {
    val contexto = LocalContext.current
    var notasSalud by remember { mutableStateOf("") }
    var notasAdaptacion by remember { mutableStateOf("") }

    val lanzadorSelectorFotos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        followUpViewModel.seleccionarFotos(uris)
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
                        lanzadorSelectorFotos.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adjuntar fotos del hogar (${followUpViewModel.fotosSeleccionadas.size}/3)")
                }

                // Miniatura de fotos seleccionadas
                if (followUpViewModel.fotosSeleccionadas.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(followUpViewModel.fotosSeleccionadas.size) { indice ->
                            Box(modifier = Modifier.size(60.dp)) {
                                AsyncImage(
                                    model = followUpViewModel.fotosSeleccionadas[indice],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { followUpViewModel.eliminarFoto(indice) },
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
                    value = notasSalud,
                    onValueChange = { notasSalud = it },
                    label = { Text("Estado de salud, peso y vacunas") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notasAdaptacion,
                    onValueChange = { notasAdaptacion = it },
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
                        context = contexto,
                        pet = pet,
                        currentUser = currentUser,
                        healthNotes = notasSalud,
                        adaptationNotes = notasAdaptacion,
                        onSuccess = onSubmitted
                    )
                },
                enabled = !followUpViewModel.estaEnviando
            ) {
                if (followUpViewModel.estaEnviando) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Enviar Reporte")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !followUpViewModel.estaEnviando) {
                Text("Cancelar")
            }
        }
    )
}
