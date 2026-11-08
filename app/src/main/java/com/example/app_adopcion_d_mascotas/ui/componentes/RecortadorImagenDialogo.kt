package com.example.app_adopcion_d_mascotas.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/*
 * ============================================================================
 * ARCHIVO: RecortadorImagenDialogo.kt
 * CAPA: UI -> Componentes Reutilizables (RF2, RF3, RF11)
 * ¿QUÉ HACE?: Abre una ventana profesional para que el usuario pueda ajustar,
 *             hacer zoom, mover y rotar cualquier foto antes de subirla a la
 *             nube de Appwrite.
 * ============================================================================
 */
@Composable
fun RecortadorImagenDialogo(
    uriImagenOriginal: Uri,
    titulo: String = "Ajustar y Recortar Foto",
    alCancelar: () -> Unit,
    alConfirmarRecorte: (Uri) -> Unit
) {
    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()

    // Paso 1: Estados para controlar el zoom, posición y rotación de la foto
    var bitmapOriginal by remember { mutableStateOf<Bitmap?>(null) }
    var escalaZoom by remember { mutableFloatStateOf(1f) }
    var desplazamientoX by remember { mutableFloatStateOf(0f) }
    var desplazamientoY by remember { mutableFloatStateOf(0f) }
    var gradosRotacion by remember { mutableFloatStateOf(0f) }
    var procesandoRecorte by remember { mutableStateOf(false) }

    // Paso 2: Cargar la imagen seleccionada en memoria al abrir el diálogo
    LaunchedEffect(uriImagenOriginal) {
        withContext(Dispatchers.IO) {
            try {
                val opciones = BitmapFactory.Options().apply { inSampleSize = 1 }
                val bmp = contexto.contentResolver.openInputStream(uriImagenOriginal)?.use {
                    BitmapFactory.decodeStream(it, null, opciones)
                }
                bitmapOriginal = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!procesandoRecorte) alCancelar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.94f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Encabezado del recortador
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = "Arrastra la imagen con un dedo o usa el control de zoom para encuadrarla.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Paso 3: Área cuadrada interactiva de vista previa y encuadre
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, paneo, zoom, _ ->
                                escalaZoom = (escalaZoom * zoom).coerceIn(1f, 3.5f)
                                val limite = (escalaZoom - 1f) * 140f + 80f
                                desplazamientoX = (desplazamientoX + paneo.x).coerceIn(-limite, limite)
                                desplazamientoY = (desplazamientoY + paneo.y).coerceIn(-limite, limite)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = bitmapOriginal
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Vista previa de recorte",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = escalaZoom,
                                    scaleY = escalaZoom,
                                    translationX = desplazamientoX,
                                    translationY = desplazamientoY,
                                    rotationZ = gradosRotacion
                                )
                        )

                        // Cuadrícula guía de tercios (estilo cámara profesional)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val ancho = size.width
                            val alto = size.height
                            val colorGuia = Color.White.copy(alpha = 0.45f)

                            // Marco exterior
                            drawRect(color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 3f))

                            // Líneas verticales y horizontales de tercios
                            drawLine(colorGuia, Offset(ancho / 3f, 0f), Offset(ancho / 3f, alto), strokeWidth = 1.5f)
                            drawLine(colorGuia, Offset(2f * ancho / 3f, 0f), Offset(2f * ancho / 3f, alto), strokeWidth = 1.5f)
                            drawLine(colorGuia, Offset(0f, alto / 3f), Offset(ancho, alto / 3f), strokeWidth = 1.5f)
                            drawLine(colorGuia, Offset(0f, 2f * alto / 3f), Offset(ancho, 2f * alto / 3f), strokeWidth = 1.5f)
                        }
                    } else {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                // Paso 4: Control deslizante de Zoom y botones de Rotar / Restablecer
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zoom: ${(escalaZoom * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { gradosRotacion = (gradosRotacion + 90f) % 360f },
                                label = { Text("Rotar 90°", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            AssistChip(
                                onClick = {
                                    escalaZoom = 1f
                                    desplazamientoX = 0f
                                    desplazamientoY = 0f
                                    gradosRotacion = 0f
                                },
                                label = { Text("Centrar", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    Slider(
                        value = escalaZoom,
                        onValueChange = { escalaZoom = it },
                        valueRange = 1f..3.5f
                    )
                }

                // Paso 5: Botones de Cancelar y Confirmar Recorte
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = alCancelar,
                        enabled = !procesandoRecorte,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val bmp = bitmapOriginal ?: return@Button
                            procesandoRecorte = true
                            alcanceCorrutina.launch {
                                val uriRecortada = procesarYGuardarRecorte(
                                    contexto = contexto,
                                    bitmapBase = bmp,
                                    zoom = escalaZoom,
                                    despX = desplazamientoX,
                                    despY = desplazamientoY,
                                    rotacion = gradosRotacion
                                ) ?: uriImagenOriginal
                                procesandoRecorte = false
                                alConfirmarRecorte(uriRecortada)
                            }
                        },
                        enabled = !procesandoRecorte && bitmapOriginal != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        if (procesandoRecorte) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Usar esta Foto", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/*
 * Función que aplica la rotación, el zoom y el encuadre elegidos por el usuario
 * y guarda la imagen optimizada (900x900 px JPEG) lista para subir a Appwrite.
 */
private suspend fun procesarYGuardarRecorte(
    contexto: Context,
    bitmapBase: Bitmap,
    zoom: Float,
    despX: Float,
    despY: Float,
    rotacion: Float
): Uri? = withContext(Dispatchers.IO) {
    try {
        // Paso 1: Aplicar rotación si el usuario giró la foto
        val matrizRotacion = Matrix().apply {
            if (rotacion != 0f) postRotate(rotacion)
        }
        val bitmapRotado = Bitmap.createBitmap(
            bitmapBase, 0, 0, bitmapBase.width, bitmapBase.height, matrizRotacion, true
        )

        // Paso 2: Calcular el recorte cuadrado centrado según el zoom y desplazamiento
        val ladoMenor = min(bitmapRotado.width, bitmapRotado.height)
        val tamanoRecorte = (ladoMenor / zoom).toInt().coerceAtLeast(100)

        val factorDespX = -(despX / 280f) * (ladoMenor / zoom)
        val factorDespY = -(despY / 280f) * (ladoMenor / zoom)

        val centroX = (bitmapRotado.width / 2f + factorDespX).toInt()
        val centroY = (bitmapRotado.height / 2f + factorDespY).toInt()

        val inicioX = (centroX - tamanoRecorte / 2).coerceIn(0, max(0, bitmapRotado.width - tamanoRecorte))
        val inicioY = (centroY - tamanoRecorte / 2).coerceIn(0, max(0, bitmapRotado.height - tamanoRecorte))

        val anchoFinal = min(tamanoRecorte, bitmapRotado.width - inicioX)
        val altoFinal = min(tamanoRecorte, bitmapRotado.height - inicioY)

        val bitmapRecortado = Bitmap.createBitmap(bitmapRotado, inicioX, inicioY, anchoFinal, altoFinal)
        val bitmapEscalado = Bitmap.createScaledBitmap(bitmapRecortado, 900, 900, true)

        // Paso 3: Guardar el archivo JPEG optimizado en la carpeta temporal de la app
        val archivoSalida = File(contexto.cacheDir, "foto_ajustada_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivoSalida).use { stream ->
            bitmapEscalado.compress(Bitmap.CompressFormat.JPEG, 88, stream)
        }

        Uri.fromFile(archivoSalida)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
