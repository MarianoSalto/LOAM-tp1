package com.example.primertpdeappmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.primertpdeappmoviles.databinding.FragmentBBinding
import com.example.primertpdeappmoviles.services.VideoService

class FragmentB : Fragment() {

    private var _binding: FragmentBBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoServicio: VideoService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar VideoService con contexto, lifecycleOwner y el PreviewView del binding
        videoServicio = VideoService(requireContext(), viewLifecycleOwner, binding.viewFinder)

        // Agregar observador al ciclo de vida del fragmento
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                videoServicio.shutdown()
            }
        })

        binding.captureVideo.setOnClickListener {
            // Inicializar Cámara si hay permisos
            if (videoServicio.allPermissionsGranted()) {
                videoServicio.startCamera()
            } else {
                videoServicio.solicitarPermisos(requireActivity())
            }
            videoServicio.takePhoto()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Va a verificar si se acpto o no el permiso
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == VideoService.REQUEST_CODE_PERMISSIONS) {
            if (videoServicio.allPermissionsGranted()) {
                videoServicio.startCamera()
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
