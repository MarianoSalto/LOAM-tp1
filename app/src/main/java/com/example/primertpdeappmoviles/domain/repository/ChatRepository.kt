package com.example.primertpdeappmoviles.domain.repository

import com.example.primertpdeappmoviles.domain.model.Mensaje

// Define las operaciones que puede realizar nuestro chat.
interface ChatRepository {

    // Permite enviar un mensaje.
    suspend fun enviarMensaje(mensaje: Mensaje)

    // Permite escuchar los mensajes de Firebase en tiempo real.
    fun escucharMensajes(
        onMensajes: (List<Mensaje>) -> Unit,
        onError: (Exception) -> Unit
    )
}
