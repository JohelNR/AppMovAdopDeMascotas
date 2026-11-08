package com.example.app_adopcion_d_mascotas

import com.example.app_adopcion_d_mascotas.datos.modelos.Cita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoCita
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoSolicitud
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.modelos.MensajeChat
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.SeguimientoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.SolicitudAdopcion
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * ============================================================================
 * PRUEBAS UNITARIAS DE MODELOS Y CONSTANTES EN ESPAÑOL (RF1 - RF20)
 * ============================================================================
 */
class ExampleUnitTest {

    @Test
    fun verificarConstantesTablasEnEspanol() {
        assertEquals("adopcion_db", ClienteAppwrite.ID_BASE_DATOS)
        assertEquals("fotos_mascotas", ClienteAppwrite.ID_BUCKET_FOTOS)
        assertEquals("usuarios", ClienteAppwrite.TABLA_USUARIOS)
        assertEquals("mascotas", ClienteAppwrite.TABLA_MASCOTAS)
        assertEquals("solicitudes", ClienteAppwrite.TABLA_SOLICITUDES)
        assertEquals("citas", ClienteAppwrite.TABLA_CITAS)
        assertEquals("seguimientos", ClienteAppwrite.TABLA_SEGUIMIENTOS)
        assertEquals("mensajes", ClienteAppwrite.TABLA_MENSAJES)
    }

    @Test
    fun verificarModelosYPropiedadesEnEspanol() {
        val usuario = Usuario(
            id = "usr_01",
            name = "Refugio Esperanza Cajamarca",
            email = "contacto@esperanza.org",
            role = RolUsuario.REFUGIO,
            phone = "987654321",
            bio = "Rescatamos animalitos en Cajamarca"
        )
        assertEquals(RolUsuario.REFUGIO, usuario.rol)
        assertEquals("Refugio Esperanza Cajamarca", usuario.nombre)
        assertEquals("987654321", usuario.telefono)

        val mascota = Mascota(
            id = "mascota_01",
            name = "Max",
            species = "Perro",
            breed = "Mestizo",
            ageYears = 2,
            gender = "Macho",
            size = "Mediano",
            description = "Perrito noble y vacunado",
            shelterName = usuario.nombre,
            shelterId = usuario.id,
            location = "Cajamarca",
            latitude = -7.1617,
            longitude = -78.5128,
            isUrgent = true,
            status = EstadoMascota.DISPONIBLE
        )
        assertEquals(EstadoMascota.DISPONIBLE, mascota.estado)
        assertEquals("Max", mascota.nombre)
        assertTrue(mascota.esUrgente)

        val solicitud = SolicitudAdopcion(
            id = "sol_01",
            petId = mascota.id,
            petName = mascota.nombre,
            applicantId = "usr_02",
            applicantName = "María Pérez",
            applicantEmail = "maria@correo.com",
            shelterName = usuario.nombre,
            reason = "Tengo un jardín amplio y mucho tiempo para cuidarlo",
            hasYard = true,
            hasExperience = true,
            termsAccepted = true,
            status = EstadoSolicitud.PENDIENTE
        )
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.estado)
        assertEquals("María Pérez", solicitud.nombreSolicitante)

        val cita = Cita(
            id = "cita_01",
            requestId = solicitud.id,
            petName = mascota.nombre,
            applicantName = solicitud.nombreSolicitante,
            shelterName = usuario.nombre,
            date = "20/11/2026",
            time = "10:30 AM",
            modality = "Presencial en el Refugio",
            status = EstadoCita.PROPUESTA
        )
        assertEquals(EstadoCita.PROPUESTA, cita.estado)
        assertEquals("20/11/2026", cita.fecha)

        val seguimiento = SeguimientoMascota(
            id = "seg_01",
            petId = mascota.id,
            petName = mascota.nombre,
            applicantId = solicitud.idSolicitante,
            applicantName = solicitud.nombreSolicitante,
            shelterName = usuario.nombre,
            date = "25/11/2026",
            healthNotes = "Vacunas al día y excelente apetito",
            adaptationNotes = "Duerme tranquilo en su cama",
            status = "ENVIADO"
        )
        assertEquals("ENVIADO", seguimiento.estado)
        assertEquals("Vacunas al día y excelente apetito", seguimiento.notasSalud)

        val mensaje = MensajeChat(
            id = "msg_01",
            chatId = "chat_mascota_01_usr_02",
            petName = mascota.nombre,
            senderId = solicitud.idSolicitante,
            senderName = solicitud.nombreSolicitante,
            receiverId = usuario.id,
            text = "Hola, me gustaría coordinar la visita",
            isRead = false
        )
        assertEquals("chat_mascota_01_usr_02", mensaje.idChat)
        assertEquals("Hola, me gustaría coordinar la visita", mensaje.texto)
    }
}
