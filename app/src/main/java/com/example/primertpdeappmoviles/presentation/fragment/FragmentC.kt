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
import com.example.primertpdeappmoviles.data.repository.LocationRepositoryImpl
import com.example.primertpdeappmoviles.data.repository.SiniestroRepositoryImpl
import com.example.primertpdeappmoviles.databinding.FragmentCBinding
import com.example.primertpdeappmoviles.domain.usecase.GetLocationUseCase
import com.example.primertpdeappmoviles.domain.usecase.GuardarSiniestroUseCase
import com.example.primertpdeappmoviles.presentation.viewmodel.SiniestroViewModel
import com.example.primertpdeappmoviles.presentation.viewmodel.SiniestroViewModelFactory
import com.example.primertpdeappmoviles.services.LocationService
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import kotlin.getValue

/**
 * Fragmento encargado de la geolocalización y registro de siniestros.
 */
class FragmentC : Fragment() {
    // Variable privada para el ViewBinding (evita fugas de memoria)
    private var _binding: FragmentCBinding? = null
    // Propiedad de acceso seguro al binding
    private val binding get() = _binding!!

    // Servicio para manejar permisos y utilidades de ubicación
    private lateinit var locationService: LocationService

    // Inyección de dependencias para el ViewModel usando un Factory
    private val siniestroViewModel: SiniestroViewModel by viewModels {
        val siniestroRepo = SiniestroRepositoryImpl() // Implementación del repositorio de Firestore
        val locationRepo = LocationRepositoryImpl(requireContext()) // Implementación del repositorio de GPS
        SiniestroViewModelFactory(
            GuardarSiniestroUseCase(siniestroRepo), // Caso de uso para guardar en la BD
            GetLocationUseCase(locationRepo)        // Caso de uso para obtener coordenadas
        )
    }

    // Componente visual del mapa
    private lateinit var map: MapView

    /**
     * Se llama para que el fragmento infle su diseño de interfaz de usuario.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializa el motor de mapas MapLibre
        MapLibre.getInstance(requireContext())
        // Infla el layout usando ViewBinding
        _binding = FragmentCBinding.inflate(inflater, container, false)
        // Retorna la raíz de la vista inflada
        return binding.root
    }

    /**
     * Se llama inmediatamente después de que onCreateView ha retornado.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el servicio de ubicación local
        locationService = LocationService(requireContext())

        // Configura el MapView y su ciclo de vida inicial
        map = binding.mapView
        map.onCreate(savedInstanceState)

        // Configura los observadores de LiveData del ViewModel
        setupObservers()

        // Verifica y solicita permisos al iniciar el fragmento
        solicitarPermisoUbicacion()

        // Configura el estilo y posición inicial del mapa
        initViewMap()

        // Configura la acción del botón de geolocalización (actualiza posición)
        binding.btnMapa.setOnClickListener {
            siniestroViewModel.fetchLocation()
        }

        // Configura la acción del botón de registro
        binding.btnRegistrarSiniestro.setOnClickListener {
            registrarSiniestro()
        }
    }

    /**
     * Observa los cambios en los datos del ViewModel.
     */
    private fun setupObservers() {
        // Observa la ubicación actual; si cambia, actualiza el mapa
        siniestroViewModel.location.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                actualizarMapaConUbicacion(location)
            } else if (locationService.hasLocationPermission()) {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación. Verifique el GPS.", Toast.LENGTH_LONG).show()
            }
        }

        // Observa el estado de carga para mostrar mensajes al usuario
        siniestroViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Toast.makeText(requireContext(), "Buscando ubicación exacta...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Verifica si hay permisos y si no, los solicita.
     */
    private fun solicitarPermisoUbicacion() {
        if (!locationService.hasLocationPermission()) {
            locationService.requestLocationPermissions(this)
        } else {
            siniestroViewModel.fetchLocation() // Si ya hay permisos, busca la ubicación
        }
    }

    /**
     * Recibe el resultado de la solicitud de permisos.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Verifica si los permisos de ubicación fueron aceptados
        if (requestCode == LocationService.LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationService.hasLocationPermission()) {
                siniestroViewModel.fetchLocation()
            }
        }
    }

    /**
     * Centra el mapa y coloca un marcador en las coordenadas dadas.
     */
    private fun actualizarMapaConUbicacion(location: Location) {
        val miUbicacion = LatLng(location.latitude, location.longitude)
        map.getMapAsync { map ->
            // Mueve la cámara a la ubicación
            map.cameraPosition = CameraPosition.Builder()
                .target(miUbicacion)
                .zoom(18.0)
                .build()

            map.clear() // Borra marcadores anteriores
            // Añade el marcador de "Mi ubicación"
            map.addMarker(
                MarkerOptions()
                    .position(miUbicacion)
                    .title("Mi ubicación")
            )
        }
    }

    /**
     * Lógica para iniciar el flujo de registro de siniestro.
     */
    private fun registrarSiniestro() {
        if (!locationService.hasLocationPermission()) {
            solicitarPermisoUbicacion()
            return
        }

        // Si ya tenemos la ubicación en el ViewModel, mostramos el diálogo
        val location = siniestroViewModel.location.value
        if (location != null) {
            mostrarDialogoReferencia(location.latitude, location.longitude)
        } else {
            // Si no, la solicitamos primero
            Toast.makeText(requireContext(), "Buscando ubicación para el registro...", Toast.LENGTH_SHORT).show()
            siniestroViewModel.fetchLocation()
        }
    }

    /**
     * Muestra un cuadro de diálogo para que el usuario ingrese una referencia.
     */
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
                // Si hay texto, guarda en la base de datos
                guardarSiniestro(latitud, longitud, referencia)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Llama al ViewModel para persistir los datos en Firestore.
     */
    private fun guardarSiniestro(latitud: Double, longitud: Double, referencia: String) {
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

    /**
     * Configuración estética inicial del mapa.
     */
    private fun initViewMap() {
        map.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/liberty") // Estilo del mapa
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(-34.6037, -58.3816)) // Buenos Aires por defecto
                .zoom(12.0)
                .build()
        }
    }

    // --- MÉTODOS OBLIGATORIOS DEL CICLO DE VIDA PARA EL MAPA ---
    // El motor de mapas nativo necesita saber el estado de la pantalla para renderizar.

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
        map.onDestroy() // Destruye el mapa para liberar memoria
        _binding = null // Limpia el binding
    }
}
