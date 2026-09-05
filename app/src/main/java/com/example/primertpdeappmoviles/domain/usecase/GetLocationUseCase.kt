package com.example.primertpdeappmoviles.domain.usecase

import android.location.Location
import com.example.primertpdeappmoviles.domain.repository.LocationRepository

class GetLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(): Location? {
        return repository.getCurrentLocation()
    }
}
