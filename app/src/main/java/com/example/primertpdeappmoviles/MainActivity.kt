package com.example.primertpdeappmoviles

// Clases de la alerta meteorológica

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.primertpdeappmoviles.data.repository.LocationRepositoryImpl
import com.example.primertpdeappmoviles.data.repository.NotificationRepositoryImpl
import com.example.primertpdeappmoviles.data.repository.WeatherRepositoryImpl
import com.example.primertpdeappmoviles.databinding.ActivityMainBinding
import com.example.primertpdeappmoviles.domain.usecase.RegistrarTokenAlertaUseCase
import com.example.primertpdeappmoviles.domain.usecase.VerificarAlertaMeteorologicaUseCase
import com.example.primertpdeappmoviles.presentation.viewmodel.MainViewModel
import com.example.primertpdeappmoviles.presentation.viewmodel.WeatherViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.MapLibre


/**
 * MainActivity: Actividad principal que actúa como contenedor de los fragmentos a través de Navigation Component.
 */
class MainActivity : AppCompatActivity() {

    // View Binding para acceder a los elementos de layout de la actividad
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel
    private var vibrator: Vibrator? = null
    private var isVibrating = false // Bandera para que la alerta no se interrumpa sola

    private lateinit var mainViewModel: MainViewModel // Para el registro de alertas push

    companion object {
        private const val CHANNEL_ID = "canal_alertas_emergencia"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializar MapLibre para el uso de mapas en la aplicación
        MapLibre.getInstance(this)
        
        super.onCreate(savedInstanceState)
        
        // Configuración de View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilita el diseño de borde a borde
        enableEdgeToEdge()

        // Variables para las notificaciones (Registro de token)
        val notificationRepo = NotificationRepositoryImpl(Any())
        val registrarTokenUseCase = RegistrarTokenAlertaUseCase(notificationRepo)
        mainViewModel = MainViewModel(registrarTokenUseCase)

        chequearPermisosDeNotificacion()

        // Variables para la alerta meteorológica local
        val weatherRepo = WeatherRepositoryImpl(FirebaseFirestore.getInstance())
        val locationRepo = LocationRepositoryImpl(applicationContext)
        val useCase = VerificarAlertaMeteorologicaUseCase(weatherRepo, locationRepo)
        viewModel = WeatherViewModel(useCase)

        // Ajusta el padding del layout principal
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
        viewModel.alertaActiva.observe(this) { alerta ->
            // Solo iniciamos si NO está vibrando ya
            if (!isVibrating) {
                lanzarAlertaSistema("Alerta Meteorológica", alerta.mensaje)
                iniciarVibracion()
            }
        }

        binding.btnDetenerAlerta.setOnClickListener {
            detenerAlerta()
        }

        // =========================
        // NAVEGACIÓN
        // =========================
        
        binding.btnFragmentA.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.fragment_a)
        }

        binding.btnFragmentB.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.fragment_b, Bundle().apply {
                putString("sample", "Navigation Component")
            })
        }

        binding.btnFragmentC.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.fragment_c)
        }

        binding.btnFragmentD.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.fragmentD)
        }

        verificarPermisosYMonitorear()
    }

    /**
     * Muestra una notificación del sistema y activa la vibración.
     */
    private fun lanzarAlertaSistema(titulo: String, mensaje: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alertas de Emergencia", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun iniciarVibracion() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?
        if (vibrator != null && vibrator!!.hasVibrator()) {
            val pattern = longArrayOf(0, 500, 500)
            vibrator!!.vibrate(VibrationEffect.createWaveform(pattern, 0))
            binding.btnDetenerAlerta.visibility = View.VISIBLE
            isVibrating = true
        }
    }

    private fun detenerAlerta() {
        vibrator?.cancel()
        binding.btnDetenerAlerta.visibility = View.GONE
        isVibrating = false
    }

    private fun verificarPermisosYMonitorear() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.comenzarMonitoreo()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

    private fun chequearPermisosDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
                mainViewModel.recuperarYEnviarTokenActual()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        } else {
            mainViewModel.recuperarYEnviarTokenActual()
        }
    }
}
