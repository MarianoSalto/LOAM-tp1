package com.example.primertpdeappmoviles.domain.usecase

/**
 * ProcesoComandoVosUsecase: Analiza el texto recibido por voz para 
 * determinar qué acción desea realizar el usuario.
 */
class ProcesoComandoVosUsecase {
    
    operator fun invoke(text: String): VoiceAction {
        val cleanText = text.lowercase().trim()

        return when {
            // Comandos para encender la linterna
            cleanText.contains("prender") || cleanText.contains("encender") || cleanText.contains("luz") -> {
                if (cleanText.contains("apagar")) VoiceAction.FlashlightOff else VoiceAction.FlashlightOn
            }
            // Comandos para apagar la linterna
            cleanText.contains("apagar") || cleanText.contains("detener") -> VoiceAction.FlashlightOff
            
            else -> VoiceAction.Unknown
        }
    }

    // Acciones soportadas por el asistente de voz
    sealed class VoiceAction {
        object FlashlightOn : VoiceAction()
        object FlashlightOff : VoiceAction()
        object Unknown : VoiceAction()
    }
}
