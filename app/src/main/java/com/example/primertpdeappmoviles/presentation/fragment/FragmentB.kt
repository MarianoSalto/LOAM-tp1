package com.example.primertpdeappmoviles.presentation.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.primertpdeappmoviles.databinding.FragmentBBinding
import com.example.primertpdeappmoviles.services.VideoService

/**
 * FragmentB: Encargado de la funcionalidad de grabación de video.
 */
class FragmentB : Fragment() {

    private var _binding: FragmentBBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoServicio: VideoService

    // Solicitar múltiples permisos (Cámara y Audio)
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                videoServicio.startCamera()
            } else {
                Toast.makeText(requireContext(), "Permisos necesarios denegados", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoServicio = VideoService(requireContext(), viewLifecycleOwner, binding.viewFinder)

        if (videoServicio.allPermissionsGranted()) {
            videoServicio.startCamera()
        } else {
            requestPermissions.launch(VideoService.REQUIRED_PERMISSIONS)
        }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                videoServicio.shutdown()
            }
        })

        // Cambiar lógica de clic para grabar
        binding.captureVideo.setOnClickListener {
            if (videoServicio.allPermissionsGranted()) {
                videoServicio.toggleRecording { isRecording ->
                    // Actualizar UI según si está grabando o no
                    binding.captureVideo.text = if (isRecording) "Detener" else "Grabar video"
                    binding.modoSelfie.isEnabled = !isRecording
                }
            } else {
                requestPermissions.launch(VideoService.REQUIRED_PERMISSIONS)
            }
        }

        binding.modoSelfie.setOnClickListener {
            if (videoServicio.allPermissionsGranted()) {
                toggleCamera()
            } else {
                requestPermissions.launch(VideoService.REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun toggleCamera() {
        videoServicio.toggleCamera()
        videoServicio.startCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
