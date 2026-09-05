package com.example.primertpdeappmoviles

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.primertpdeappmoviles.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import org.maplibre.android.MapLibre

/**
 * MainActivity: Actividad principal que actúa como contenedor de los fragmentos a través de Navigation Component.
 */
class MainActivity : AppCompatActivity() {

    // View Binding para acceder a los elementos de layout de la actividad
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializar MapLibre para el uso de mapas en la aplicación
        MapLibre.getInstance(this)
        
        super.onCreate(savedInstanceState)
        
        // Configuración de View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilita el diseño de borde a borde para aprovechar toda la pantalla
        enableEdgeToEdge()

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
    }
}
