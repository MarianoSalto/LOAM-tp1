package com.example.primertpdeappmoviles.data.remote

import com.example.primertpdeappmoviles.data.remote.dto.ClimaResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz para definir las peticiones a la API de Open-Meteo.
 */
interface ClimaApiServicio {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") currentParams: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m",
        @Query("timezone") timezone: String = "auto"
    ): ClimaResponseDto
}
