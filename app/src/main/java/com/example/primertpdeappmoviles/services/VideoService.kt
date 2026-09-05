package com.example.primertpdeappmoviles.services

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Servicio para manejar la funcionalidad de la cámara usando CameraX.
 * Recibe el contexto, el LifecycleOwner y el PreviewView para interactuar con la UI y el ciclo de vida.
 */
class VideoService(
    private val context: android.content.Context,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    private val previewView: PreviewView
) {
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    companion object {
        private const val TAG = "VideoService"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    /**
     * Verifica si todos los permisos necesarios han sido otorgados.
     */
    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita los permisos necesarios para la cámara.
     */
    /*fun solicitarPermisos(activity: android.app.Activity) {
        ActivityCompat.requestPermissions(
            activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }*/

    /**
     * Configura e inicia la vista previa de la cámara.
     */
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Se usa para vincular el ciclo de vida de las cámaras al ciclo de vida del LifecycleOwner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configurar el Use Case de Preview (Vista previa)
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Configurar el Use Case de ImageCapture (Tomar foto)
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Seleccionar la cámara actual (trasera o delantera)
            val cameraSelector = this.cameraSelector

            try {
                // Desvincular cualquier uso previo antes de volver a vincular
                cameraProvider.unbindAll()

                // Vincular los casos de uso a la cámara
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Error al vincular la cámara", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Captura una imagen y la guarda en la galería.
     */
    fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Log.e(TAG, "La cámara aún no está lista")
            Toast.makeText(context, "Cámara no lista, intente de nuevo", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear nombre del archivo basado en el tiempo actual
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Configurar metadatos para guardar en MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Crear opciones de salida que contienen el archivo + metadatos
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Configurar el listener de captura de imagen
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Error al capturar foto: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Foto guardada: ${output.savedUri}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    /**
     * Limpia los recursos cuando el servicio ya no es necesario.
     */
    fun shutdown() {
        cameraExecutor.shutdown()
    }

    fun getCameraSelector(): CameraSelector {
        return cameraSelector
    }

    fun setCameraSelector(selector: CameraSelector) {
        cameraSelector = selector
    }

    /**
     * Alterna entre la cámara frontal y trasera.
     */
    fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
}
