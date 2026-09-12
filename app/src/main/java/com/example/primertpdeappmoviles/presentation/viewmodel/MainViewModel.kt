package com.example.primertpdeappmoviles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primertpdeappmoviles.domain.usecase.RegistrarTokenAlertaUseCase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

/**
 * MainViewModel: Gestiona configuraciones globales de la aplicación,
 * como el registro de notificaciones para alertas.
 */
class MainViewModel(
    private val registrarTokenAlertaUseCase: RegistrarTokenAlertaUseCase
) : ViewModel() {

    /**
     * Recupera el token de Firebase (FCM) y lo envía al repositorio para
     * quedar suscrito a las alertas de emergencia.
     */
    fun recuperarYEnviarTokenActual() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModelScope.launch {
                    registrarTokenAlertaUseCase(token)
                }
            }
        }
    }
}
