package com.example.primertpdeappmoviles.data.datasouce

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.primertpdeappmoviles.domain.model.BatteryInfo
import kotlin.math.abs

/**
 * Fuente de datos para la batería.
 * Usa BatteryManager para obtener datos precisos de hardware.
 */
class BatteryDataSource(private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun getBatteryInfo(): BatteryInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)

        // Porcentaje básico
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level != -1 && scale != -1) level * 100 / scale.toFloat() else 0f

        // Estado de carga
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

        // --- CÁLCULO INSTANTÁNEO ---
        // Obtenemos la carga restante en microamperios-hora (uAh)
        val remainingCharge = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        // Obtenemos la corriente actual en microamperios (uA)
        val currentNow = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        var estimatedMinutes: Long? = null

        // Si no está cargando y tenemos flujo de corriente (negativo indica descarga)
        if (!isCharging && currentNow < 0) {
            // Tiempo (h) = Capacidad (uAh) / Corriente (uA)
            val hours = remainingCharge.toDouble() / abs(currentNow.toDouble())
            estimatedMinutes = (hours * 60).toLong()
        }

        return BatteryInfo(
            percentage = percentage,
            isCharging = isCharging,
            estimatedMinutesRemaining = estimatedMinutes
        )
    }
}
