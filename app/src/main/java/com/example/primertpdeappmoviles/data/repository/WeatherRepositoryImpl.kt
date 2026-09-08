package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.domain.model.Alerta
import com.example.primertpdeappmoviles.domain.repository.WeatherRepository
import com.google.firebase.firestore.FirebaseFirestore

class WeatherRepositoryImpl(private val firestore: FirebaseFirestore) : WeatherRepository {
    override fun escucharAlertas(onAlertasCambio: (List<Alerta>) -> Unit) {
        // Escuchamos únicamente la colección "catastrofe"
        firestore.collection("catastrofe").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener


            val listaAlertas = snapshot.documents.mapNotNull { doc ->
                // 1. Obtener la fecha como Timestamp
                val timestamp = doc.getTimestamp("fecha")

                // 2. Convertir el Timestamp a Date y luego formatear a String
                val fechaString = timestamp?.toDate()?.let { date ->
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
                } ?: ""

                Alerta(
                    id = doc.id,
                    mensaje = doc.getString("mensaje") ?: "",
                    fecha = fechaString,
                    latitud = doc.getDouble("latitud") ?: 0.0,
                    longitud = doc.getDouble("longitud") ?: 0.0
                )
            }
            onAlertasCambio(listaAlertas)
        }
    }
}
