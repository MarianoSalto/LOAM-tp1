package com.example.primertpdeappmoviles.domain.repository

import android.location.Location

interface LocationRepository {//definimos la iterfaces asociada a nuestra ubicación
    suspend fun getCurrentLocation(): Location?//donde estoy
    fun hasLocationPermission(): Boolean//Permisos de accesos
}
