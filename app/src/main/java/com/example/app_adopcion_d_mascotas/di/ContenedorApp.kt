package com.example.app_adopcion_d_mascotas.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app_adopcion_d_mascotas.AplicacionMascotas
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import com.example.app_adopcion_d_mascotas.datos.repositorios.*
import com.example.app_adopcion_d_mascotas.ui.modelovista.*

/** Un grafo por Application; los ViewModels reciben las mismas instancias por constructor. */
class ContenedorApp(cliente: ClienteAppwrite) {
    val autenticacion = RepositorioAutenticacionAppwrite(cliente)
    val mascotas = RepositorioMascotasAppwrite(cliente)
    val almacenamiento = RepositorioAlmacenamientoAppwrite(cliente)
    val solicitudes = RepositorioSolicitudesAppwrite(cliente, mascotas)
    val seguimiento = RepositorioSeguimientoAppwrite(cliente)
    val chat = RepositorioChatAppwrite(cliente)

    val fabrica = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
            ModeloAutenticacion::class.java -> ModeloAutenticacion(autenticacion)
            ModeloCatalogo::class.java -> ModeloCatalogo(mascotas)
            ModeloSolicitudes::class.java -> ModeloSolicitudes(solicitudes)
            ModeloPublicar::class.java -> ModeloPublicar(almacenamiento, mascotas)
            ModeloPerfil::class.java -> ModeloPerfil(almacenamiento, autenticacion, mascotas, solicitudes)
            ModeloSeguimiento::class.java -> ModeloSeguimiento(almacenamiento, seguimiento)
            ModeloChat::class.java -> ModeloChat(autenticacion, chat)
            else -> error("ViewModel no registrado: ${modelClass.name}")
        } as T
    }
}

@Composable
fun contenedorApp(): ContenedorApp =
    (LocalContext.current.applicationContext as AplicacionMascotas).contenedor

/** Conserva el scope del NavBackStackEntry y usa siempre la fábrica con dependencias. */
@Composable
inline fun <reified T : ViewModel> modeloVista(): T =
    androidx.lifecycle.viewmodel.compose.viewModel(factory = contenedorApp().fabrica)
