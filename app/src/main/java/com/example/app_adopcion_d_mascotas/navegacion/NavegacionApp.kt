package com.example.app_adopcion_d_mascotas.navegacion

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
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

@Composable
fun NavegacionApp() {
    // 1. El controlador de navegación oficial de Google Compose
    val controladorNavegacion = rememberNavController()
    val alcanceCorrutina = rememberCoroutineScope()

    // 2. ViewModels compartidos para todo el ciclo de vida de la aplicación
    val modeloCatalogo: ModeloCatalogo = viewModel()
    val modeloSolicitudes: ModeloSolicitudes = viewModel()

    // 3. Estado en memoria del usuario actualmente conectado
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    // 4. El NavHost envuelto en FondoApp para el fondo con huellitas en toda la app
    FondoApp {
        NavHost(
            navController = controladorNavegacion,
            startDestination = Ruta.InicioSesion.ruta
        ) {
            // RUTA 1: Login y Registro (RF1)
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

            // RUTA 2: Catálogo de Mascotas con Filtros y Búsqueda (RF4, RF5, RF6)
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
                        usuarioActual = null
                        controladorNavegacion.navigate(Ruta.InicioSesion.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // RUTA 3: Detalle de Mascota con fotos, mapa, chat y seguimiento (RF4, RF7)
            composable(
                route = Ruta.Detalle.ruta,
                arguments = listOf(
                    navArgument("petId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val idMascota = backStackEntry.arguments?.getString("petId") ?: ""
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
                            RepositorioMascotasAppwrite.deletePet(id)
                            modeloCatalogo.cargarMascotas()
                            controladorNavegacion.popBackStack()
                        }
                    },
                    alMarcarAdoptada = { id ->
                        alcanceCorrutina.launch {
                            RepositorioMascotasAppwrite.markAsAdopted(id)
                            modeloCatalogo.cargarMascotas()
                            controladorNavegacion.popBackStack()
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
                                nombreDestinatario = mascota?.shelterName ?: "Refugio"
                            )
                        )
                    },
                    alVerSeguimiento = { id ->
                        controladorNavegacion.navigate(
                            Ruta.Seguimiento.crearRuta(
                                idMascota = id,
                                nombreMascota = mascota?.name ?: "",
                                nombreRefugio = mascota?.shelterName ?: "Refugio"
                            )
                        )
                    }
                )
            }

            // RUTA 4: Crear o Editar Mascota con subida real de fotos (RF3)
            composable(
                route = Ruta.Publicar.ruta,
                arguments = listOf(
                    navArgument("petId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val idMascota = backStackEntry.arguments?.getString("petId")
                val mascotaExistente = idMascota?.let { id -> modeloCatalogo.todasLasMascotas.find { it.id == id } }

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

            // RUTA 5: Formulario de Postulación de Adopción (RF8)
            composable(
                route = Ruta.FormularioAdopcion.ruta,
                arguments = listOf(
                    navArgument("petId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val idMascota = backStackEntry.arguments?.getString("petId") ?: ""
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

            // RUTA 6: Panel de Solicitudes (RF9, RF10)
            composable(Ruta.Solicitudes.ruta) {
                PantallaSolicitudes(
                    usuarioActual = usuarioActual,
                    modeloSolicitudes = modeloSolicitudes,
                    modeloCatalogo = modeloCatalogo,
                    alVolver = { controladorNavegacion.popBackStack() },
                    alIrACitas = {
                        controladorNavegacion.navigate(Ruta.Citas.ruta)
                    }
                )
            }

            // RUTA 7: Coordinación de Citas y Entrevistas (RF20)
            composable(Ruta.Citas.ruta) {
                PantallaCitas(
                    usuarioActual = usuarioActual,
                    modeloSolicitudes = modeloSolicitudes,
                    alVolver = { controladorNavegacion.popBackStack() }
                )
            }

            // RUTA 8: Perfil de Usuario con foto de avatar y gestión (RF2)
            composable(Ruta.Perfil.ruta) {
                PantallaPerfil(
                    usuarioActual = usuarioActual,
                    alActualizarUsuario = { actualizado ->
                        usuarioActual = actualizado
                    },
                    alVolver = { controladorNavegacion.popBackStack() },
                    alCerrarSesion = {
                        usuarioActual = null
                        controladorNavegacion.navigate(Ruta.InicioSesion.ruta) {
                            popUpTo(0) { inclusive = true }
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

            // RUTA 9: Chat en Tiempo Real Adoptante - Refugio (RF13)
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
            ) { backStackEntry ->
                val idChat = backStackEntry.arguments?.getString("chatId") ?: ""
                val nombreMascota = backStackEntry.arguments?.getString("petName") ?: ""
                val nombreDestinatario = backStackEntry.arguments?.getString("receiverName") ?: "Refugio"

                PantallaChat(
                    idChat = idChat,
                    nombreMascota = nombreMascota,
                    nombreDestinatario = nombreDestinatario,
                    alVolver = { controladorNavegacion.popBackStack() }
                )
            }

            // RUTA 10: Formulario y Seguimiento Post-Adopción (RF11)
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
            ) { backStackEntry ->
                val idMascota = backStackEntry.arguments?.getString("petId") ?: ""
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

// Wrapper para compatibilidad
@Composable
fun AppNavigation() = NavegacionApp()
