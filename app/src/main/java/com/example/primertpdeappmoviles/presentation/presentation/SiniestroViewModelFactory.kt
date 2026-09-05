package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.primertpdeappmoviles.domain.usecase.GetLocationUseCase
import com.example.primertpdeappmoviles.domain.usecase.GuardarSiniestroUseCase

class SiniestroViewModelFactory(
    private val guardarSiniestroUseCase: GuardarSiniestroUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SiniestroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SiniestroViewModel(
                guardarSiniestroUseCase,
                getLocationUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
