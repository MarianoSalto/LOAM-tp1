package com.example.primertpdeappmoviles.domain.usecase

import com.example.primertpdeappmoviles.domain.model.DatosClimas
import com.example.primertpdeappmoviles.domain.repository.ClimaRepository

class GetClimaUseCase(private val repository: ClimaRepository) {
    suspend operator fun invoke(lat: Double, lon: Double): Result<DatosClimas> {
        return repository.fetchWeather(lat, lon)
    }
}
