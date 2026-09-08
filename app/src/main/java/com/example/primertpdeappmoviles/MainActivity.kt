package com.example.primertpdeappmoviles

//Clases de la alerta meterelogica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.primertpdeappmoviles.data.repository.LocationRepositoryImpl
import com.example.primertpdeappmoviles.data.repository.WeatherRepositoryImpl
import com.example.primertpdeappmoviles.databinding.ActivityMainBinding
import com.example.primertpdeappmoviles.domain.usecase.VerificarAlertaMeteorologicaUseCase
import com.example.primertpdeappmoviles.presentation.viewmodel.WeatherViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.MapLibre


/**
 * MainActivity: Actividad principal que actúa como contenedor de los fragmentos a través de Navigation Component.
 */
class MainActivity : AppCompatActivity() {

    // View Binding para acceder a los elementos de layout de la actividad
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel// implemento las alertas en el main, dado que no importa en que fragmento  este, debe reaccionar en cualquier lado

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializar MapLibre para el uso de mapas en la aplicación
        MapLibre.getInstance(this)
        
        super.onCreate(savedInstanceState)
        
        // Configuración de View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilita el diseño de borde a borde para aprovechar toda la pantalla
        enableEdgeToEdge()

        //Varaibles paras la alerta
        val weatherRepo = WeatherRepositoryImpl(FirebaseFirestore.getInstance())
        val locationRepo = LocationRepositoryImpl(applicationContext)
        val useCase = VerificarAlertaMeteorologicaUseCase(weatherRepo, locationRepo)
        viewModel = WeatherViewModel(useCase)

        // Ajusta el padding del layout principal para que no se superponga con las barras del sistema (estado, navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Observar la alerta capturada desde el dominio
        viewModel.alertaActiva.observe(this) { alerta ->// Acá debo implemtar la funcionalidad de que el haga ruido
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?// esto hace vibrar al telefono.. se debe agregar unos permisos al AndroidManifest
            if (vibrator != null && vibrator.hasVibrator()) {
                // Vibra continuamente: espera 0ms, vibra 500ms, descansa 500ms...
                val pattern = longArrayOf(0, 500, 500)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)) // '0' repite el bucle
            }
            
        }

        // =========================
        // NAVEGACIÓN
        // =========================
        
        // Listener para navegar al FragmentA (Linterna y Audio)
        binding.btnFragmentA.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.fragment_a)
        }

        // Listener para navegar al FragmentB (Cámara)
        binding.btnFragmentB.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            // Ejemplo de paso de argumentos mediante Bundle
            navController.navigate(R.id.fragment_b, Bundle().apply {
                putString("sample", "Navigation Component")
            })
        }

        // Listener para navegar al FragmentC (Mapa y Registro de Siniestros)
        binding.btnFragmentC.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.fragment_c)
        }

        // Listener para navegar al FragmentD (Emergencias)
        binding.btnFragmentD.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.fragmentD)
        }

        verificarPermisosYMonitorear()
    }

    private fun verificarPermisosYMonitorear() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.comenzarMonitoreo()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }


}
