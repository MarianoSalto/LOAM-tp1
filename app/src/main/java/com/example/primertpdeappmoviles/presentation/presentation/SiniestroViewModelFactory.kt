package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.primertpdeappmoviles.domain.usecase.GetLocationUseCase
import com.example.primertpdeappmoviles.domain.usecase.GuardarSiniestroUseCase

/**
 * SiniestroViewModelFactory: Fábrica para instanciar el ViewModel con sus dependencias inyectadas.
 * Necesario porque el SiniestroViewModel requiere parámetros en su constructor que el sistema de ViewModel por defecto no conoce.
 */
class SiniestroViewModelFactory(
    private val guardarSiniestroUseCase: GuardarSiniestroUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModelProvider.Factory {

    /**
     * Crea una instancia del ViewModel solicitado si es del tipo correcto.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificamos si la clase solicitada es SiniestroViewModel
        if (modelClass.isAssignableFrom(SiniestroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SiniestroViewModel(
                guardarSiniestroUseCase,
                getLocationUseCase
            ) as T
        }
        // Lanzamos una excepción si el Factory no sabe cómo crear el ViewModel solicitado
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
