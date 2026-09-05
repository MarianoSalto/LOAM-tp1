package com.example.primertpdeappmoviles.presentation.fragment

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.primertpdeappmoviles.data.repository.LocationRepositoryImpl
import com.example.primertpdeappmoviles.data.repository.SiniestroRepositoryImpl
import com.example.primertpdeappmoviles.databinding.FragmentABinding
import com.example.primertpdeappmoviles.domain.usecase.GetLocationUseCase
import com.example.primertpdeappmoviles.domain.usecase.GuardarSiniestroUseCase
import com.example.primertpdeappmoviles.presentation.viewmodel.SiniestroViewModel
import com.example.primertpdeappmoviles.presentation.viewmodel.SiniestroViewModelFactory
import com.example.primertpdeappmoviles.services.AudioService
import com.example.primertpdeappmoviles.services.FlashlightService
import com.example.primertpdeappmoviles.services.LocationService
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

class FragmentA : Fragment() {

    private var _binding: FragmentABinding? = null
    private val binding get() = _binding!!

    // =========================
    // SERVICIOS
    // =========================
    private lateinit var flashlightService: FlashlightService
    private lateinit var locationService: LocationService
    private lateinit var audioService: AudioService

    private val siniestroViewModel: SiniestroViewModel by viewModels {
        val siniestroRepo = SiniestroRepositoryImpl()
        val locationRepo = LocationRepositoryImpl(requireContext())
        SiniestroViewModelFactory(
            GuardarSiniestroUseCase(siniestroRepo),
            GetLocationUseCase(locationRepo)
        )
    }

    private lateinit var map: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapLibre.getInstance(requireContext())
        _binding = FragmentABinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Servicios
        flashlightService = FlashlightService(requireContext())
        locationService = LocationService(requireContext())
        audioService = AudioService(requireContext())

        map = binding.mapView
        map.onCreate(savedInstanceState)

        setupObservers()

        // =========================
        // UBICACIÓN
        solicitarPermisoUbicacion()

        // =========================
        // MAPA
        initViewMap()

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
                if (flashlightService.isFlashOn) {
                    toggleFlashlight(false)
                }
            }
        })

        // ACCIONES
        binding.btnLinterna.setOnClickListener {
            toggleFlashlight(!flashlightService.isFlashOn)
        }

        binding.btnMapa.setOnClickListener {
            siniestroViewModel.fetchLocation()
        }

        binding.btnRegistrarSiniestro.setOnClickListener {
            registrarSiniestro()
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
    }

    private fun setupObservers() {
        siniestroViewModel.location.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                actualizarMapaConUbicacion(location)
            } else if (locationService.hasLocationPermission()) {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación. Verifique el GPS.", Toast.LENGTH_LONG).show()
            }
        }

        siniestroViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Toast.makeText(requireContext(), "Buscando ubicación exacta...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun solicitarPermisoUbicacion() {
        if (!locationService.hasLocationPermission()) {
            locationService.requestLocationPermissions(this)
        } else {
            siniestroViewModel.fetchLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LocationService.LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationService.hasLocationPermission()) {
                siniestroViewModel.fetchLocation()
            }
        } else if (requestCode == AudioService.REQUEST_CODE_AUDIO) {
            if (audioService.hasAudioPermission()) {
                iniciarGrabacion()
            } else {
                Toast.makeText(requireContext(), "Permiso de audio denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarMapaConUbicacion(location: Location) {
        val miUbicacion = LatLng(location.latitude, location.longitude)
        map.getMapAsync { map ->
            map.cameraPosition = CameraPosition.Builder()
                .target(miUbicacion)
                .zoom(18.0)
                .build()

            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(miUbicacion)
                    .title("Mi ubicación")
            )
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

    private fun registrarSiniestro() {
        if (!locationService.hasLocationPermission()) {
            solicitarPermisoUbicacion()
            return
        }

        val location = siniestroViewModel.location.value
        if (location != null) {
            mostrarDialogoReferencia(location.latitude, location.longitude)
        } else {
            Toast.makeText(requireContext(), "Buscando ubicación para el registro...", Toast.LENGTH_SHORT).show()
            siniestroViewModel.fetchLocation()
            // Podríamos observar un estado una vez para disparar el diálogo
        }
    }

    private fun mostrarDialogoReferencia(latitud: Double, longitud: Double) {
        val input = EditText(requireContext())
        input.hint = "Ingrese una referencia"

        AlertDialog.Builder(requireContext())
            .setTitle("Registrar siniestro")
            .setMessage("Ingrese una referencia del lugar")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val referencia = input.text.toString().trim()
                if (referencia.isEmpty()) {
                    Toast.makeText(requireContext(), "Debe ingresar una referencia", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                guardarSiniestro(latitud, longitud, referencia)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarSiniestro(
        latitud: Double,
        longitud: Double,
        referencia: String
    ) {
        siniestroViewModel.guardarSiniestro(
            latitud = latitud,
            longitud = longitud,
            referencia = referencia,
            onSuccess = {
                Toast.makeText(requireContext(), "Ubicación registrada correctamente", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error al guardar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun iniciarGrabacion() {
        audioService.startRecording()
        binding.btnStartAudio.isEnabled = false
        binding.btnFinishAudio.isEnabled = true
    }

    private fun initViewMap() {
        map.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/liberty")
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(-34.6037, -58.3816))
                .zoom(12.0)
                .build()
        }
    }

    override fun onStart() {
        super.onStart()
        map.onStart()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onStop() {
        super.onStop()
        map.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::map.isInitialized) {
            map.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.onDestroy()
        _binding = null
    }
}
