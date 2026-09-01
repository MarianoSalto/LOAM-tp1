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
 * Servicio para manejar la grabación de audio.
 */
class AudioService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""

    companion object {
        const val REQUEST_CODE_AUDIO = 200
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Verifica si se tiene el permiso de grabación de audio.
     */
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita el permiso de grabación de audio.
     */
    fun requestAudioPermissions(activity: android.app.Activity) {
        androidx.core.app.ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_AUDIO
        )
    }

    fun requestAudioPermissions(fragment: androidx.fragment.app.Fragment) {
        fragment.requestPermissions(
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_AUDIO
        )
    }

    /**
     * Inicia la grabación de audio.
     */
    fun startRecording() {
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(musicDir, "AUDIO_$timeStamp.3gp")
        outputFile = audioFile.absolutePath
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
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
     * Detiene la grabación de audio.
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
