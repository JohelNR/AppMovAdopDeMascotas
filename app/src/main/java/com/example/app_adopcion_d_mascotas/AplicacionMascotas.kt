package com.example.app_adopcion_d_mascotas

import android.app.Application
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import com.example.app_adopcion_d_mascotas.di.ContenedorApp

class AplicacionMascotas : Application() {
    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        ClienteAppwrite.init(this)
        contenedor = ContenedorApp(ClienteAppwrite)
    }
}
