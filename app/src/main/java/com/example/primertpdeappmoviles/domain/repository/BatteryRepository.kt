package com.example.primertpdeappmoviles.domain.repository

import com.example.primertpdeappmoviles.domain.model.BatteryInfo

/**
 * Interfaz del repositorio de batería.
 * Define el contrato que la capa de datos debe implementar.
 */
interface BatteryRepository {
    /**
     * Obtiene la información actual de la batería.
     */
    fun getBatteryInfo(): BatteryInfo
}
