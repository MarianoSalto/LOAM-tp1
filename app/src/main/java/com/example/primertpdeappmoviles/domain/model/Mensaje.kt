package com.example.primertpdeappmoviles.domain.model

// Representa un mensaje dentro de nuestra aplicación.
data class Mensaje( // Texto escrito por el usuario o por el asistente.
    val texto: String, // Indica quién escribió el mensaje. // Puede ser "usuario" o "asistente".
    val emisor: String, // Fecha del mensaje expresada en milisegundos.
    val fecha: Long)

