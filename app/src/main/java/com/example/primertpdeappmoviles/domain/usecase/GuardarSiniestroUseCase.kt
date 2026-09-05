package com.example.primertpdeappmoviles.domain.usecase

import com.example.primertpdeappmoviles.domain.model.Siniestro
import com.example.primertpdeappmoviles.domain.repository.SiniestroRepository

/**
 * GuardarSiniestroUseCase: Caso de uso para registrar un siniestro.
 * Representa una regla de negocio en la capa de Dominio (Clean Architecture).
 * Es independiente de la implementación de datos (Firestore, Base de datos local, etc.).
 */
class GuardarSiniestroUseCase(
    private val repository: SiniestroRepository // Interfaz del repositorio definida en el dominio
) {

    /**
     * Operador invoke que permite llamar a esta clase como si fuera una función.
     * @param siniestro El objeto de dominio con los datos del siniestro.
     */
    suspend operator fun invoke(siniestro: Siniestro) {
        // Delega la persistencia al repositorio
        repository.guardarSiniestro(siniestro)
    }
}
