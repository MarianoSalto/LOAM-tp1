package com.example.primertpdeappmoviles.domain.usecase

import com.example.primertpdeappmoviles.domain.model.Siniestro
import com.example.primertpdeappmoviles.domain.repository.SiniestroRepository

class GuardarSiniestroUseCase(
    private val repository: SiniestroRepository
) {

    suspend operator fun invoke(siniestro: Siniestro) {
        repository.guardarSiniestro(siniestro)
    }
}