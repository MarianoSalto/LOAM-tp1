package com.example.primertpdeappmoviles.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.primertpdeappmoviles.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

/**
 * LocationRepositoryImpl: Implementación de la captura de ubicación GPS.
 * Capa de Datos: Utiliza FusedLocationProviderClient para obtener coordenadas reales del dispositivo.
 */
class LocationRepositoryImpl(private val context: Context) : LocationRepository {

    // Cliente de servicios de ubicación de Google Play Services
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la ubicación actual intentando primero la última conocida y luego una petición fresca.
     */
    @SuppressLint("MissingPermission") // La verificación se hace en hasLocationPermission()
    override suspend fun getCurrentLocation(): Location? {
        // Si no hay permisos, no se puede obtener la ubicación
        if (!hasLocationPermission()) return null

        return try {
            // Intentar obtener la última ubicación conocida guardada en caché
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) return lastLocation

            // Si no hay ubicación en caché, pedir una actualización con alta precisión
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            // En caso de error de conexión o hardware, devolvemos null
            null
        }
    }

    /**
     * Verifica si el usuario ha otorgado permisos de ubicación (Fina o Aproximada).
     */
    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
