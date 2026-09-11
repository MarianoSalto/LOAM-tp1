package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.domain.model.Mensaje
import com.example.primertpdeappmoviles.domain.repository.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// Implementa las operaciones definidas por ChatRepository.
class ChatRepositoryImpl : ChatRepository {

    // Obtenemos una instancia de Cloud Firestore.
    private val db = FirebaseFirestore.getInstance()

    // Referencia a la colección donde se guardarán los mensajes.
    private val mensajesRef = db.collection("mensajes")

    // Función encargada de enviar un mensaje a Firestore.
    override suspend fun enviarMensaje(mensaje: Mensaje) {

        // Creamos un mapa con los datos que queremos guardar.
        val datos = hashMapOf(

            // Guardamos el texto del mensaje.
            "texto" to mensaje.texto,

            // Guardamos quién escribió el mensaje.
            "emisor" to mensaje.emisor,

            // Guardamos la fecha actual.
            "fecha" to Timestamp.now()
        )

        // Agregamos un nuevo documento dentro de "mensajes".
        mensajesRef
            .add(datos)
            .await()
    }

    // Escucha permanentemente los cambios realizados en Firestore.
    override fun escucharMensajes(
        onMensajes: (List<Mensaje>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        // Ordenamos los mensajes desde el más antiguo al más reciente.
        mensajesRef
            .orderBy("fecha", Query.Direction.ASCENDING)

            // Escuchamos cambios en tiempo real.
            .addSnapshotListener { snapshot, error ->

                // Si Firestore devuelve un error, lo informamos.
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                // Si no recibimos información, no hacemos nada.
                if (snapshot == null) {
                    return@addSnapshotListener
                }

                // Convertimos los documentos de Firebase en objetos Mensaje.
                val mensajes = snapshot.documents.map { documento ->

                    // Obtenemos el texto guardado.
                    val texto =
                        documento.getString("texto") ?: ""

                    // Obtenemos el emisor.
                    val emisor =
                        documento.getString("emisor") ?: ""

                    // Obtenemos la fecha.
                    val fecha =
                        documento.getTimestamp("fecha")
                            ?.toDate()
                            ?.time ?: 0L

                    // Creamos nuestro objeto de dominio.
                    Mensaje(
                        texto = texto,
                        emisor = emisor,
                        fecha = fecha
                    )
                }

                // Enviamos la lista de mensajes a la capa superior.
                onMensajes(mensajes)
            }
    }
}
