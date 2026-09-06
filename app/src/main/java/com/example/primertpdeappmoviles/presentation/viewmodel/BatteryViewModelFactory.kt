package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.primertpdeappmoviles.domain.usecase.EstimateBatteryUseCase
import com.example.primertpdeappmoviles.domain.usecase.GetBatteryInfoUseCase

/**
 * Factory para crear instancias de BatteryViewModel con sus dependencias.
 */
class BatteryViewModelFactory(
    private val getBatteryInfoUseCase: GetBatteryInfoUseCase,
    private val estimateBatteryUseCase: EstimateBatteryUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatteryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BatteryViewModel(
                getBatteryInfoUseCase,
                estimateBatteryUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
