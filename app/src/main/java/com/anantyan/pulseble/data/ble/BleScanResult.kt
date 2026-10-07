package com.anantyan.pulseble.data.ble

data class BleScanResult(
    val macAddress: String,
    val name: String,
    val rssi: Int,
    val txPower: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
