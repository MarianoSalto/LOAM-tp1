package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.primertpdeappmoviles.domain.usecase.RegistrarTokenAlertaUseCase

/**
 * Factory para crear instancias de MainViewModel inyectando el UseCase correcto.
 */
class MainViewModelFactory(
    private val registrarTokenAlertaUseCase: RegistrarTokenAlertaUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(registrarTokenAlertaUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
