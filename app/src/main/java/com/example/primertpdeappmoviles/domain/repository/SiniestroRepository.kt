package com.example.primertpdeappmoviles.domain.repository

import com.example.primertpdeappmoviles.domain.model.Siniestro

interface SiniestroRepository {

    suspend fun guardarSiniestro(
        siniestro: Siniestro
    )
}