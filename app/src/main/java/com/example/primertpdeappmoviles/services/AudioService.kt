package com.example.primertpdeappmoviles.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AudioService: Encapsula la lógica para la grabación de audio en el dispositivo.
 */
class AudioService(private val context: Context) {
    
    // El grabador multimedia de Android
    private var mediaRecorder: MediaRecorder? = null
    // Ruta del archivo donde se guardará el audio
    private var outputFile: String = ""

    companion object {
        const val REQUEST_CODE_AUDIO = 200
        // Permiso requerido para grabar audio
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Verifica si la aplicación tiene el permiso necesario para usar el micrófono.
     */
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita permisos de audio desde una Activity.
     */
    fun requestAudioPermissions(activity: android.app.Activity) {
        androidx.core.app.ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_AUDIO
        )
    }

    /**
     * Solicita permisos de audio desde un Fragment.
     */
    fun requestAudioPermissions(fragment: androidx.fragment.app.Fragment) {
        fragment.requestPermissions(
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_AUDIO
        )
    }

    /**
     * Configura el grabador e inicia la captura de sonido.
     */
    fun startRecording() {
        // Definir la ruta de salida en la carpeta de Música del almacenamiento externo
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(musicDir, "AUDIO_$timeStamp.3gp")
        outputFile = audioFile.absolutePath
        
        // Instanciar MediaRecorder según la versión de Android
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)        // Usar micrófono
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Formato 3GP
            setOutputFile(outputFile)                            // Ruta de destino
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)   // Codificador de audio
            try {
                prepare()
                start()
                Toast.makeText(context, "Grabando...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Detiene la grabación y libera los recursos del MediaRecorder.
     */
    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
        Toast.makeText(context, "Grabación guardada en: $outputFile", Toast.LENGTH_LONG).show()
    }
}
