package com.example.primertpdeappmoviles.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClimaResponseDto(
    @SerializedName("current") val current: CurrentWeatherDataDto
)

data class CurrentWeatherDataDto(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("weather_code") val weatherCode: Int
)