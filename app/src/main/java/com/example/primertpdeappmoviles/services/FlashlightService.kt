package com.example.primertpdeappmoviles.services

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.example.primertpdeappmoviles.R

/**
 * FlashlightService: Encargado de controlar el hardware del flash de la cámara para usarlo como linterna.
 */
class FlashlightService(context: Context) {

    // Gestor del sistema para las cámaras del dispositivo
    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    // ID de la cámara que posee el flash
    private var cameraId: String? = null
    
    // Propiedad para saber si el flash está actualmente encendido
    var isFlashOn: Boolean = false
        private set

    init {
        // Al inicializar, buscamos en la lista de cámaras cuál tiene soporte para Flash
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Indica si el dispositivo posee hardware de flash disponible.
     */
    fun hasFlash(): Boolean = cameraId != null

    /**
     * Enciende o apaga el flash.
     * @param turnOn true para encender, false para apagar.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    fun toggleFlashlight(turnOn: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            // Activa el modo linterna en el ID de cámara detectado
            cameraManager.setTorchMode(id, turnOn)
            isFlashOn = turnOn
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retorna el recurso gráfico (icono) correspondiente al estado actual del flash.
     */
    fun getFlashIconResource(): Int {
        return if (isFlashOn) R.drawable.linterna_encendida else R.drawable.linterna_apagada
    }
}
