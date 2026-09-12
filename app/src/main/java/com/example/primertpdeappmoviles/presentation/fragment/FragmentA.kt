package com.example.primertpdeappmoviles.presentation.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primertpdeappmoviles.R
import com.example.primertpdeappmoviles.data.datasouce.BatteryDataSource
import com.example.primertpdeappmoviles.data.remote.RetrofitClient
import com.example.primertpdeappmoviles.data.repository.BatteryRepositoryImpl
import com.example.primertpdeappmoviles.data.repository.ClimaRepositoryImpl
import com.example.primertpdeappmoviles.databinding.FragmentABinding
import com.example.primertpdeappmoviles.domain.model.ClimaUiState
import com.example.primertpdeappmoviles.domain.usecase.*
import com.example.primertpdeappmoviles.presentation.ChatAdapter
import com.example.primertpdeappmoviles.presentation.ChatViewModel
import com.example.primertpdeappmoviles.presentation.viewmodel.*
import com.example.primertpdeappmoviles.services.AudioService
import com.example.primertpdeappmoviles.services.FlashlightService
import kotlinx.coroutines.launch


class FragmentA : Fragment() {

    // =================================================
    // BINDING
    // =================================================

    // Variable que contiene el binding del XML.
    private var _binding: FragmentABinding? = null

    // Permite acceder al binding sin escribir !! continuamente.
    private val binding get() = _binding!!


    // =================================================
    // SERVICIOS EXISTENTES
    // =================================================

    // Servicio encargado de controlar la linterna.
    private lateinit var flashlightService: FlashlightService

    // Servicio encargado de grabar audio.
    private lateinit var audioService: AudioService


    // =================================================
    // CHAT
    // =================================================

    // ViewModel encargado de manejar los mensajes.
    private val chatViewModel: ChatViewModel by viewModels()

    // Adapter encargado de mostrar los mensajes.
    private lateinit var chatAdapter: ChatAdapter

    // ViewModel para la batería (Clean Architecture)
    private val batteryViewModel: BatteryViewModel by viewModels {
        val dataSource = BatteryDataSource(requireContext())
        val repository = BatteryRepositoryImpl(dataSource)
        val getBatteryUseCase = GetBatteryInfoUseCase(repository)
        val estimateUseCase = EstimateBatteryUseCase()
        
        BatteryViewModelFactory(getBatteryUseCase, estimateUseCase)
    }

    // ViewModel para el Clima (Clean Architecture)
    private val climaViewModel: ClimaViewModel by viewModels {
        val apiService = RetrofitClient.weatherApiService
        val repository = ClimaRepositoryImpl(apiService)
        ClimaViewModelFactory(GetClimaUseCase(repository))
    }

    ///CÓDIGO PARA EL COMANDO DE VOS
    // Inyección de dependencia manual o con Hilt/Koin
    private val vosViewModel = VosViewModel(ProcesoComandoVosUsecase())


    // =================================================
    // RECONOCIMIENTO DE VOZ (LAUCHER)
    // =================================================
    private val voiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoEscuchado = matches?.firstOrNull()

