package com.example.primertpdeappmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.primertpdeappmoviles.databinding.FragmentABinding
import com.example.primertpdeappmoviles.services.AudioService
import com.example.primertpdeappmoviles.services.DataBaseService
import com.example.primertpdeappmoviles.services.FlashlightService
import com.example.primertpdeappmoviles.services.LocationService
import com.google.android.material.button.MaterialButton
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
    private lateinit var dataBaseService: DataBaseService
    private lateinit var audioService: AudioService

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
        dataBaseService = DataBaseService()
        audioService = AudioService(requireContext())

        map = binding.mapView
        map.onCreate(savedInstanceState)

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

        // =========================
        // BOTÓN MAPA
        // =========================
        binding.btnMapa.setOnClickListener {
            irAMiUbicacion()
        }

        //==== Listener Guardar ubi
        binding.btnRegistrarSiniestro.setOnClickListener {
            registrarSiniestro()
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

    private fun solicitarPermisoUbicacion() {
        if (!locationService.hasLocationPermission()) {
            locationService.requestLocationPermissions(this)
        } else {
            irAMiUbicacion()
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
                irAMiUbicacion()
            }
        } else if (requestCode == AudioService.REQUEST_CODE_AUDIO) {
            if (audioService.hasAudioPermission()) {
                iniciarGrabacion()
            } else {
                Toast.makeText(requireContext(), "Permiso de audio denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irAMiUbicacion() {
        if (!locationService.hasLocationPermission()) {
            solicitarPermisoUbicacion()
            return
        }

        // Primero intentamos con la última ubicación conocida (rápido)
        locationService.getLastLocation()
            .addOnSuccessListener { location ->
                if (location != null) {
                    actualizarMapaConUbicacion(location)
                } else {
                    // Si es null (común en emuladores), pedimos una ubicación fresca
                    Toast.makeText(requireContext(), "Buscando ubicación exacta...", Toast.LENGTH_SHORT).show()
                    locationService.getCurrentLocation { freshLocation ->
                        if (freshLocation != null) {
                            actualizarMapaConUbicacion(freshLocation)
                        } else {
                            Toast.makeText(requireContext(), "No se pudo obtener la ubicación. Verifique el GPS.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarMapaConUbicacion(location: android.location.Location) {
        val miUbicacion = LatLng(location.latitude, location.longitude)
        map.getMapAsync { map ->
            map.cameraPosition = CameraPosition.Builder()
                .target(miUbicacion)
                .zoom(18.0)
                .build()

            map.clear() // Limpiar marcadores anteriores
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

        locationService.getLastLocation()
            .addOnSuccessListener { location ->
                if (location == null) {
                    // Reintento con ubicación fresca
                    locationService.getCurrentLocation { freshLocation ->
                        if (freshLocation != null) {
                            mostrarDialogoReferencia(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            Toast.makeText(requireContext(), "No se pudo obtener la ubicación para el registro", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@addOnSuccessListener
                }
                mostrarDialogoReferencia(location.latitude, location.longitude)
            }
    }

    private fun mostrarDialogoReferencia(latitud: Double, longitud: Double) {
        val input = android.widget.EditText(requireContext())
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
                guardarEnFirestore(latitud, longitud, referencia)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarEnFirestore(latitud: Double, longitud: Double, referencia: String) {
        dataBaseService.guardarSiniestro(latitud, longitud, referencia)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Ubicación registrada correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error al guardar: ${error.message}", Toast.LENGTH_LONG).show()
            }
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

    // =========================
    // MAPVIEW LIFECYCLE
    // =========================
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
