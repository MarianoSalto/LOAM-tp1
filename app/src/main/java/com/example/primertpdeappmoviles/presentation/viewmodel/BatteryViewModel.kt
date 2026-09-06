package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primertpdeappmoviles.domain.model.BatteryInfo
import com.example.primertpdeappmoviles.domain.usecase.EstimateBatteryUseCase
import com.example.primertpdeappmoviles.domain.usecase.GetBatteryInfoUseCase
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel para gestionar el estado de la batería.
 */
class BatteryViewModel(
    private val getBatteryInfoUseCase: GetBatteryInfoUseCase,
    private val estimateBatteryUseCase: EstimateBatteryUseCase
) : ViewModel() {

    private val _batteryInfo = MutableLiveData<BatteryInfo>()
    val batteryInfo: LiveData<BatteryInfo> = _batteryInfo

    // Fallback: Datos para el cálculo manual si el hardware no provee datos
    private var initialPercentage: Float? = null
    private var startTimeMillis: Long = System.currentTimeMillis()

    /**
     * Actualiza la información de la batería.
     * Prioriza la estimación por hardware; si falla, usa el cálculo manual.
     */
    fun refreshBatteryInfo() {
        val currentInfo = getBatteryInfoUseCase()
        
        // Si el hardware ya nos dio una estimación (gracias a los Amperios), la usamos
        if (currentInfo.estimatedMinutesRemaining != null) {
            _batteryInfo.value = currentInfo
            return
        }

        // --- LÓGICA DE FALLBACK (Manual) ---
        if (initialPercentage == null) {
            initialPercentage = currentInfo.percentage
            startTimeMillis = System.currentTimeMillis()
        }

        val elapsedMinutes = (System.currentTimeMillis() - startTimeMillis) / (1000f * 60f)
        
        val manualMinutes = if (!currentInfo.isCharging) {
            estimateBatteryUseCase(
                initialPercentage!!,
                currentInfo.percentage,
                elapsedMinutes
            )
        } else{// le añadi el mismo código de arriba para que se ejecute el tiempo..... enteoria deberia ir null dado que esta cargando
            estimateBatteryUseCase(
                initialPercentage!!,
                currentInfo.percentage,
                elapsedMinutes
            )
        }

        _batteryInfo.value = currentInfo.copy(
            estimatedMinutesRemaining = manualMinutes
        )
    }

    /**
     * Calcula la hora estimada en la que se agotará la batería.
     */
    fun getEstimatedEndTimeString(minutesRemaining: Long): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutesRemaining.toInt())
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }
}
