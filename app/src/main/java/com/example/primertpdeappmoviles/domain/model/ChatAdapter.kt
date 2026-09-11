package com.example.primertpdeappmoviles.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.primertpdeappmoviles.R
import com.example.primertpdeappmoviles.domain.model.Mensaje

// Adapter encargado de mostrar los mensajes en el RecyclerView.
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MensajeViewHolder>() {

    // Lista de mensajes que se mostrarán.
    private var mensajes = listOf<Mensaje>()

    // Actualiza la lista de mensajes.
    fun actualizarMensajes(nuevosMensajes: List<Mensaje>) {

        // Reemplazamos la lista anterior.
        mensajes = nuevosMensajes

        // Avisamos al RecyclerView que los datos cambiaron.
        notifyDataSetChanged()
    }

    // Indica cuántos elementos tiene nuestra lista.
    override fun getItemCount(): Int {

        // Devolvemos la cantidad de mensajes.
        return mensajes.size
    }

    // Crea la vista visual de un mensaje.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MensajeViewHolder {

        // Inflamos el XML correspondiente al mensaje.
        val vista = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_mensaje,
                parent,
                false
            )

        // Devolvemos el ViewHolder.
        return MensajeViewHolder(vista)
    }

    // Coloca la información de un mensaje en pantalla.
    override fun onBindViewHolder(
        holder: MensajeViewHolder,
        position: Int
    ) {

        // Obtenemos el mensaje correspondiente.
        val mensaje = mensajes[position]

        // Mostramos el texto.
        holder.txtMensaje.text = mensaje.texto

        // Si el mensaje pertenece al usuario...
        if (mensaje.emisor == "usuario") {

            // Lo mostramos a la derecha.
            holder.txtMensaje.textAlignment =
                View.TEXT_ALIGNMENT_TEXT_END

        } else {

            // Si pertenece al asistente, lo mostramos a la izquierda.
            holder.txtMensaje.textAlignment =
                View.TEXT_ALIGNMENT_TEXT_START
        }
    }

    // Representa cada mensaje individual.
    class MensajeViewHolder(
        vista: View
    ) : RecyclerView.ViewHolder(vista) {

        // Buscamos el TextView definido en item_mensaje.xml.
        val txtMensaje: TextView =
            vista.findViewById(R.id.txtMensaje)
    }
}
