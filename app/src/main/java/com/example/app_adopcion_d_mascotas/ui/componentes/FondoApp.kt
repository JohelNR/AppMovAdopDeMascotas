package com.example.app_adopcion_d_mascotas.ui.componentes

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
 */
@Composable
fun FondoApp(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Capa de huellitas decorativas (no interactivas)
        CapaHuellitas()

        // Contenido real de la pantalla encima
        content()
    }
}

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = FondoApp(modifier, content)

@Composable
private fun CapaHuellitas() {
    val paws = listOf(
        DatosHuella(0.08f, 0.05f, -20f, 28.dp, 0.10f),
        DatosHuella(0.80f, 0.03f, 15f,  22.dp, 0.08f),
        DatosHuella(0.55f, 0.10f, 30f,  18.dp, 0.07f),
        DatosHuella(0.20f, 0.18f, -35f, 24.dp, 0.09f),
        DatosHuella(0.90f, 0.20f, 10f,  32.dp, 0.11f),
        DatosHuella(0.05f, 0.35f, 25f,  20.dp, 0.08f),
        DatosHuella(0.70f, 0.32f, -15f, 26.dp, 0.09f),
        DatosHuella(0.40f, 0.42f, 40f,  16.dp, 0.06f),
        DatosHuella(0.85f, 0.50f, -30f, 30.dp, 0.10f),
        DatosHuella(0.15f, 0.55f, 5f,   22.dp, 0.07f),
        DatosHuella(0.60f, 0.60f, 20f,  28.dp, 0.09f),
        DatosHuella(0.30f, 0.68f, -10f, 18.dp, 0.08f),
        DatosHuella(0.92f, 0.72f, 35f,  24.dp, 0.10f),
        DatosHuella(0.10f, 0.78f, -25f, 20.dp, 0.07f),
        DatosHuella(0.50f, 0.82f, 15f,  32.dp, 0.11f),
        DatosHuella(0.75f, 0.88f, -40f, 22.dp, 0.08f),
        DatosHuella(0.25f, 0.92f, 30f,  26.dp, 0.09f),
        DatosHuella(0.65f, 0.96f, -5f,  18.dp, 0.06f),
    )

    Layout(
        content = {
            paws.forEach { paw ->
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier
                        .graphicsLayer(
                            rotationZ = paw.rotacion,
                            alpha = paw.opacidad
                        )
                )
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.mapIndexed { index, measurable ->
            val sizePx = paws[index].tamano.toPx().toInt()
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
                val x = (constraints.maxWidth * paw.fraccionX - placeable.width / 2).toInt()
                val y = (constraints.maxHeight * paw.fraccionY - placeable.height / 2).toInt()
                placeable.placeRelative(x, y)
            }
        }
    }
}

private data class DatosHuella(
    val fraccionX: Float,
    val fraccionY: Float,
    val rotacion: Float,
    val tamano: Dp,
    val opacidad: Float
)
