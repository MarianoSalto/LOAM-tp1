package com.example.primertpdeappmoviles.domain.model

data class BatteryInfo(
    val percentage: Float,
    val estimatedMinutesRemaining: Long? = null,
    val isCharging: Boolean = false
)