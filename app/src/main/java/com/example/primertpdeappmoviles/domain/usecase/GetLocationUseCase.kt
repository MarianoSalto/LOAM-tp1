package com.example.primertpdeappmoviles.domain.usecase

import android.location.Location
import com.example.primertpdeappmoviles.domain.repository.LocationRepository

/**
 * GetLocationUseCase: Caso de uso para obtener la ubicación actual.
 * Capa de Dominio: Contiene la lógica necesaria para interactuar con los datos de ubicación desde el punto de vista del negocio.
 */
class GetLocationUseCase(
    private val repository: LocationRepository // Dependencia de la interfaz del repositorio
) {
    /**
     * Obtiene la ubicación actual del repositorio de forma asíncrona.
     * @return Un objeto Location o null si no se puede determinar.
     */
    suspend operator fun invoke(): Location? {
        return repository.getCurrentLocation()
    }
}
