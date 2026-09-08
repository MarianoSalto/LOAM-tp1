package com.example.primertpdeappmoviles.domain.usecase

import com.example.primertpdeappmoviles.domain.model.Alerta// Clase que define a una alerta
import com.example.primertpdeappmoviles.domain.repository.LocationRepository//Clase asosiada a mi ubicación
import com.example.primertpdeappmoviles.domain.repository.WeatherRepository//Esta Clase escucha las alertas de la base de datos
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

class VerificarAlertaMeteorologicaUseCase (
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
)
{
    // Usamos 'suspend' porque getCurrentLocation() es una función de suspensión
    suspend fun ejecutar(onAlertaCapturada: (Alerta) -> Unit) {

        // 1. Verificar si la app tiene permisos asignados antes de operar
        if (!locationRepository.hasLocationPermission()) return

        // 2. Obtener la ubicación actual (fresca o en caché) de forma síncrona/suspendida
        val ubicacionUsuario = locationRepository.getCurrentLocation() ?: return

        // 3. Escuchar las alertas desde Firebase
        weatherRepository.escucharAlertas { alertas ->

            kotlinx.coroutines.MainScope().launch {
                val ubicacionUsuario = locationRepository.getCurrentLocation() ?: return@launch
                val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                for (alerta in alertas) {
                    // Comparamos solo la fecha (día)
                    if (alerta.fecha == fechaHoy) {
                        val distancia = calcularDistancia(
                            ubicacionUsuario.latitude, ubicacionUsuario.longitude,
                            alerta.latitud, alerta.longitud
                        )

                        if (distancia <= 10.0) { // Radio de 10 Kilómetros
                            onAlertaCapturada(alerta)
                        }
                    }
                }
            }
        }
    }

    // Fórmula de Haversine pura (sin dependencias de Android SDK)
    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}