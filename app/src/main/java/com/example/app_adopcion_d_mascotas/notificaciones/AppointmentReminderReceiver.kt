package com.example.app_adopcion_d_mascotas.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// RF20: Receptor del sistema que se activa cuando la alarma de la cita expira
class AppointmentReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_notification_title"
        const val EXTRA_MESSAGE = "extra_notification_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, (System.currentTimeMillis() % 10000).toInt())
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "📅 Recordatorio de Cita"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Tienes una cita programada para tu proceso de adopción."

        AyudanteNotificaciones.showNotification(
            context = context,
            notificationId = id,
            title = title,
            message = message
        )
    }
}
