package com.example.app_adopcion_d_mascotas

import com.example.app_adopcion_d_mascotas.notificaciones.AyudanteNotificaciones.fechaHoraMillis
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class RecordatoriosTest {
    @Test fun rechazaFechasImposiblesYHorasInvalidas() {
        assertNull(fechaHoraMillis("31/02/2028", "09:30"))
        assertNull(fechaHoraMillis("29/02/2027", "09:30"))
        assertNull(fechaHoraMillis("06/11/2028", "24:00"))
        assertNull(fechaHoraMillis("", ""))
    }

    @Test fun programaLaFechaElegidaEnLaZonaDelDispositivo() {
        val instante = fechaHoraMillis("29/02/2028", "14:35")!!
        val fecha = Instant.ofEpochMilli(instante).atZone(ZoneId.systemDefault())
        assertEquals(2028, fecha.year)
        assertEquals(2, fecha.monthValue)
        assertEquals(29, fecha.dayOfMonth)
        assertEquals(14, fecha.hour)
        assertEquals(35, fecha.minute)
    }
}