            if (!textoEscuchado.isNullOrEmpty()) {
                // Procesamos el comando para ejecutar acciones de hardware (Linterna)
                // Se desvincula del chat para que no aparezca como mensaje
                vosViewModel.onVoiceTextReceived(textoEscuchado)
            }
        } else {
            Toast.makeText(requireContext(), "No se reconoció ninguna voz", Toast.LENGTH_SHORT)
                .show()
        }
    }



    // =================================================
    // CREACIÓN DE LA VISTA
    // =================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflamos el XML de FragmentA.
        _binding = FragmentABinding.inflate(
            inflater,
            container,
            false
        )


        // Devolvemos la vista principal.
        return binding.root
    }


    // =================================================
    // INICIALIZACIÓN
    // =================================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        // Ejecutamos primero el comportamiento normal del Fragment.
        super.onViewCreated(view, savedInstanceState)


        // =================================================
        // INICIALIZAR SERVICIOS
        // =================================================

        // Creamos el servicio de la linterna.
        flashlightService =
            FlashlightService(requireContext())

        // Creamos el servicio de audio.
        audioService =
            AudioService(requireContext())


        // =================================================
        // LINTERNA
        // =================================================

        // Comprobamos si el dispositivo tiene flash.
        initCamera()


        // =================================================
        // AUDIO
        // =================================================

        // Al comenzar la pantalla,
        // el botón para finalizar audio queda desactivado.
        binding.btnFinishAudio.isEnabled = false


        // =================================================
        // CHAT
        // =================================================

        // Configuramos el RecyclerView.
        configurarChat()

        // Comenzamos a observar los mensajes.
        observarMensajes()

        // Observamos la información de la batería
        observarBateria()

        // Observamos el clima
        observarClima()

        // Observamos los comandos de voz
        observarVoz()



        // =================================================
        // CICLO DE VIDA
        // =================================================

        // Observamos el ciclo de vida del Fragment.
        viewLifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {

                override fun onStop(owner: LifecycleOwner) {

                    // Ejecutamos el comportamiento normal.
                    super.onStop(owner)

                    // Si la linterna está encendida...
                    if (flashlightService.isFlashOn) {

                        // La apagamos.
                        toggleFlashlight(false)
                    }
                }
            }
        )


        // =================================================
        // BOTÓN LINTERNA
        // =================================================

        binding.btnLinterna.setOnClickListener {

            // Cambiamos el estado de la linterna.
            toggleFlashlight(
                !flashlightService.isFlashOn
            )
        }

        // Mostramos la guía inicial
        setupVideo()

        //================Chat=================================
        // Configuramos el botón que muestra el chat.
        binding.btnAsistente.setOnClickListener {
            if (binding.layoutChat.visibility == View.GONE) {
                // Mostramos el chat.
                binding.layoutChat.visibility = View.VISIBLE
                // Ocultamos todo el contenido superior (Herramientas, Guía, Video) usando el ScrollView
                binding.scrollViewContenido.visibility = View.GONE
                // Cambiamos el texto del botón
                binding.btnAsistente.text = "Cerrar Chat"
            } else {
                // Ocultamos el chat.
                binding.layoutChat.visibility = View.GONE
                // Mostramos nuevamente el contenido superior
                binding.scrollViewContenido.visibility = View.VISIBLE
                // Restauramos el texto del botón
                binding.btnAsistente.text = "Asistente"
            }
        }

        // Configuramos el botón para enviar del chat principal (Manual)
        binding.btnEnviar.setOnClickListener {
            val texto = binding.editMensaje.text.toString()
            if (texto.isNotBlank()) {
                chatViewModel.enviarMensaje(texto)
                binding.editMensaje.text.clear()
            }
        }

        // NUEVO: Botón para activar el comando de voz (barra superior junto a batería)
        binding.btnVoz.setOnClickListener {
            lanzarDictadoPorVoz()
        }

        // =================================================
        // BOTÓN INICIAR AUDIO
        // =================================================

        binding.btnStartAudio.setOnClickListener {

            // Comprobamos si tenemos permiso para grabar.
            if (audioService.hasAudioPermission()) {

                // Comenzamos la grabación.
                iniciarGrabacion()

            } else {

                // Solicitamos los permisos.
                audioService.requestAudioPermissions(this)
            }
        }


        // =================================================
        // BOTÓN FINALIZAR AUDIO
        // =================================================

        binding.btnFinishAudio.setOnClickListener {

            // Detenemos la grabación.
            audioService.stopRecording()

            // Habilitamos nuevamente iniciar.
            binding.btnStartAudio.isEnabled = true

            // Deshabilitamos parar.
            binding.btnFinishAudio.isEnabled = false
        }

        // =================================================
        // BOTÓN BATERÍA
        // =================================================

        binding.btnBateria.setOnClickListener {
            // Solicita la actualización de info de batería al ViewModel
            batteryViewModel.refreshBatteryInfo()
        }

        // =================================================
        // CLIC EN IMAGEN GUÍA (AGRANDAR)
        // =================================================

        binding.ivGuiaAyuda.setOnClickListener {
            mostrarImagenAgrandada()
        }

        // Lógica para agrandar el video al hacer clic
        binding.videoClickOverlay.setOnClickListener {
            mostrarVideoAgrandado()
        }

        // Cargar clima inicial (Ej: General Pico)
        climaViewModel.loadWeather(-35.6596, -63.7568)
    }


    // =================================================
    // CONFIGURAR CHAT
    // =================================================

    private fun configurarChat() {

        // Creamos el adapter.
        chatAdapter = ChatAdapter()

        // Indicamos que los mensajes se muestran
        // uno debajo del otro.
        binding.recyclerMensajes.layoutManager =
            LinearLayoutManager(requireContext())

        // Asignamos el adapter al RecyclerView.
        binding.recyclerMensajes.adapter =
            chatAdapter
    }


    // =================================================
    // OBSERVAR MENSAJES
    // =================================================

    private fun observarMensajes() {

        // Lanzamos una corrutina asociada al ciclo de vida.
        viewLifecycleOwner.lifecycleScope.launch {

            // Observamos permanentemente el StateFlow.
            chatViewModel.mensajes.collect { mensajes ->

                // Actualizamos el adapter.
                chatAdapter.actualizarMensajes(mensajes)

                // Si hay mensajes...
                if (mensajes.isNotEmpty()) {

                    // Movemos el RecyclerView
                    // hasta el último mensaje.
                    binding.recyclerMensajes.scrollToPosition(
                        mensajes.size - 1
                    )
                }
            }
        }


        // También observamos posibles errores.
        viewLifecycleOwner.lifecycleScope.launch {

            // Observamos el StateFlow de errores.
            chatViewModel.error.collect { error ->

                // Si existe un error...
                if (error != null) {

                    // Mostramos el error.
                    Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_LONG
                    ).show()

                    // Limpiamos el error.
                    chatViewModel.limpiarError()
                }
            }
        }
    }


    // =================================================
    // PERMISOS DE AUDIO
    // =================================================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        // Ejecutamos el comportamiento normal.
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        // Comprobamos si el permiso corresponde al audio.
        if (
            requestCode ==
            AudioService.REQUEST_CODE_AUDIO
        ) {

            // Si tenemos permiso...
            if (audioService.hasAudioPermission()) {

                // Comenzamos la grabación.
                iniciarGrabacion()

            } else {

                // Informamos que fue rechazado.
                Toast.makeText(
                    requireContext(),
                    "Permiso de audio denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    // =================================================
    // CÁMARA / LINTERNA
    // =================================================
    // CONFIGURAR VIDEO
    // =================================================

    private fun setupVideo() {
        val videoPath = "android.resource://" + requireContext().packageName + "/" + R.raw.guia_emergencia_animada
        val uri = Uri.parse(videoPath)
        binding.videoAyuda.setVideoURI(uri)

        // Controles de reproducción
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.videoAyuda)
        binding.videoAyuda.setMediaController(mediaController)

        // Opcional: Iniciar automáticamente o al hacer clic
        binding.videoAyuda.setOnPreparedListener { mp ->
            mp.isLooping = true // El video se repetirá
        }
    }

    // =================================================
    // OBSERVAR CLIMA
    // =================================================

    private fun observarClima() {
        viewLifecycleOwner.lifecycleScope.launch {
            climaViewModel.uiState.collect { state ->
                when (state) {
                    is ClimaUiState.Loading -> {
                        binding.pbClima.visibility = View.VISIBLE
                    }
                    is ClimaUiState.Error -> {
                        binding.pbClima.visibility = View.GONE
                        binding.tvCondicion.text = "Error de clima"
                    }
                    is ClimaUiState.Success -> {
                        binding.pbClima.visibility = View.GONE
                        val data = state.data
                        binding.tvIconoClima.text = data.conditionIcon
                        binding.tvTemperatura.text = data.temperature
                        binding.tvCondicion.text = data.conditionText
                        binding.tvHumedadViento.text = "💧 Humedad: ${data.humidity} | 💨 Viento: ${data.windSpeed}"
                    }
                }
            }
        }
    }


    // =================================================
    // OBSERVAR BATERÍA
    // =================================================

    private fun observarBateria() {
        batteryViewModel.batteryInfo.observe(viewLifecycleOwner) { info ->
            var message = "Batería: ${info.percentage}%"

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


    // =================================================

    private fun initCamera() {

        // Comprobamos si el dispositivo tiene flash.
        if (!flashlightService.hasFlash()) {

            // Informamos al usuario.
            Toast.makeText(
                requireContext(),
                "El dispositivo no cuenta con Flash",
                Toast.LENGTH_SHORT
            ).show()

            // Deshabilitamos el botón.
            binding.btnLinterna.isEnabled = false
        }
    }


    // =================================================
    // CAMBIAR LINTERNA
    // =================================================

    private fun toggleFlashlight(turnOn: Boolean) {

        // Intentamos cambiar el estado del flash.
        if (
            flashlightService.toggleFlashlight(turnOn)
        ) {

            // Cambiamos el icono del botón.
            binding.btnLinterna.setIconResource(
                flashlightService.getFlashIconResource()
            )

        } else {

            // Informamos si ocurrió un error.
            Toast.makeText(
                requireContext(),
                "Error al cambiar el estado del flash",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =================================================
    // GRABACIÓN DE AUDIO
    // =================================================

    private fun iniciarGrabacion() {

        // Comenzamos a grabar.
        audioService.startRecording()

        // Deshabilitamos iniciar.
        binding.btnStartAudio.isEnabled = false

        // Habilitamos parar.
        binding.btnFinishAudio.isEnabled = true
    }


    // =================================================
    // AGRANDAR IMAGEN GUÍA
    // =================================================

    private fun mostrarImagenAgrandada() {
        val builder = AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_image_preview, null)
        builder.setView(vistaDialogo)

        val dialog = builder.create()

        // Botón cerrar dentro del diálogo
        vistaDialogo.findViewById<View>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // =================================================
    // AGRANDAR VIDEO GUÍA
    // =================================================

    private fun mostrarVideoAgrandado() {
        val builder = AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_video_preview, null)
        builder.setView(vistaDialogo)

        val dialog = builder.create()

        val videoView = vistaDialogo.findViewById<VideoView>(R.id.vvFullVideo)
        val videoPath = "android.resource://" + requireContext().packageName + "/" + R.raw.guia_emergencia_animada
        videoView.setVideoURI(Uri.parse(videoPath))

        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoView.start()
        }

        vistaDialogo.findViewById<View>(R.id.btnCloseVideo).setOnClickListener {
            videoView.stopPlayback()
            dialog.dismiss()
        }

        dialog.show()
    }


    // =================================================
    // DESTRUCCIÓN DE LA VISTA
    // =================================================

    override fun onDestroyView() {

        // Eliminamos la referencia al binding.
        _binding = null

        // Ejecutamos el comportamiento normal.
        super.onDestroyView()
    }

    //=============Funciones para el comando de vos=========
    // =================================================
// RECONOCIMIENTO DE VOZ (MÉTODO)
// =================================================
    private fun lanzarDictadoPorVoz() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Te escucho... ¿Qué querés decir?")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tu dispositivo no soporta reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    // =================================================
    // OBSERVAR COMANDOS DE VOZ
    // =================================================
    private fun observarVoz() {
        viewLifecycleOwner.lifecycleScope.launch {
            vosViewModel.uiState.collect { state ->
                when (state) {
                    is VosViewModel.VoiceUiState.ActionFlashlight -> {
                        // Ejecutamos la acción de la linterna
                        toggleFlashlight(state.turnOn)
                        // Reiniciamos el estado para no repetir la acción
                        vosViewModel.resetState()
                    }
                    is VosViewModel.VoiceUiState.Error -> {
                        Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
                        vosViewModel.resetState()
                    }
                    VosViewModel.VoiceUiState.Idle -> { /* No hacer nada */ }
                }
            }
        }
    }
}
