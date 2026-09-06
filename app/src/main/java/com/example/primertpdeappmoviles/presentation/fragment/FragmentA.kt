package com.example.primertpdeappmoviles.presentation.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.primertpdeappmoviles.data.datasouce.BatteryDataSource
import com.example.primertpdeappmoviles.data.repository.BatteryRepositoryImpl
import com.example.primertpdeappmoviles.databinding.FragmentABinding
import com.example.primertpdeappmoviles.domain.usecase.EstimateBatteryUseCase
import com.example.primertpdeappmoviles.domain.usecase.GetBatteryInfoUseCase
import com.example.primertpdeappmoviles.presentation.viewmodel.BatteryViewModel
import com.example.primertpdeappmoviles.presentation.viewmodel.BatteryViewModelFactory
import com.example.primertpdeappmoviles.services.AudioService
import com.example.primertpdeappmoviles.services.FlashlightService

/**
 * FragmentA: Se encarga de la interfaz de usuario para las funciones de Linterna, 
 * Grabación de Audio y Estado de Batería con estimación de tiempo.
 * Representa la capa de Presentación en Clean Architecture.
 */
class FragmentA : Fragment() {

    // Referencia al binding para acceder a los componentes de la vista de forma segura
    private var _binding: FragmentABinding? = null
    private val binding get() = _binding!!

    // =========================
    // SERVICIOS
    // =========================
    private lateinit var flashlightService: FlashlightService
    private lateinit var audioService: AudioService

    // ViewModel para la batería siguiendo Clean Architecture
    private val batteryViewModel: BatteryViewModel by viewModels {
        val dataSource = BatteryDataSource(requireContext())
        val repository = BatteryRepositoryImpl(dataSource)
        val getBatteryUseCase = GetBatteryInfoUseCase(repository)
        val estimateUseCase = EstimateBatteryUseCase()
        
        BatteryViewModelFactory(getBatteryUseCase, estimateUseCase)
    }

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
     * Se ejecuta después de que la vista ha sido creada. 
     * Aquí se inicializan los servicios, observadores y listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Servicios
        flashlightService = FlashlightService(requireContext())
        audioService = AudioService(requireContext())

        // Configurar observadores de LiveData
        setupObservers()

        // Configuración inicial de la linterna
        initCamera()

        // Estado inicial de botones
        binding.btnFinishAudio.isEnabled = false

        // =========================
        // CICLO DE VIDA
        // =========================
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                if (flashlightService.isFlashOn) {
                    toggleFlashlight(false)
                }
            }
        })

        // ACCIONES DE LA UI
        
        binding.btnLinterna.setOnClickListener {
            toggleFlashlight(!flashlightService.isFlashOn)
        }

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

        binding.btnBateria.setOnClickListener {
            batteryViewModel.refreshBatteryInfo()
        }
    }

    /**
     * Configura la observación de los datos expuestos por el ViewModel.
     */
    private fun setupObservers() {
        batteryViewModel.batteryInfo.observe(viewLifecycleOwner) { info ->

            Log.d("BATTERY_DEBUG", "percentage: ${info.percentage}")
            Log.d("BATTERY_DEBUG", "isCharging: ${info.isCharging}")
            Log.d("BATTERY_DEBUG", "estimatedMinutesRemaining: ${info.estimatedMinutesRemaining}")

            val chargingStatus = if (info.isCharging) "Cargando" else "Descargando"
            var message = "Batería: ${info.percentage}%\nEstado: $chargingStatus"
            
            // Si tenemos estimación de tiempo, la añadimos al mensaje
            info.estimatedMinutesRemaining?.let { minutes ->
                val endTime = batteryViewModel.getEstimatedEndTimeString(minutes)
                message += "\nQuedan aprox: $minutes min"
                message += "\nSe agotará a las: $endTime"
            } ?: run {
                if (!info.isCharging) {
                    message += "\nCalculando estimación..."
                }
            }
            
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
