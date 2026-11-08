package com.example.app_adopcion_d_mascotas.navegacion

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioAutenticacionAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.RepositorioMascotasAppwrite
import com.example.app_adopcion_d_mascotas.ui.componentes.FondoApp
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloCatalogo
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloSolicitudes
import com.example.app_adopcion_d_mascotas.ui.pantallas.autenticacion.PantallaInicioSesion
import com.example.app_adopcion_d_mascotas.ui.pantallas.catalogo.PantallaCatalogo
import com.example.app_adopcion_d_mascotas.ui.pantallas.chat.PantallaChat
import com.example.app_adopcion_d_mascotas.ui.pantallas.citas.PantallaCitas
import com.example.app_adopcion_d_mascotas.ui.pantallas.detalle.PantallaDetalleMascota
import com.example.app_adopcion_d_mascotas.ui.pantallas.perfil.PantallaPerfil
import com.example.app_adopcion_d_mascotas.ui.pantallas.publicar.PantallaCrearEditarMascota
import com.example.app_adopcion_d_mascotas.ui.pantallas.seguimiento.PantallaSeguimiento
import com.example.app_adopcion_d_mascotas.ui.pantallas.solicitudes.PantallaFormularioAdopcion
import com.example.app_adopcion_d_mascotas.ui.pantallas.solicitudes.PantallaSolicitudes
import kotlinx.coroutines.launch

/*
 * ============================================================================
 * ARCHIVO: NavegacionApp.kt
 * CAPA: Navegación Central (Jetpack Navigation Compose)
 * ¿QUÉ HACE?: Conecta las 10 pantallas de la aplicación como un mapa de rutas.
 *             Si el usuario ya tenía su sesión iniciada en Appwrite Cloud,
 *             lo lleva automáticamente al Catálogo al abrir la app.
 * ============================================================================
 */
