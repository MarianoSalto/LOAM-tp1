package com.example.primertpdeappmoviles.presentation.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.primertpdeappmoviles.databinding.FragmentDBinding

/**
 * FragmentD: Fragmento encargado de mostrar opciones para realizar llamadas de emergencia.
 * Parte de la capa de Presentación.
 */
class FragmentD : Fragment() {

    // View Binding para acceso seguro a las vistas
    private var _binding: FragmentDBinding? = null
    private val binding get() = _binding!!

    /**
     * Infla el diseño XML correspondiente.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los botones de llamada una vez creada la vista.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Definir los números de contacto para cada botón (en este caso, todos usan 113 como ejemplo)
        val nro1 = "113"
        val nro2 = "113"
        val nro3 = "113"
        val nro4 = "113"

        // Asignar listeners a cada botón para ejecutar la acción de llamada
        binding.btnLLamada1.setOnClickListener { realizarLlamada(nro1) }
        binding.btnLLamada2.setOnClickListener { realizarLlamada(nro2) }
        binding.btnLLamada3.setOnClickListener { realizarLlamada(nro3) }
        binding.btnLLamada4.setOnClickListener { realizarLlamada(nro4) }
    }

    /**
     * Crea un Intent implícito para abrir la aplicación de marcado telefónico con el número especificado.
     * @param numero El número de teléfono al cual llamar.
     */
    private fun realizarLlamada(numero: String) {
        // ACTION_DIAL abre el marcador sin realizar la llamada automáticamente, lo cual es más seguro y no requiere permisos de CALL_PHONE inmediatos.
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$numero")
        }
        startActivity(intent)
    }

    /**
     * Libera el binding al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
