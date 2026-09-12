package com.example.primertpdeappmoviles.domain.model

sealed interface ClimaUiState {
    object Loading : ClimaUiState
    data class Success(val data: DatosClimas) : ClimaUiState
    data class Error(val message: String) : ClimaUiState
}