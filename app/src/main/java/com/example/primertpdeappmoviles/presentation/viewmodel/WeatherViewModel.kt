package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primertpdeappmoviles.domain.model.Alerta
import com.example.primertpdeappmoviles.domain.usecase.VerificarAlertaMeteorologicaUseCase

import kotlinx.coroutines.launch

class WeatherViewModel(//Clase del Funcionalidad de alertas metereologica, basada en el caso de uso
    private val verificarAlertaMeteorologicaUseCase: VerificarAlertaMeteorologicaUseCase
) : ViewModel() {

    private val _alertaActiva = MutableLiveData<Alerta>()
    val alertaActiva: LiveData<Alerta> = _alertaActiva

    fun comenzarMonitoreo() {
        // Abrimos el scope de corrutinas obligatorio para ejecutar funciones suspend
        viewModelScope.launch {
            verificarAlertaMeteorologicaUseCase.ejecutar { alerta ->
                _alertaActiva.postValue(alerta)
            }
        }
    }
}