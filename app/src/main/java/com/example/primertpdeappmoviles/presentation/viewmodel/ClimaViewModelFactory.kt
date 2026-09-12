package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.primertpdeappmoviles.domain.usecase.GetClimaUseCase

class ClimaViewModelFactory(
    private val getClimaUseCase: GetClimaUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClimaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClimaViewModel(getClimaUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
