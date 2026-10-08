package com.anantyan.pulseble.domain.repository

import com.anantyan.pulseble.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleDeviceRepository {
    val activeDevices: StateFlow<List<BleDevice>>
    val isScanning: StateFlow<Boolean>
    val scanError: StateFlow<String?>

    fun startScan()
    fun stopScan()
    fun clearScanError()

    fun trackDevice(macAddress: String): Flow<BleDevice?>
    fun getHistoryDevices(): Flow<List<BleDevice>>
    suspend fun clearHistory()
    suspend fun deleteHistoryDevice(macAddress: String)
    suspend fun updateCustomDeviceName(macAddress: String, customName: String)

    fun isBluetoothEnabled(): Boolean
    fun isBluetoothSupported(): Boolean
}
