package com.example.primertpdeappmoviles.presentation.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.primertpdeappmoviles.databinding.FragmentDBinding

class FragmentD : Fragment() {

    private var _binding: FragmentDBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Definir los números para cada botón
        val nro1 = "113"
        val nro2 = "113"
        val nro3 = "113"
        val nro4 = "113"

        // Configurar los botones
        binding.btnLLamada1.setOnClickListener { realizarLlamada(nro1) }
        binding.btnLLamada2.setOnClickListener { realizarLlamada(nro2) }
        binding.btnLLamada3.setOnClickListener { realizarLlamada(nro3) }
        binding.btnLLamada4.setOnClickListener { realizarLlamada(nro4) }
    }

    private fun realizarLlamada(numero: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$numero")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
