package com.example.app_adopcion_d_mascotas.ui.pantallas.publicar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.Context
import android.location.LocationManager
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.componentes.RecortadorImagenDialogo
import com.example.app_adopcion_d_mascotas.ui.componentes.VisorMapaInteractivo
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloPublicar

/*
 * ============================================================================
 * ARCHIVO: PantallaCrearEditarMascota.kt
 * CAPA: UI -> Pantallas -> Publicar (RF3, RF6 y RF7)
 * ¿QUÉ HACE?: Permite al Refugio publicar o editar una mascota en la nube:
 *             1. Selecciona fotos y abre el Recortador Interactivo para ajustarlas.
 *             2. Llena especie (Perro, Gato, Otro), raza, edad, sexo y tamaño.
 *             3. Permite obtener la ubicación GPS real del celular o tocar el
 *                mapa interactivo para fijar el punto exacto del refugio (RF7).
 * ============================================================================
 */
@SuppressLint("MissingPermission")
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

    // Paso 1: Estados en español de cada campo del formulario
    var nombre by remember { mutableStateOf(mascotaExistente?.nombre ?: "") }
    var raza by remember { mutableStateOf(mascotaExistente?.raza ?: "") }
    var especie by remember { mutableStateOf(mascotaExistente?.especie ?: "Perro") }
    var textoEdad by remember { mutableStateOf(mascotaExistente?.edadAnios?.toString() ?: "1") }
    var sexo by remember { mutableStateOf(mascotaExistente?.sexo ?: "Macho") }
    var tamano by remember { mutableStateOf(mascotaExistente?.tamano ?: "Mediano") }
    var ubicacion by remember { mutableStateOf(mascotaExistente?.ubicacion ?: "Cajamarca") }
    var latitud by remember { mutableDoubleStateOf(mascotaExistente?.latitud ?: -7.1617) }
    var longitud by remember { mutableDoubleStateOf(mascotaExistente?.longitud ?: -78.5128) }
    var nombreRefugio by remember {
        mutableStateOf(mascotaExistente?.nombreRefugio ?: usuarioActual?.nombre ?: "Refugio")
    }
    var descripcion by remember { mutableStateOf(mascotaExistente?.descripcion ?: "") }
    var esUrgente by remember { mutableStateOf(mascotaExistente?.esUrgente ?: false) }

    // Estado que guarda la foto recién elegida para abrir la ventana de Recorte/Ajuste
    var uriPendienteRecortar by remember { mutableStateOf<Uri?>(null) }

    // Paso 2: Selector de foto de galería que abre primero el Recortador Interactivo
    val selectorFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uriElegida ->
        if (uriElegida != null) {
            uriPendienteRecortar = uriElegida
        }
    }

    // Paso 3: Permiso y gestor GPS nativo para obtener coordenadas reales del teléfono (RF7)
    val obtenerCoordenadasGps: () -> Unit = {
        try {
            val gestorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = gestorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: gestorUbicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                latitud = loc.latitude
                longitud = loc.longitude
            }
        } catch (_: Exception) { }
    }
    val lanzadorPermisoGps = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            obtenerCoordenadasGps()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (esEdicion) "Editar Mascota" else "Publicar Nueva Mascota",
                        fontWeight = FontWeight.Bold
                    )
                },
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

                // SECCIÓN 1: FOTOS CON RECORTE Y AJUSTE PROFESIONAL (RF3)
                Text(
                    text = "1. Fotos de la Mascota (Hasta 5 fotos ajustadas) *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Button(
                    onClick = {
                        selectorFotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = modeloPublicar.fotosSeleccionadas.size < 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir y Ajustar Foto (${modeloPublicar.fotosSeleccionadas.size}/5)")
                }

                if (modeloPublicar.fotosSeleccionadas.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(modeloPublicar.fotosSeleccionadas) { indice, uri ->
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Foto $indice",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { modeloPublicar.quitarFoto(indice) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(26.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
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
                } else if (esEdicion && mascotaExistente?.fotos?.isNotEmpty() == true) {
                    Text(
                        text = "Conserva las ${mascotaExistente.fotos.size} foto(s) actuales en la nube, o agrega nuevas arriba.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // SECCIÓN 2: DATOS DE LA MASCOTA
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la mascota *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Especie (Perro, Gato, Otro)
                Column {
                    Text("Especie *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Perro", "Gato", "Otro").forEach { opcion ->
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
                    label = { Text("Raza (ej. Mestizo, Golden, Siamés) *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = textoEdad,
                    onValueChange = { nuevo -> if (nuevo.all { it.isDigit() } && nuevo.length <= 2) textoEdad = nuevo },
                    label = { Text("Edad aproximada en años (0 a 25) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Sexo y Tamaño
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sexo *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Macho", "Hembra").forEach { opcion ->
                                FilterChip(
                                    selected = sexo == opcion,
                                    onClick = { sexo = opcion },
                                    label = { Text(opcion) }
                                )
                            }
                        }
                    }
                }

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

                // SECCIÓN 3: UBICACIÓN Y MAPA GPS REAL (RF7)
                HorizontalDivider()
                Text(
                    text = "2. Ubicación del Refugio y Coordenadas GPS (RF7)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Dirección o Ciudad *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Botones rápidos de ciudades y botón de GPS del teléfono
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            val tienePermiso = ContextCompat.checkSelfPermission(
                                contexto,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (tienePermiso) {
                                obtenerCoordenadasGps()
                            } else {
                                lanzadorPermisoGps.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        label = { Text("Usar mi GPS actual") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )

                    AssistChip(
                        onClick = {
                            ubicacion = "Cajamarca Centro"
                            latitud = -7.1617
                            longitud = -78.5128
                        },
                        label = { Text("📍 Cajamarca") }
                    )

                    AssistChip(
                        onClick = {
                            ubicacion = "Baños del Inca, Cajamarca"
                            latitud = -7.1650
                            longitud = -78.4658
                        },
                        label = { Text("📍 Baños del Inca") }
                    )

                    AssistChip(
                        onClick = {
                            ubicacion = "Lima Centro"
                            latitud = -12.0464
                            longitud = -77.0428
                        },
                        label = { Text("📍 Lima") }
                    )
                }

                Text(
                    text = "Toca cualquier punto del mapa para mover el marcador exacto (${String.format("%.4f", latitud)}, ${String.format("%.4f", longitud)}):",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    VisorMapaInteractivo(
                        latitud = latitud,
                        longitud = longitud,
                        tituloMarcador = nombreRefugio.ifBlank { "Ubicación del Refugio" },
                        subtituloMarcador = ubicacion,
                        permitirSeleccionarPunto = true,
                        alSeleccionarUbicacion = { nuevaLat, nuevaLng ->
                            latitud = nuevaLat
                            longitud = nuevaLng
                        }
                    )
                }

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
                    label = { Text("Historia, vacunas y personalidad (mínimo 10 letras) *") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Switch de Caso Urgente (RF5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Es un caso de adopción urgente?", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Aparecerá destacado en el carrusel superior del catálogo",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = esUrgente, onCheckedChange = { esUrgente = it })
                }

                // Mensaje de validación o progreso
                modeloPublicar.mensajeEstado?.let { msg ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // BOTÓN GUARDAR EN APPWRITE CLOUD
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
                            idRefugio = usuarioActual?.id ?: "",
                            esUrgente = esUrgente,
                            sexo = sexo,
                            tamano = tamano,
                            ubicacion = ubicacion,
                            latitud = latitud,
                            longitud = longitud,
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
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Guardando en Appwrite...")
                    } else {
                        Text(
                            text = if (esEdicion) "Guardar Cambios en la Nube" else "Publicar Mascota en la Nube",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Ventana emergente para Recortar, Rotar y Hacer Zoom a la foto elegida
    uriPendienteRecortar?.let { uriOriginal ->
        RecortadorImagenDialogo(
            uriImagenOriginal = uriOriginal,
            titulo = "Ajustar Foto de la Mascota",
            alCancelar = { uriPendienteRecortar = null },
            alConfirmarRecorte = { uriRecortada ->
                uriPendienteRecortar = null
                modeloPublicar.agregarFotoAjustada(uriRecortada)
            }
        )
    }
}

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
