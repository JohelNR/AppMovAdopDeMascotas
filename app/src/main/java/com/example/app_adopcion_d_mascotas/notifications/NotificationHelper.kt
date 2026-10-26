package com.example.app_adopcion_d_mascotas.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.app_adopcion_d_mascotas.MainActivity

// RF14 & RF20: Utilidad para Notificaciones locales y Recordatorios de Citas
object NotificationHelper {

    const val CHANNEL_ID = "adopcion_channel"
    private const val CHANNEL_NAME = "Adopciones y Citas"
    private const val CHANNEL_DESC = "Notificaciones sobre el estado de solicitudes, citas y adopciones"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // RF14: Mostrar notificación inmediata en la barra de estado
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)

        // Verificar permiso en Android 13+ (POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    // RF20: Programar recordatorio de cita con AlarmManager
    fun scheduleAppointmentAlarm(
        context: Context,
        appointmentId: String,
        petName: String,
        shelterName: String,
        date: String,
        time: String,
        triggerAtMillis: Long = System.currentTimeMillis() + 10_000 // Por defecto 10s para pruebas en vivo
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
            putExtra(AppointmentReminderReceiver.EXTRA_TITLE, "📅 Recordatorio de Cita: $petName")
            putExtra(
                AppointmentReminderReceiver.EXTRA_MESSAGE,
                "Tienes tu cita programada con $shelterName para $date a las $time. ¡No olvides asistir!"
            )
            putExtra(AppointmentReminderReceiver.EXTRA_ID, appointmentId.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Si el dispositivo restringe alarmas exactas, fallback a alarma inexacta
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
