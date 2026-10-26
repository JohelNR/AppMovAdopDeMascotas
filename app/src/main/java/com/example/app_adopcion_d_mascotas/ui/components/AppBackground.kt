package com.example.app_adopcion_d_mascotas.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fondo decorativo de la app con huellitas semitransparentes dispersas.
 * Envuelve cualquier pantalla para dar el patrón de huellas sobre el fondo oscuro.
 *
 * Uso:
 *   AppBackground {
 *       // Contenido de tu pantalla aquí
 *   }
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Capa de huellitas decorativas (no interactivas)
        PawPrintLayer()

        // Contenido real de la pantalla encima
        content()
    }
}

/**
 * Capa de huellitas dispersas semitransparentes.
 * Posiciones y opacidades fijas para imitar el patrón de la imagen.
 */
@Composable
private fun PawPrintLayer() {
    // Cada huellita: (offsetX en %, offsetY en %, rotación en grados, tamaño, alfa)
    val paws = listOf(
        PawData(0.08f, 0.05f, -20f, 28.dp, 0.10f),
        PawData(0.80f, 0.03f, 15f,  22.dp, 0.08f),
        PawData(0.55f, 0.10f, 30f,  18.dp, 0.07f),
        PawData(0.20f, 0.18f, -35f, 24.dp, 0.09f),
        PawData(0.90f, 0.20f, 10f,  32.dp, 0.11f),
        PawData(0.05f, 0.35f, 25f,  20.dp, 0.08f),
        PawData(0.70f, 0.32f, -15f, 26.dp, 0.09f),
        PawData(0.40f, 0.42f, 40f,  16.dp, 0.06f),
        PawData(0.85f, 0.50f, -30f, 30.dp, 0.10f),
        PawData(0.15f, 0.55f, 5f,   22.dp, 0.07f),
        PawData(0.60f, 0.60f, 20f,  28.dp, 0.09f),
        PawData(0.30f, 0.68f, -10f, 18.dp, 0.08f),
        PawData(0.92f, 0.72f, 35f,  24.dp, 0.10f),
        PawData(0.10f, 0.78f, -25f, 20.dp, 0.07f),
        PawData(0.50f, 0.82f, 15f,  32.dp, 0.11f),
        PawData(0.75f, 0.88f, -40f, 22.dp, 0.08f),
        PawData(0.25f, 0.92f, 30f,  26.dp, 0.09f),
        PawData(0.65f, 0.96f, -5f,  18.dp, 0.06f),
    )

    Layout(
        content = {
            paws.forEach { paw ->
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFFE65100), // Naranja cálido de la marca
                    modifier = Modifier
                        .graphicsLayer(
                            rotationZ = paw.rotation,
                            alpha = paw.alpha
                        )
                )
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.mapIndexed { index, measurable ->
            val sizePx = paws[index].size.toPx().toInt()
            measurable.measure(
                constraints.copy(
                    minWidth = sizePx,
                    maxWidth = sizePx,
                    minHeight = sizePx,
                    maxHeight = sizePx
                )
            )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val paw = paws[index]
                val x = (constraints.maxWidth * paw.xFraction - placeable.width / 2).toInt()
                val y = (constraints.maxHeight * paw.yFraction - placeable.height / 2).toInt()
                placeable.placeRelative(x, y)
            }
        }
    }
}

private data class PawData(
    val xFraction: Float,   // posición horizontal: 0.0 = izquierda, 1.0 = derecha
    val yFraction: Float,   // posición vertical: 0.0 = arriba, 1.0 = abajo
    val rotation: Float,    // rotación en grados
    val size: Dp,           // tamaño del ícono
    val alpha: Float        // opacidad (0.0 = invisible, 1.0 = opaco)
)
