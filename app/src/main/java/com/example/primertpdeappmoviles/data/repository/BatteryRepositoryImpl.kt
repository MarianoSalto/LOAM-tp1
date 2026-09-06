package com.example.primertpdeappmoviles.data.repository

import com.example.primertpdeappmoviles.data.datasouce.BatteryDataSource
import com.example.primertpdeappmoviles.domain.model.BatteryInfo
import com.example.primertpdeappmoviles.domain.repository.BatteryRepository

class BatteryRepositoryImpl(
    private val batteryDataSource: BatteryDataSource
) : BatteryRepository {

    override fun getBatteryInfo(): BatteryInfo {
        return batteryDataSource.getBatteryInfo()
    }
}