@Composable
fun NavegacionApp() {
    // Paso 1: Crear el controlador de navegación y el alcance para tareas en la nube
    val controladorNavegacion = rememberNavController()
    val alcanceCorrutina = rememberCoroutineScope()

    // Paso 2: Crear los ModeloVista compartidos entre pantallas
    val modeloCatalogo: ModeloCatalogo = viewModel()
    val modeloSolicitudes: ModeloSolicitudes = viewModel()

    // Paso 3: Guardar en estado reactivo quién es el usuario conectado actualmente
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    // Paso 4: Al abrir la aplicación, verificar si ya hay sesión activa en Appwrite Cloud
    LaunchedEffect(Unit) {
        val sesionActiva = RepositorioAutenticacionAppwrite.obtenerUsuarioActual()
        if (sesionActiva != null) {
            usuarioActual = sesionActiva
            modeloCatalogo.cargarMascotas()
            controladorNavegacion.navigate(Ruta.Catalogo.ruta) {
                popUpTo(Ruta.InicioSesion.ruta) { inclusive = true }
            }
        }
    }

    // Paso 5: Envolver todas las pantallas en nuestro fondo cálido con huellitas
    FondoApp {
        NavHost(
            navController = controladorNavegacion,
            startDestination = Ruta.InicioSesion.ruta
        ) {
            // RUTA 1: Inicio de Sesión y Registro con Roles (RF1)
            composable(Ruta.InicioSesion.ruta) {
                PantallaInicioSesion(
                    alIniciarSesion = { usuarioConectado ->
                        usuarioActual = usuarioConectado
                        modeloCatalogo.cargarMascotas()
                        controladorNavegacion.navigate(Ruta.Catalogo.ruta) {
                            popUpTo(Ruta.InicioSesion.ruta) { inclusive = true }
                        }
                    }
                )
            }

            // RUTA 2: Catálogo de Mascotas con Búsqueda y Filtros Combinados (RF4 y RF5)
            composable(Ruta.Catalogo.ruta) {
                PantallaCatalogo(
                    usuarioActual = usuarioActual,
                    modeloCatalogo = modeloCatalogo,
                    alHacerClicMascota = { idMascota ->
                        controladorNavegacion.navigate(Ruta.Detalle.crearRuta(idMascota))
                    },
                    alPublicarMascota = {
                        controladorNavegacion.navigate(Ruta.Publicar.crearRuta())
                    },
                    alVerSolicitudes = {
                        controladorNavegacion.navigate(Ruta.Solicitudes.ruta)
                    },
                    alVerCitas = {
                        controladorNavegacion.navigate(Ruta.Citas.ruta)
                    },
                    alVerPerfil = {
                        controladorNavegacion.navigate(Ruta.Perfil.ruta)
                    },
                    alCerrarSesion = {
                        alcanceCorrutina.launch {
                            RepositorioAutenticacionAppwrite.cerrarSesion()
                            usuarioActual = null
                            controladorNavegacion.navigate(Ruta.InicioSesion.ruta) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // RUTA 3: Detalle de Mascota con Carrusel, Mapa Interactivo, Chat y Gestión (RF6 y RF7)
            composable(
                route = Ruta.Detalle.ruta,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { entrada ->
                val idMascota = entrada.arguments?.getString("petId") ?: ""
                val mascota = modeloCatalogo.todasLasMascotas.find { it.id == idMascota }

                PantallaDetalleMascota(
                    mascota = mascota,
                    usuarioActual = usuarioActual,
                    alVolver = { controladorNavegacion.popBackStack() },
                    alEditar = { id ->
                        controladorNavegacion.navigate(Ruta.Publicar.crearRuta(id))
                    },
                    alEliminar = { id ->
                        alcanceCorrutina.launch {
                            RepositorioMascotasAppwrite.eliminarMascota(id)
                            modeloCatalogo.cargarMascotas()
                            controladorNavegacion.popBackStack()
                        }
                    },
                    alMarcarAdoptada = { id ->
                        alcanceCorrutina.launch {
                            RepositorioMascotasAppwrite.marcarComoAdoptada(id)
                            modeloCatalogo.cargarMascotas()
                            controladorNavegacion.popBackStack()
                        }
                    },
                    alCambiarEstado = { id, nuevoEstado ->
                        alcanceCorrutina.launch {
                            RepositorioMascotasAppwrite.cambiarEstadoMascota(id, nuevoEstado)
                            modeloCatalogo.cargarMascotas()
                        }
                    },
                    alAdoptar = { id ->
                        controladorNavegacion.navigate(Ruta.FormularioAdopcion.crearRuta(id))
                    },
                    alAbrirChat = { idChat, nombreMascota ->
                        controladorNavegacion.navigate(
                            Ruta.Chat.crearRuta(
                                idChat = idChat,
                                nombreMascota = nombreMascota,
                                nombreDestinatario = mascota?.nombreRefugio ?: "Refugio"
                            )
                        )
                    },
                    alVerSeguimiento = { id ->
                        controladorNavegacion.navigate(
                            Ruta.Seguimiento.crearRuta(
                                idMascota = id,
                                nombreMascota = mascota?.nombre ?: "",
                                nombreRefugio = mascota?.nombreRefugio ?: "Refugio"
                            )
                        )
                    }
                )
            }

            // RUTA 4: Publicar o Editar Mascota con Recorte de Foto y Mapa GPS (RF3, RF6 y RF7)
            composable(
                route = Ruta.Publicar.ruta,
                arguments = listOf(
                    navArgument("petId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entrada ->
                val idMascota = entrada.arguments?.getString("petId")
                val mascotaExistente = idMascota?.let { id ->
                    modeloCatalogo.todasLasMascotas.find { it.id == id }
                }

                PantallaCrearEditarMascota(
                    mascotaExistente = mascotaExistente,
                    usuarioActual = usuarioActual,
                    alVolver = { controladorNavegacion.popBackStack() },
                    alGuardarExitosamente = {
                        modeloCatalogo.cargarMascotas()
                        controladorNavegacion.popBackStack()
                    }
                )
            }

            // RUTA 5: Formulario de Solicitud de Adopción (RF8)
            composable(
                route = Ruta.FormularioAdopcion.ruta,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { entrada ->
                val idMascota = entrada.arguments?.getString("petId") ?: ""
                val mascota = modeloCatalogo.todasLasMascotas.find { it.id == idMascota }

                PantallaFormularioAdopcion(
                    mascota = mascota,
                    usuarioActual = usuarioActual,
                    modeloSolicitudes = modeloSolicitudes,
                    alVolver = { controladorNavegacion.popBackStack() },
                    alEnviarSolicitud = {
                        controladorNavegacion.popBackStack()
                        controladorNavegacion.navigate(Ruta.Solicitudes.ruta)
                    }
                )
            }

            // RUTA 6: Panel de Solicitudes con Aprobación en Cascada y Botón de Chat (RF9, RF10, RF13)
            composable(Ruta.Solicitudes.ruta) {
                PantallaSolicitudes(
                    usuarioActual = usuarioActual,
                    modeloSolicitudes = modeloSolicitudes,
                    modeloCatalogo = modeloCatalogo,
                    alVolver = { controladorNavegacion.popBackStack() },
                    alIrACitas = {
                        controladorNavegacion.navigate(Ruta.Citas.ruta)
                    },
                    alAbrirChat = { idChat, nombreMascota, nombreDestinatario ->
                        controladorNavegacion.navigate(
                            Ruta.Chat.crearRuta(
                                idChat = idChat,
                                nombreMascota = nombreMascota,
                                nombreDestinatario = nombreDestinatario
                            )
                        )
                    }
                )
            }

            // RUTA 7: Agenda de Citas y Entrevistas (RF20)
            composable(Ruta.Citas.ruta) {
                PantallaCitas(
                    usuarioActual = usuarioActual,
                    modeloSolicitudes = modeloSolicitudes,
                    alVolver = { controladorNavegacion.popBackStack() }
                )
            }

            // RUTA 8: Perfil de Usuario con Recorte de Avatar y Selector de Tema (RF2 y RF16)
            composable(Ruta.Perfil.ruta) {
                PantallaPerfil(
                    usuarioActual = usuarioActual,
                    alActualizarUsuario = { actualizado ->
                        usuarioActual = actualizado
                    },
                    alVolver = { controladorNavegacion.popBackStack() },
                    alCerrarSesion = {
                        alcanceCorrutina.launch {
                            RepositorioAutenticacionAppwrite.cerrarSesion()
                            usuarioActual = null
                            controladorNavegacion.navigate(Ruta.InicioSesion.ruta) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    alHacerClicMascota = { idMascota ->
                        controladorNavegacion.navigate(Ruta.Detalle.crearRuta(idMascota))
                    },
                    alAbrirChat = { idChat, nombreDestinatario ->
                        controladorNavegacion.navigate(
                            Ruta.Chat.crearRuta(
                                idChat = idChat,
                                nombreDestinatario = nombreDestinatario
                            )
                        )
                    }
                )
            }

            // RUTA 9: Chat en Tiempo Real Adoptante - Refugio (RF13 y RF14)
            composable(
                route = Ruta.Chat.ruta,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("petName") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("receiverName") {
                        type = NavType.StringType
                        defaultValue = "Refugio"
                    }
                )
            ) { entrada ->
                val idChat = entrada.arguments?.getString("chatId") ?: ""
                val nombreMascota = entrada.arguments?.getString("petName") ?: ""
                val nombreDestinatario = entrada.arguments?.getString("receiverName") ?: "Refugio"

                PantallaChat(
                    idChat = idChat,
                    nombreMascota = nombreMascota,
                    nombreDestinatario = nombreDestinatario,
                    alVolver = { controladorNavegacion.popBackStack() }
                )
            }

            // RUTA 10: Seguimiento Post-Adopción con Fotos en la Nube (RF11)
            composable(
                route = Ruta.Seguimiento.ruta,
                arguments = listOf(
                    navArgument("petId") { type = NavType.StringType },
                    navArgument("petName") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("shelterName") {
                        type = NavType.StringType
                        defaultValue = "Refugio"
                    }
                )
            ) { entrada ->
                val idMascota = entrada.arguments?.getString("petId") ?: ""
                val mascota = modeloCatalogo.todasLasMascotas.find { it.id == idMascota }

                PantallaSeguimiento(
                    mascota = mascota,
                    usuarioActual = usuarioActual,
                    alVolver = { controladorNavegacion.popBackStack() }
                )
            }
        }
    }
}
