package com.example.primertpdeappmoviles.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * VideoService: Gestiona la cámara del dispositivo utilizando la biblioteca CameraX.
 * Se encarga de la vista previa y la grabación de video.
 */
class VideoService(
    private val context: android.content.Context,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    private val previewView: PreviewView
) {
    // Caso de uso para capturar fotos
    private var imageCapture: ImageCapture? = null
    // Caso de uso para grabar video
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    
    // Ejecutor para tareas de cámara en segundo plano
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    // Selector de cámara (trasera por defecto)
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    companion object {
        private const val TAG = "VideoService"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    /**
     * Comprueba si se han concedido los permisos necesarios.
     */
    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Configura e inicia la cámara vinculada al ciclo de vida.
     */
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Vista previa
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Configurar ImageCapture
            imageCapture = ImageCapture.Builder()
                .build()

            // Configurar Recorder para VideoCapture
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Error al vincular la cámara", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Captura una foto y la guarda en el almacenamiento externo.
     */
    fun takePhoto() {
        // Obtener una referencia estable del caso de uso de captura de imágenes
        val imageCapture = imageCapture ?: return

        // Crear nombre del archivo con marca de tiempo
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Crear opciones de salida para MediaStore
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
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
                    val msg = "Foto capturada: ${output.savedUri}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    /**
     * Inicia o detiene la grabación de video.
     * @param onRecordingStatus callback para informar si está grabando o no.
     */
    @SuppressLint("MissingPermission")
    fun toggleRecording(onRecordingStatus: (Boolean) -> Unit) {
        val videoCapture = this.videoCapture ?: return

        val curRecording = recording
        if (curRecording != null) {
            // Detener la grabación actual
            curRecording.stop()
            recording = null
            onRecordingStatus(false)
            return
        }

        // Preparar nombre y ubicación del archivo
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        // Iniciar nueva grabación
        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        onRecordingStatus(true)
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video guardado: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            recording?.stop()
                            recording = null
                            Log.e(TAG, "Error en grabación: ${recordEvent.error}")
                        }
                        onRecordingStatus(false)
                    }
                }
            }
    }

    fun isRecording(): Boolean = recording != null

    /**
     * Alterna entre cámara frontal y trasera.
     */
    fun toggleCamera() {
        if (isRecording()) return // No cambiar mientras graba
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun shutdown() {
        recording?.stop()
        recording = null
        cameraExecutor.shutdown()
    }
}
