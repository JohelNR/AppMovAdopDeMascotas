package com.example.app_adopcion_d_mascotas.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.repository.AppwritePetRepository
import com.example.app_adopcion_d_mascotas.ui.components.AppBackground
import com.example.app_adopcion_d_mascotas.ui.screens.appointments.AppointmentsScreen
import com.example.app_adopcion_d_mascotas.ui.screens.auth.LoginScreen
import com.example.app_adopcion_d_mascotas.ui.screens.detail.PetDetailScreen
import com.example.app_adopcion_d_mascotas.ui.screens.feed.FeedScreen
import com.example.app_adopcion_d_mascotas.ui.screens.publish.CreateEditPetScreen
import com.example.app_adopcion_d_mascotas.ui.screens.requests.AdoptionFormScreen
import com.example.app_adopcion_d_mascotas.ui.screens.requests.RequestsScreen
import com.example.app_adopcion_d_mascotas.ui.viewmodel.FeedViewModel
import com.example.app_adopcion_d_mascotas.ui.viewmodel.RequestsViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    // 1. El controlador de navegación oficial de Google Compose
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // 2. ViewModels compartidos para todo el ciclo de vida de la aplicación
    val feedViewModel: FeedViewModel = viewModel()
    val requestsViewModel: RequestsViewModel = viewModel()

    // 3. Estado en memoria del usuario actualmente conectado
    var currentUser by remember { mutableStateOf<User?>(null) }

    // 4. El NavHost envuelto en AppBackground para el fondo con huellitas en toda la app
    AppBackground {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // RUTA 1: Login y Registro (RF1)
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { loggedUser ->
                    currentUser = loggedUser
                    feedViewModel.loadPets()
                    // Navegamos al Feed y limpiamos el Login del historial
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // RUTA 2: Catálogo / Feed de Mascotas con Filtros y Búsqueda (RF4, RF5, RF6)
        composable(Screen.Feed.route) {
            FeedScreen(
                currentUser = currentUser,
                feedViewModel = feedViewModel,
                onPetClick = { petId ->
                    navController.navigate(Screen.Detail.createRoute(petId))
                },
                onAddPetClick = {
                    navController.navigate(Screen.Publish.createRoute())
                },
                onRequestsClick = {
                    navController.navigate(Screen.Requests.route)
                },
                onAppointmentsClick = {
                    navController.navigate(Screen.Appointments.route)
                },
                onLogoutClick = {
                    currentUser = null
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // RUTA 3: Detalle de Mascota con carrusel de fotos (RF4) y acción de adopción (RF8)
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            val pet = feedViewModel.allPets.find { it.id == petId }

            PetDetailScreen(
                pet = pet,
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.Publish.createRoute(id))
                },
                onDeleteClick = { id ->
                    coroutineScope.launch {
                        AppwritePetRepository.deletePet(id)
                        feedViewModel.loadPets()
                        navController.popBackStack()
                    }
                },
                onMarkAdoptedClick = { id ->
                    coroutineScope.launch {
                        AppwritePetRepository.markAsAdopted(id)
                        feedViewModel.loadPets()
                        navController.popBackStack()
                    }
                },
                onAdoptClick = { id ->
                    navController.navigate(Screen.AdoptionForm.createRoute(id))
                }
            )
        }

        // RUTA 4: Crear o Editar Mascota con subida real de fotos a Cloud Storage (RF3 CRUD)
        composable(
            route = Screen.Publish.route,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            val existingPet = petId?.let { id -> feedViewModel.allPets.find { it.id == id } }

            CreateEditPetScreen(
                existingPet = existingPet,
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() },
                onSavedSuccessfully = {
                    feedViewModel.loadPets()
                    navController.popBackStack()
                }
            )
        }

        // RUTA 5: Formulario de Postulación de Adopción (RF8)
        composable(
            route = Screen.AdoptionForm.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            val pet = feedViewModel.allPets.find { it.id == petId }

            AdoptionFormScreen(
                pet = pet,
                currentUser = currentUser,
                requestsViewModel = requestsViewModel,
                onBackClick = { navController.popBackStack() },
                onRequestSubmitted = {
                    navController.popBackStack()
                    navController.navigate(Screen.Requests.route)
                }
            )
        }

        // RUTA 6: Panel de Solicitudes de Adopción y Cierre Automático RF10
        composable(Screen.Requests.route) {
            RequestsScreen(
                currentUser = currentUser,
                requestsViewModel = requestsViewModel,
                feedViewModel = feedViewModel,
                onBackClick = { navController.popBackStack() },
                onGoToAppointments = {
                    navController.navigate(Screen.Appointments.route)
                }
            )
        }

        // RUTA 7: Coordinación de Citas y Entrevistas (RF20)
        composable(Screen.Appointments.route) {
            AppointmentsScreen(
                currentUser = currentUser,
                requestsViewModel = requestsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
    } // fin AppBackground
}