package com.example.primertpdeappmoviles.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit para centralizar la configuración de red.
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val weatherApiService: ClimaApiServicio by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiServicio::class.java)
    }
}
