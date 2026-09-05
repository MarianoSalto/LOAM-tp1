package com.example.primertpdeappmoviles.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * LocationService: Servicio utilitario para gestionar permisos y acceso a la ubicación.
 */
class LocationService(private val context: Context) {

    // Cliente principal para interactuar con los servicios de ubicación de Google
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        // Código de identificación para las solicitudes de permisos de ubicación
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        // Lista de permisos requeridos para GPS y Red
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Comprueba si todos los permisos de ubicación necesarios están otorgados.
     */
    fun hasLocationPermission(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Solicita permisos de ubicación desde una Activity.
     */
    fun requestLocationPermissions(activity: android.app.Activity) {
        androidx.core.app.ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Solicita permisos de ubicación desde un Fragment.
     */
    fun requestLocationPermissions(fragment: androidx.fragment.app.Fragment) {
        fragment.requestPermissions(
            REQUIRED_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
}
