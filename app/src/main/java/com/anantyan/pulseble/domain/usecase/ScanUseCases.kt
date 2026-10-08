package com.anantyan.pulseble.domain.usecase

import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.repository.BleDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class StartScanUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    operator fun invoke(): Unit = repository.startScan()
}

class StopScanUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    operator fun invoke(): Unit = repository.stopScan()
}

class GetActiveDevicesUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    operator fun invoke(): StateFlow<List<BleDevice>> = repository.activeDevices
}

class TrackDeviceUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    operator fun invoke(macAddress: String): Flow<BleDevice?> = repository.trackDevice(macAddress)
}

class GetHistoryDevicesUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    operator fun invoke(): Flow<List<BleDevice>> = repository.getHistoryDevices()
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    suspend operator fun invoke(): Unit = repository.clearHistory()
}

class UpdateCustomDeviceNameUseCase @Inject constructor(
    private val repository: BleDeviceRepository
) {
    suspend operator fun invoke(macAddress: String, customName: String): Unit =
        repository.updateCustomDeviceName(macAddress, customName)
}
