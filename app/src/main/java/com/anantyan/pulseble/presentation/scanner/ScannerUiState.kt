package com.anantyan.pulseble.presentation.scanner

import com.anantyan.pulseble.domain.model.BleDevice

data class ScannerUiState(
    val devices: List<BleDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isMockMode: Boolean = false,
    val searchQuery: String = "",
    val minRssiThreshold: Int = -100,
    val isBluetoothEnabled: Boolean = true,
    val hasPermissions: Boolean = true,
    val scanError: String? = null
) {
    val activeDeviceCount: Int
        get() = devices.size
}
