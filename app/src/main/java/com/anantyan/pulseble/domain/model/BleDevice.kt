package com.anantyan.pulseble.domain.model

data class BleDevice(
    val macAddress: String,
    val name: String,
    val rawRssi: Int,
    val smoothedRssi: Double,
    val estimatedDistanceMeters: Double,
    val proximityZone: ProximityZone,
    val lastSeenTimestamp: Long,
    val txPower: Int? = null,
    val rssiHistory: List<Int> = emptyList(),
    val updateCount: Int = 1,
    val isSimulated: Boolean = false,
    val packetsPerSecond: Double = 0.0
) {
    val displayName: String
        get() = name.ifBlank { "Unknown BLE Device" }

    val formattedDistance: String
        get() = when {
            proximityZone == ProximityZone.LOST -> "Terputus"
            estimatedDistanceMeters < 1.0 -> "< 1.0 m"
            estimatedDistanceMeters > 20.0 -> "> 20.0 m"
            else -> String.format(java.util.Locale.US, "%.1f m", estimatedDistanceMeters)
        }
}
