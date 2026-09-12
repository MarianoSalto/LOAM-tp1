package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primertpdeappmoviles.domain.model.ClimaUiState
import com.example.primertpdeappmoviles.domain.usecase.GetClimaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClimaViewModel(private val getWeatherUseCase: GetClimaUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<ClimaUiState>(ClimaUiState.Loading)
    val uiState: StateFlow<ClimaUiState> = _uiState.asStateFlow()

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = ClimaUiState.Loading
            getWeatherUseCase(lat, lon)
                .onSuccess { weatherInfo ->
                    _uiState.value = ClimaUiState.Success(weatherInfo)
                }
                .onFailure { exception ->
                    _uiState.value = ClimaUiState.Error(exception.localizedMessage ?: "Error desconocido")
                }
        }
    }
}
