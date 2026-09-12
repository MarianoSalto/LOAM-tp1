package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepositoryImpl(
    private val apiService: Any // Reemplaza "Any" por tu interfaz de Retrofit/Ktor si la tienes
) : NotificationRepository {

    override suspend fun registrarTokenEnServidor(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Ejemplo de llamada HTTP simulada:
            // apiService.enviarToken(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}