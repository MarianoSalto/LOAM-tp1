package com.example.primertpdeappmoviles.domain.repository

import com.example.primertpdeappmoviles.domain.model.DatosClimas

interface ClimaRepository {
    suspend fun fetchWeather(lat: Double, lon: Double): Result<DatosClimas>
}
