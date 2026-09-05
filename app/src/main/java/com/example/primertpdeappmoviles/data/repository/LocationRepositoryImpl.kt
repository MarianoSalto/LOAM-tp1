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

class LocationRepositoryImpl(private val context: Context) : LocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            // Intentar obtener la última ubicación conocida primero
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) return lastLocation

            // Si es null, pedir una ubicación fresca
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

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
