package com.example.primertpdeappmoviles.domain.usecase

import com.example.primertpdeappmoviles.domain.model.BatteryInfo
import com.example.primertpdeappmoviles.domain.repository.BatteryRepository

/**
 * GetBatteryInfoUseCase: Caso de Uso encargado de obtener la información de la batería.
 * 
 * En Clean Architecture, los Casos de Uso (o Interactors) representan la lógica de negocio
 * pura y específica de la aplicación. Actúan como mediadores entre la capa de presentación 
 * (ViewModels) y la capa de datos (Repositories).
 */
class GetBatteryInfoUseCase(
    // Dependencia del repositorio (abstracción) inyectada a través del constructor.
    // Esto permite que el caso de uso no dependa de cómo se obtienen los datos (Firestore, API, Sensores).
    private val repository: BatteryRepository
) {
    /**
     * El operador 'invoke' permite ejecutar esta clase como si fuera una función.
     * Ejemplo de uso: getLocationUseCase()
     * 
     * @return Un objeto BatteryInfo con los datos actuales de carga y porcentaje.
     */
    operator fun invoke(): BatteryInfo {
        // Delega la responsabilidad de obtener los datos al repositorio.
        return repository.getBatteryInfo()
    }
}
