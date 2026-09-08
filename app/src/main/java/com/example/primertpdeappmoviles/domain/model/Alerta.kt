package com.example.primertpdeappmoviles.domain.model

data class Alerta(
    val id: String,
    val mensaje: String,
    val fecha: String,
    val latitud: Double,
    val longitud: Double
)