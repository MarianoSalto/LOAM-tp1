package com.example.primertpdeappmoviles

import android.Manifest//Se va a utilizar para dar permiso al acceso a la camara
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
import com.example.primertpdeappmoviles.services.VideoService//importe mi servicio video, se encuentra la implentación de la camara

class FragmentB : Fragment() {

    private var _binding: FragmentBBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoServicio: VideoService

    private val requestCameraPermission =//Acá se va a solicitar el permiso para acceder a la camara
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                videoServicio.startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de cámara denegado",
                    Toast.LENGTH_SHORT
                ).show()
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

        // Inicializar VideoService con contexto, lifecycleOwner y el PreviewView del binding
        videoServicio = VideoService(requireContext(), viewLifecycleOwner, binding.viewFinder)

        // Intentar iniciar la cámara al abrir el fragmento si ya tiene permisos
        if (videoServicio.allPermissionsGranted()) {
            videoServicio.startCamera()
        }

        // Agregar observador al ciclo de vida del fragmento
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                videoServicio.shutdown()
            }
        })

        binding.captureVideo.setOnClickListener {
            if (videoServicio.allPermissionsGranted()) {
                videoServicio.takePhoto()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.modoSelfie.setOnClickListener {
            if (videoServicio.allPermissionsGranted()) {
                toggleCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
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
        Toast.makeText(requireContext(), "anda", Toast.LENGTH_SHORT).show()

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == VideoService.REQUEST_CODE_PERMISSIONS) {
            if (videoServicio.allPermissionsGranted()) {
                videoServicio.startCamera()
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleCamera() {
        videoServicio.toggleCamera()
        videoServicio.startCamera()
    }


}
