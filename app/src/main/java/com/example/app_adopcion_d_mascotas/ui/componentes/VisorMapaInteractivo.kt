package com.example.app_adopcion_d_mascotas.ui.componentes

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/*
 * ============================================================================
 * ARCHIVO: VisorMapaInteractivo.kt
 * CAPA: UI -> Componentes Reutilizables (RF7: Mapa y Ubicación GPS)
 * ¿QUÉ HACE?: Muestra un mapa real e interactivo con calles, zoom y marcador
 *             del refugio usando cartografía abierta (OpenStreetMap + Leaflet).
 *             Funciona al 100% gratis sin requerir tarjeta de crédito en Google.
 *             Además, permite tocar el mapa al publicar una mascota para
 *             elegir la coordenada exacta (latitud y longitud).
 * ============================================================================
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VisorMapaInteractivo(
    latitud: Double,
    longitud: Double,
    tituloMarcador: String,
    subtituloMarcador: String = "",
    permitirSeleccionarPunto: Boolean = false,
    alSeleccionarUbicacion: ((Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Escapar comillas simples para evitar errores en el texto del globo del mapa
    val tituloLimpio = remember(tituloMarcador) { tituloMarcador.replace("'", "\\'") }
    val subtituloLimpio = remember(subtituloMarcador) { subtituloMarcador.replace("'", "\\'") }

    // Paso 1: Generar el documento HTML ligero con el mapa interactivo real (Leaflet + OpenStreetMap)
    val htmlMapa = remember(latitud, longitud, tituloLimpio, subtituloLimpio, permitirSeleccionarPunto) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e8eef2; }
                .leaflet-popup-content-wrapper { border-radius: 12px; font-family: sans-serif; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: true }).setView([$latitud, $longitud], 15);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OpenStreetMap'
                }).addTo(map);

                var marker = L.marker([$latitud, $longitud]).addTo(map);
                marker.bindPopup("<b>$tituloLimpio</b><br>$subtituloLimpio").openPopup();

                if ($permitirSeleccionarPunto) {
                    map.on('click', function(e) {
                        var lat = e.latlng.lat;
                        var lng = e.latlng.lng;
                        marker.setLatLng([lat, lng]);
                        if (window.AndroidMapa) {
                            window.AndroidMapa.alCambiarPunto(lat, lng);
                        }
                    });
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    // Paso 2: Mostrar el visor web nativo de Android dentro de Jetpack Compose
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { contexto ->
            WebView(contexto).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                if (permitirSeleccionarPunto && alSeleccionarUbicacion != null) {
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun alCambiarPunto(lat: Double, lng: Double) {
                                alSeleccionarUbicacion(lat, lng)
                            }
                        },
                        "AndroidMapa"
                    )
                }
                loadDataWithBaseURL("https://openstreetmap.org", htmlMapa, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://openstreetmap.org", htmlMapa, "text/html", "UTF-8", null)
        }
    )
}
