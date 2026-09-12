package com.example.primertpdeappmoviles.data.remote.fmc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.primertpdeappmoviles.data.repository.NotificationRepositoryImpl
import com.example.primertpdeappmoviles.domain.usecase.RegistrarTokenAlertaUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio encargado de recibir y procesar las notificaciones push de Firebase (FCM).
 * Enfocado exclusivamente en la recepción de Alertas de Emergencia.
 */
class PushNotificationService : FirebaseMessagingService() {

    private val registrarTokenUseCase by lazy {
        val repository = NotificationRepositoryImpl(Any())
        RegistrarTokenAlertaUseCase(repository)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Registramos el token para recibir alertas dirigidas
        CoroutineScope(Dispatchers.IO).launch {
            registrarTokenUseCase(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Procesamos solo si el mensaje es una notificación de alerta
        remoteMessage.notification?.let {
            mostrarNotificacionAlerta(it.title, it.body)
        }
    }

    private fun mostrarNotificacionAlerta(title: String?, body: String?) {
        val channelId = "canal_alertas_emergencia"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Alertas de Emergencia", 
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title ?: "Nueva Alerta")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
