package com.example.primertpdeappmoviles.domain.repository

import android.location.Location

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
    fun hasLocationPermission(): Boolean
}
