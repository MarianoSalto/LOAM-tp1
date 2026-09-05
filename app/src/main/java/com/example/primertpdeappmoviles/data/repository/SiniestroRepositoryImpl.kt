package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.domain.model.Siniestro
import com.example.primertpdeappmoviles.domain.repository.SiniestroRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * SiniestroRepositoryImpl: Implementación del repositorio de Siniestros.
 * Capa de Datos (Clean Architecture): Aquí se define CÓMO se guardan los datos (en este caso, Firebase Firestore).
 */
class SiniestroRepositoryImpl : SiniestroRepository {
    
    // Instancia de Firestore para interactuar con la base de datos de Google
    private val db = FirebaseFirestore.getInstance()

    /**
     * Guarda un siniestro en la colección "siniestros" de Firestore.
     * @param siniestro Entidad de dominio a persistir.
     */
    override suspend fun guardarSiniestro(
        siniestro: Siniestro
    ) {
        // Se crea un mapa de datos para Firebase a partir de la entidad
        db.collection("siniestros")
            .add(
                hashMapOf(
                    "latitud" to siniestro.latitud,
                    "longitud" to siniestro.longitud,
                    "referencia" to siniestro.referencia
                )
            )
            .await() // Se espera a que la operación de red termine de forma suspendida
    }
}
