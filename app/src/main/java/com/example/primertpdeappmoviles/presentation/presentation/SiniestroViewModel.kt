package com.example.primertpdeappmoviles.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primertpdeappmoviles.domain.model.Siniestro
import com.example.primertpdeappmoviles.domain.usecase.GetLocationUseCase
import com.example.primertpdeappmoviles.domain.usecase.GuardarSiniestroUseCase
import kotlinx.coroutines.launch

/**
 * SiniestroViewModel: Gestiona el estado de la UI relacionado con la ubicación y el registro de siniestros.
 * En Clean Architecture, el ViewModel actúa como intermediario entre la Vista y los Casos de Uso.
 */
class SiniestroViewModel(
    private val guardarSiniestroUseCase: GuardarSiniestroUseCase, // Caso de uso para persistir un siniestro
    private val getLocationUseCase: GetLocationUseCase           // Caso de uso para obtener la ubicación GPS
) : ViewModel() {

    // LiveData privado para manejar la ubicación y su exposición pública de solo lectura
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    // LiveData para manejar el estado de carga (loading)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Solicita la ubicación actual del dispositivo de forma asíncrona.
     */
    fun fetchLocation() {
        _isLoading.value = true
        // Se utiliza viewModelScope para lanzar la corrutina ligada al ciclo de vida del ViewModel
        viewModelScope.launch {
            val result = getLocationUseCase() // Ejecuta el caso de uso
            _location.postValue(result)       // Actualiza el LiveData con el resultado
            _isLoading.postValue(false)
        }
    }

    /**
     * Crea un objeto Siniestro y lo envía al caso de uso para guardarlo.
     * @param latitud Latitud capturada.
     * @param longitud Longitud capturada.
     * @param referencia Descripción del siniestro.
     * @param onSuccess Callback ejecutado tras el guardado exitoso.
     * @param onError Callback ejecutado si ocurre un error.
     */
    fun guardarSiniestro(
        latitud: Double,
        longitud: Double,
        referencia: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Mapeo de parámetros a una entidad de dominio
                val siniestro = Siniestro(
                    latitud = latitud,
                    longitud = longitud,
                    referencia = referencia
                )
                // Ejecución del caso de uso de negocio
                guardarSiniestroUseCase(siniestro)
                onSuccess()
            } catch (e: Exception) {
                // Manejo de errores delegando la respuesta a la vista
                onError(e)
            }
        }
    }
}
