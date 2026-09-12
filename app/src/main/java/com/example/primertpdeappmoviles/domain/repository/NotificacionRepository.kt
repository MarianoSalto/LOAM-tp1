package com.example.primertpdeappmoviles.domain.repository

interface NotificationRepository{
    suspend fun registrarTokenEnServidor(token: String): Result<Unit>
}