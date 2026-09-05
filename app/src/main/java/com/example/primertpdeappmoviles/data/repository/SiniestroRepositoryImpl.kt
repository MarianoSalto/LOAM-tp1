package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.domain.model.Siniestro
import com.example.primertpdeappmoviles.domain.repository.SiniestroRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class SiniestroRepositoryImpl : SiniestroRepository {
    private val db = FirebaseFirestore.getInstance()

    override suspend fun guardarSiniestro(
        siniestro: Siniestro
    ) {
        db.collection("siniestros")
            .add(
                hashMapOf(
                    "latitud" to siniestro.latitud,
                    "longitud" to siniestro.longitud,
                    "referencia" to siniestro.referencia
                )
            )
            .await()
    }
}
