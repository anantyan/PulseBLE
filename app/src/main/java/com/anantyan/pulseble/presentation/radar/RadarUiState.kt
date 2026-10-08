package com.anantyan.pulseble.presentation.radar

import com.anantyan.pulseble.domain.model.BleDevice

data class RadarUiState(
    val device: BleDevice? = null,
    val macAddress: String = "",
    val packetReceptionRateHz: Double = 0.0,
    val signalJitter: Double = 0.0,
    val isTracking: Boolean = true
) {
    val isLost: Boolean
        get() = device?.proximityZone == com.anantyan.pulseble.domain.model.ProximityZone.LOST || device == null
}
