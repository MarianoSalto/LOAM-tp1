package com.example.primertpdeappmoviles.domain.usecase

import com.example.primertpdeappmoviles.domain.repository.NotificationRepository

/**
 * Caso de uso para registrar el token de notificaciones en el servidor.
 * Esto permite que el sistema envíe alertas dirigidas a este dispositivo.
 */
class RegistrarTokenAlertaUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(token: String): Result<Unit> {
        // Registramos el token para recibir alertas de emergencia
        return repository.registrarTokenEnServidor(token)
    }
}
