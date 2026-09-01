package com.example.primertpdeappmoviles.services

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.example.primertpdeappmoviles.R

class FlashlightService(context: Context) {

    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    var isFlashOn: Boolean = false
        private set

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasFlash(): Boolean = cameraId != null

    fun toggleFlashlight(turnOn: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            cameraManager.setTorchMode(id, turnOn)
            isFlashOn = turnOn
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getFlashIconResource(): Int {
        return if (isFlashOn) R.drawable.linterna_encendida else R.drawable.linterna_apagada
    }
}
