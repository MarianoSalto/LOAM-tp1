package com.example.primertpdeappmoviles.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun hasLocationPermission(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestLocationPermissions(activity: android.app.Activity) {
        androidx.core.app.ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun requestLocationPermissions(fragment: androidx.fragment.app.Fragment) {
        fragment.requestPermissions(
            REQUIRED_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(): Task<android.location.Location> {
        return fusedLocationClient.lastLocation
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onSuccess: (android.location.Location?) -> Unit) {
        val priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                onSuccess(location)
            }
            .addOnFailureListener {
                onSuccess(null)
            }
    }
}
