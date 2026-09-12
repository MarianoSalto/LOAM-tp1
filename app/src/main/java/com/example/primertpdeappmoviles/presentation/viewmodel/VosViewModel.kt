package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.primertpdeappmoviles.domain.usecase.ProcesoComandoVosUsecase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * VosViewModel: Gestiona el estado del asistente de voz y 
 * traduce los resultados del caso de uso a estados de la UI.
 */
class VosViewModel(
    private val procesoComandoVosUsecase: ProcesoComandoVosUsecase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val uiState: StateFlow<VoiceUiState> = _uiState

    fun onVoiceTextReceived(text: String) {
        // Ejecutamos el caso de uso para interpretar la voz
        when (procesoComandoVosUsecase(text)) {
            is ProcesoComandoVosUsecase.VoiceAction.FlashlightOn -> {
                _uiState.value = VoiceUiState.ActionFlashlight(true)
            }

            is ProcesoComandoVosUsecase.VoiceAction.FlashlightOff -> {
                _uiState.value = VoiceUiState.ActionFlashlight(false)
            }

            ProcesoComandoVosUsecase.VoiceAction.Unknown -> {
                _uiState.value = VoiceUiState.Error("No entendí el comando: $text")
            }
        }
    }

    // Reinicia el estado después de ejecutar la acción
    fun resetState() {
        _uiState.value = VoiceUiState.Idle
    }

    sealed class VoiceUiState {
        object Idle : VoiceUiState()
        data class ActionFlashlight(val turnOn: Boolean) : VoiceUiState()
        data class Error(val errorMessage: String) : VoiceUiState()
    }
}
