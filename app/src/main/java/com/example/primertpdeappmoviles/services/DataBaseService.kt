package com.example.primertpdeappmoviles.services

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class DataBaseService {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Guarda un siniestro en la colección "siniestros" de Firestore.
     * Retorna un Task para que la Actividad pueda reaccionar al éxito o error.
     */
    fun guardarSiniestro(
        latitud: Double,
        longitud: Double,
        referencia: String
    ): Task<DocumentReference> {
        
        val siniestro = hashMapOf(
            "latitud" to latitud,
            "longitud" to longitud,
            "referencia" to referencia,
            "fecha" to Date()
        )

        return db.collection("siniestros").add(siniestro)
    }
}
