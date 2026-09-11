package com.example.primertpdeappmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primertpdeappmoviles.data.repository.ChatRepositoryImpl
import com.example.primertpdeappmoviles.domain.model.Mensaje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel encargado de controlar el estado del chat.
class ChatViewModel : ViewModel() {

    // Creamos el Repository que se comunicará con Firebase.
    private val repository = ChatRepositoryImpl()

    // Lista privada que contiene los mensajes.
    private val _mensajes =
        MutableStateFlow<List<Mensaje>>(emptyList())

    // Lista pública que puede observar la interfaz.
    val mensajes: StateFlow<List<Mensaje>> = _mensajes

    // Contiene un posible error ocurrido al comunicarnos con Firebase.
    private val _error =
        MutableStateFlow<String?>(null)

    // Error disponible para la interfaz.
    val error: StateFlow<String?> = _error

    // Al crear el ViewModel comenzamos a escuchar Firebase.
    init {
        escucharMensajes()
    }

    // Escucha los mensajes en tiempo real.
    private fun escucharMensajes() {

        // Le pedimos al Repository que observe Firestore.
        repository.escucharMensajes(

            // Se ejecuta cada vez que cambian los mensajes.
            onMensajes = { nuevosMensajes ->

                // Actualizamos la lista.
                _mensajes.value = nuevosMensajes
            },

            // Se ejecuta si ocurre un error.
            onError = { exception ->

                // Guardamos el mensaje de error.
                _error.value =
                    exception.message ?: "Error al obtener mensajes"
            }
        )
    }

    // Envía un mensaje escrito por el usuario.
    fun enviarMensaje(texto: String) {

        // Evitamos enviar mensajes vacíos.
        if (texto.isBlank()) {
            return
        }

        // Ejecutamos la operación de forma asíncrona.
        viewModelScope.launch {

            try {

                // Creamos el mensaje del usuario.
                val mensaje = Mensaje(
                    texto = texto,
                    emisor = "usuario",
                    fecha = System.currentTimeMillis()
                )

                // Enviamos el mensaje al Repository.
                repository.enviarMensaje(mensaje)

            } catch (exception: Exception) {

                // Guardamos el error para mostrarlo en pantalla.
                _error.value =
                    exception.message ?: "Error al enviar mensaje"
            }
        }
    }

    // Permite limpiar el error después de mostrarlo.
    fun limpiarError() {

        // Quitamos el mensaje de error.
        _error.value = null
    }
}
