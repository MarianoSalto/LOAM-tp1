package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.data.remote.ClimaApiServicio
import com.example.primertpdeappmoviles.domain.model.DatosClimas
import com.example.primertpdeappmoviles.domain.repository.ClimaRepository

class ClimaRepositoryImpl(private val apiService: ClimaApiServicio) : ClimaRepository {

    override suspend fun fetchWeather(lat: Double, lon: Double): Result<DatosClimas> {
        return try {
            val response = apiService.getWeather(lat, lon)
            val current = response.current
            val (text, icon) = parseWeatherCode(current.weatherCode)

            Result.success(
                DatosClimas(
                    temperature = "${current.temperature}°C",
                    humidity = "${current.humidity}%",
                    windSpeed = "${current.windSpeed} km/h",
                    conditionText = text,
                    conditionIcon = icon
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("Despejado", "☀️")
            in 1..3 -> Pair("Parcialmente Nublado", "⛅")
            45, 48 -> Pair("Niebla", " Fluss 🌫️")
            in 51..67 -> Pair("Lluvia", "🌧️")
            in 71..77 -> Pair("Nieve", "🌨️")
            in 80..82 -> Pair("Chubascos", "🌦️")
            in 95..99 -> Pair("Tormenta", "⛈️")
            else -> Pair("Variable", "☁️")
        }
    }
}