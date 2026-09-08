package com.example.primertpdeappmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.primertpdeappmoviles.databinding.FragmentABinding
import com.example.primertpdeappmoviles.services.AudioService
import com.example.primertpdeappmoviles.services.FlashlightService


class FragmentA : Fragment() {

    private var _binding: FragmentABinding? = null
    private val binding get() = _binding!!

    // =========================
    // SERVICIOS
    // =========================
    private lateinit var flashlightService: FlashlightService

    private lateinit var audioService: AudioService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentABinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Servicios
        flashlightService = FlashlightService(requireContext())

        audioService = AudioService(requireContext())

        // =========================
        // LINTERNA
        initCamera()

        //===========================
        //Audio
        binding.btnFinishAudio.isEnabled = false

        // =========================
        // CICLO DE VIDA
        // =========================
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                // Apagar linterna al salir del fragmento
                if (flashlightService.isFlashOn) {
                    toggleFlashlight(false)
                }
            }
        })

        // ACCIONES
        // =========================
        // BOTÓN LINTERNA
        // =========================
        binding.btnLinterna.setOnClickListener {
            toggleFlashlight(!flashlightService.isFlashOn)
        }


        //====Audio
        binding.btnStartAudio.setOnClickListener {
            if (audioService.hasAudioPermission()) {
                iniciarGrabacion()
            } else {
                audioService.requestAudioPermissions(this)
            }
        }

        binding.btnFinishAudio.setOnClickListener {
            audioService.stopRecording()
            binding.btnStartAudio.isEnabled = true
            binding.btnFinishAudio.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == AudioService.REQUEST_CODE_AUDIO) {
            if (audioService.hasAudioPermission()) {
                iniciarGrabacion()
            } else {
                Toast.makeText(requireContext(), "Permiso de audio denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initCamera() {
        if (!flashlightService.hasFlash()) {
            Toast.makeText(requireContext(), "El dispositivo no cuenta con Flash", Toast.LENGTH_SHORT).show()
            binding.btnLinterna.isEnabled = false
        }
    }

    private fun toggleFlashlight(turnOn: Boolean) {
        if (flashlightService.toggleFlashlight(turnOn)) {
            binding.btnLinterna.setIconResource(flashlightService.getFlashIconResource())
        } else {
            Toast.makeText(requireContext(), "Error al cambiar el estado del flash", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarGrabacion() {
        audioService.startRecording()
        binding.btnStartAudio.isEnabled = false
        binding.btnFinishAudio.isEnabled = true
    }



}
