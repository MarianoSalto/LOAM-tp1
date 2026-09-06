package com.example.primertpdeappmoviles.domain.usecase

class EstimateBatteryUseCase {

    operator fun invoke(
        initialPercentage: Float,
        currentPercentage: Float,
        elapsedMinutes: Float
    ): Long? {

        // No se puede calcular si no pasó tiempo
        if (elapsedMinutes <= 0) {
            return null
        }

        // Cuánta batería se consumió
        val batteryConsumed =
            initialPercentage - currentPercentage

        // Si no consumió batería, no podemos estimar
        if (batteryConsumed <= 0) {

            return null
        }

        // Consumo de batería por minuto
        val consumptionPerMinute =
            batteryConsumed / elapsedMinutes

        // Minutos aproximados hasta llegar al 0%
        val estimatedMinutes =
            currentPercentage / consumptionPerMinute

        return estimatedMinutes.toLong()
    }
}