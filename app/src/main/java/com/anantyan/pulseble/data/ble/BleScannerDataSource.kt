package com.anantyan.pulseble.data.ble

import kotlinx.coroutines.flow.Flow

interface BleScannerDataSource {
    fun scan(): Flow<BleScanResult>
}
