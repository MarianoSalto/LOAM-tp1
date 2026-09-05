package com.example.primertpdeappmoviles.presentation.fragment

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

/**
 * FragmentA: Se encarga de la interfaz de usuario para las funciones de Linterna y Grabación de Audio.
 * Representa la capa de Presentación en Clean Architecture.
 */
class FragmentA : Fragment() {

    // Referencia al binding para acceder a los componentes de la vista de forma segura
    private var _binding: FragmentABinding? = null
    private val binding get() = _binding!!

    // =========================
    // SERVICIOS
    // =========================
    // Servicios encargados de la lógica de hardware (Linterna y Audio)
    private lateinit var flashlightService: FlashlightService
    private lateinit var audioService: AudioService

    /**
     * Infla el diseño del fragmento y configura el View Binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentABinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta después de que la vista ha sido creada. Aquí se inicializan los servicios y se configuran los listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Servicios pasando el contexto necesario
        flashlightService = FlashlightService(requireContext())
        audioService = AudioService(requireContext())

        // Configuración inicial de la linterna (verificar si el dispositivo tiene flash)
        initCamera()

        // El botón de finalizar audio comienza deshabilitado
        binding.btnFinishAudio.isEnabled = false

        // =========================
        // CICLO DE VIDA
        // =========================
        // Añadimos un observador al ciclo de vida para apagar la linterna si el fragmento se detiene
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                // Si la linterna está encendida al salir, la apagamos por seguridad
                if (flashlightService.isFlashOn) {
                    toggleFlashlight(false)
                }
            }
        })

        // ACCIONES DE LA UI
        
        // Listener para el botón de la linterna: alterna entre encendido y apagado
        binding.btnLinterna.setOnClickListener {
            toggleFlashlight(!flashlightService.isFlashOn)
        }

        // Listener para iniciar la grabación de audio
        binding.btnStartAudio.setOnClickListener {
            // Verificamos permisos antes de proceder
            if (audioService.hasAudioPermission()) {
                iniciarGrabacion()
            } else {
                // Si no hay permisos, se solicitan al usuario
                audioService.requestAudioPermissions(this)
            }
        }

        // Listener para detener la grabación de audio
        binding.btnFinishAudio.setOnClickListener {
            audioService.stopRecording()
            // Re-habilitamos el botón de inicio y deshabilitamos el de fin
            binding.btnStartAudio.isEnabled = true
            binding.btnFinishAudio.isEnabled = false
        }
    }

    /**
     * Verifica la disponibilidad del flash en el hardware.
     */
    private fun initCamera() {
        if (!flashlightService.hasFlash()) {
            Toast.makeText(requireContext(), "El dispositivo no cuenta con Flash", Toast.LENGTH_SHORT).show()
            binding.btnLinterna.isEnabled = false
        }
    }

    /**
     * Alterna el estado del flash y actualiza el icono del botón.
     */
    private fun toggleFlashlight(turnOn: Boolean) {
        if (flashlightService.toggleFlashlight(turnOn)) {
            // Actualiza el recurso del icono según el estado de la linterna
            binding.btnLinterna.setIconResource(flashlightService.getFlashIconResource())
        } else {
            Toast.makeText(requireContext(), "Error al cambiar el estado del flash", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Inicia el proceso de grabación y gestiona el estado de los botones.
     */
    private fun iniciarGrabacion() {
        audioService.startRecording()
        binding.btnStartAudio.isEnabled = false
        binding.btnFinishAudio.isEnabled = true
    }

    /**
     * Limpia la referencia al binding para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
