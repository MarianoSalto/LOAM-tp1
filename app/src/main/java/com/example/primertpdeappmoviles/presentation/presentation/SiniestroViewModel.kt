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

class SiniestroViewModel(
    private val guardarSiniestroUseCase: GuardarSiniestroUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = getLocationUseCase()
            _location.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun guardarSiniestro(
        latitud: Double,
        longitud: Double,
        referencia: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val siniestro = Siniestro(
                    latitud = latitud,
                    longitud = longitud,
                    referencia = referencia
                )
                guardarSiniestroUseCase(siniestro)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
