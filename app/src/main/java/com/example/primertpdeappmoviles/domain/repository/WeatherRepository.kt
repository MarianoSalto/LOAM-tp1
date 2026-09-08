package com.example.primertpdeappmoviles.domain.repository

import com.example.primertpdeappmoviles.domain.model.Alerta// Calse que defuine los datos de la alerta

interface WeatherRepository {//Aca definimos una función que va a escuchar las alertas
    fun escucharAlertas(onAlertasCambio: (List<Alerta>) -> Unit)
}