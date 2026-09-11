package com.example.primertpdeappmoviles.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primertpdeappmoviles.databinding.FragmentABinding
import com.example.primertpdeappmoviles.presentation.ChatAdapter
import com.example.primertpdeappmoviles.presentation.ChatViewModel
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

        //================Chat=================================
        // Configuramos el botón que muestra el chat.
        binding.btnAsistente.setOnClickListener {
            if (binding.layoutChat.visibility == View.GONE) {
                // Mostramos el chat.
                binding.layoutChat.visibility = View.VISIBLE
                // Ocultamos las herramientas.
                binding.layoutHerramientas.visibility = View.GONE
                // Cambiamos el texto del botón
                binding.btnAsistente.text = "Cerrar Chat"
            } else {
                // Ocultamos el chat.
                binding.layoutChat.visibility = View.GONE
                // Mostramos las herramientas.
                binding.layoutHerramientas.visibility = View.VISIBLE
                // Restauramos el texto del botón
                binding.btnAsistente.text = "Asistente"
            }
        }

        // Configuramos el botón para enviar.
        binding.btnEnviar.setOnClickListener {

            // Obtenemos el texto escrito.
            val texto =
                binding.editMensaje.text.toString()

            // Enviamos el texto al ViewModel.
            chatViewModel.enviarMensaje(texto)

            // Limpiamos el campo.
            binding.editMensaje.text.clear()
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
    // DESTRUCCIÓN DE LA VISTA
    // =================================================

    override fun onDestroyView() {

        // Eliminamos la referencia al binding.
        _binding = null

        // Ejecutamos el comportamiento normal.
        super.onDestroyView()
    }
}
