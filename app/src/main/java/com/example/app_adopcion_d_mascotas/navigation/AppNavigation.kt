package com.example.app_adopcion_d_mascotas.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.repository.PetRepository
import com.example.app_adopcion_d_mascotas.ui.screens.auth.LoginScreen
import com.example.app_adopcion_d_mascotas.ui.screens.detail.PetDetailScreen
import com.example.app_adopcion_d_mascotas.ui.screens.feed.FeedScreen
import com.example.app_adopcion_d_mascotas.ui.screens.publish.CreateEditPetScreen

@Composable
fun AppNavigation() {
    // 1. El controlador de navegación oficial de Google Compose
    val navController = rememberNavController()

    // 2. Estado en memoria del usuario actualmente conectado
    var currentUser by remember { mutableStateOf<User?>(null) }

    // 3. El NavHost que decide qué pantalla mostrar según la ruta activa
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // RUTA 1: Login y Registro (RF1)
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { loggedUser ->
                    currentUser = loggedUser
                    // Navegamos al Feed y limpiamos el Login del historial (popUpTo)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // RUTA 2: Catálogo / Feed de Mascotas (RF4)
        composable(Screen.Feed.route) {
            FeedScreen(
                currentUser = currentUser,
                pets = PetRepository.pets,
                onPetClick = { petId ->
                    navController.navigate(Screen.Detail.createRoute(petId))
                },
                onAddPetClick = {
                    navController.navigate(Screen.Publish.createRoute())
                },
                onLogoutClick = {
                    currentUser = null
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // RUTA 3: Detalle de Mascota (RF4)
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            val pet = PetRepository.getPetById(petId)

            PetDetailScreen(
                pet = pet,
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.Publish.createRoute(id))
                },
                onDeleteClick = { id ->
                    PetRepository.deletePet(id)
                    navController.popBackStack()
                },
                onMarkAdoptedClick = { id ->
                    PetRepository.markAsAdopted(id)
                }
            )
        }

        // RUTA 4: Crear o Editar Mascota (RF3 CRUD)
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
            val existingPet = petId?.let { PetRepository.getPetById(it) }

            CreateEditPetScreen(
                existingPet = existingPet,
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() },
                onSavePet = { petToSave ->
                    if (existingPet != null) {
                        PetRepository.updatePet(petToSave)
                    } else {
                        PetRepository.addPet(petToSave)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}