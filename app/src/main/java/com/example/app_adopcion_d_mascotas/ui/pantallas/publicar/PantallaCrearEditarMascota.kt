package com.example.app_adopcion_d_mascotas.ui.pantallas.publicar

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
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Pet
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloPublicar
import com.example.app_adopcion_d_mascotas.ui.modelovista.PublishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearEditarMascota(
    mascotaExistente: Mascota?,
    usuarioActual: Usuario?,
    modeloPublicar: ModeloPublicar = viewModel(),
    alVolver: () -> Unit,
    alGuardarExitosamente: () -> Unit
) {
    val contexto = LocalContext.current
    val esEdicion = mascotaExistente != null

    // Variables de estado en español
    var nombre by remember { mutableStateOf(mascotaExistente?.name ?: "") }
    var raza by remember { mutableStateOf(mascotaExistente?.breed ?: "") }
    var especie by remember { mutableStateOf(mascotaExistente?.species ?: "Perro") }
    var textoEdad by remember { mutableStateOf(mascotaExistente?.ageYears?.toString() ?: "1") }
    var sexo by remember { mutableStateOf(mascotaExistente?.gender ?: "Macho") }
    var tamano by remember { mutableStateOf(mascotaExistente?.size ?: "Mediano") }
    var ubicacion by remember { mutableStateOf(mascotaExistente?.location ?: "Lima") }
    var nombreRefugio by remember { mutableStateOf(mascotaExistente?.shelterName ?: usuarioActual?.name ?: "Refugio") }
    var descripcion by remember { mutableStateOf(mascotaExistente?.description ?: "") }
    var esUrgente by remember { mutableStateOf(mascotaExistente?.isUrgent ?: false) }

    // RF3: Selector oficial de fotos para hasta 5 fotos reales
    val selectorFotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            modeloPublicar.onPhotosSelected(uris)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Mascota" else "Publicar Mascota") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
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
                    .widthIn(max = 700.dp)
                    .fillMaxWidth()
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
                    selectorFotosLauncher.launch(
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
            if (modeloPublicar.fotosSeleccionadas.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(modeloPublicar.fotosSeleccionadas) { indice, uri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Foto $indice",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Botón para eliminar foto individual
                            IconButton(
                                onClick = { modeloPublicar.quitarFoto(indice) },
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
            } else if (esEdicion && mascotaExistente?.photos?.isNotEmpty() == true) {
                Text(
                    text = "Conserva las ${mascotaExistente.photos.size} fotos ya subidas a la nube, o selecciona nuevas arriba.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // CAMPOS DE FORMULARIO
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
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
                    listOf("Perro", "Gato").forEach { opcion ->
                        FilterChip(
                            selected = especie == opcion,
                            onClick = { especie = opcion },
                            label = { Text(opcion) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = raza,
                onValueChange = { raza = it },
                label = { Text("Raza *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = textoEdad,
                onValueChange = { textoEdad = it },
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
                    listOf("Macho", "Hembra").forEach { opcion ->
                        FilterChip(
                            selected = sexo == opcion,
                            onClick = { sexo = opcion },
                            label = { Text(opcion) }
                        )
                    }
                }
            }

            // Selector Tamaño
            Column {
                Text("Tamaño *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Pequeño", "Mediano", "Grande").forEach { opcion ->
                        FilterChip(
                            selected = tamano == opcion,
                            onClick = { tamano = opcion },
                            label = { Text(opcion) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación / Ciudad *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nombreRefugio,
                onValueChange = { nombreRefugio = it },
                label = { Text("Nombre del Refugio responsable *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
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
                Switch(checked = esUrgente, onCheckedChange = { esUrgente = it })
            }

            // Mensajes de estado
            modeloPublicar.mensajeEstado?.let { msg ->
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
                    val edad = textoEdad.toIntOrNull() ?: 1
                    modeloPublicar.guardarMascota(
                        contexto = contexto,
                        mascotaExistente = mascotaExistente,
                        nombre = nombre,
                        raza = raza,
                        especie = especie,
                        edadAnos = edad,
                        descripcion = descripcion,
                        nombreRefugio = nombreRefugio,
                        esUrgente = esUrgente,
                        sexo = sexo,
                        tamano = tamano,
                        ubicacion = ubicacion,
                        alTenerExito = alGuardarExitosamente
                    )
                },
                enabled = !modeloPublicar.estaGuardando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (modeloPublicar.estaGuardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Subiendo a la nube...")
                } else {
                    Text(
                        text = if (esEdicion) "Actualizar en la Nube" else "Publicar en la Nube",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

// Wrapper para compatibilidad
@Composable
fun CreateEditPetScreen(
    existingPet: Mascota?,
    currentUser: Usuario?,
    publishViewModel: ModeloPublicar = viewModel(),
    onBackClick: () -> Unit,
    onSavedSuccessfully: () -> Unit
) = PantallaCrearEditarMascota(
    mascotaExistente = existingPet,
    usuarioActual = currentUser,
    modeloPublicar = publishViewModel,
    alVolver = onBackClick,
    alGuardarExitosamente = onSavedSuccessfully
)
