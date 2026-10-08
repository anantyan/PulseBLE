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
    val packetsPerSecond: Double = 0.0,
    val customName: String? = null,
    val vendorName: String? = null
) {
    val displayName: String
        get() {
            if (!customName.isNullOrBlank()) return customName
            if (name.isNotBlank() &&
                !name.equals("Unknown", ignoreCase = true) &&
                !name.equals("Unknown BLE Device", ignoreCase = true)) {
                return name
            }
            val suffix = macAddress.takeLast(5)
            if (!vendorName.isNullOrBlank()) {
                return "$vendorName ($suffix)"
            }
            return "BLE Peripheral ($suffix)"
        }

    val formattedDistance: String
        get() = when {
            proximityZone == ProximityZone.LOST -> "Terputus"
            estimatedDistanceMeters < 1.0 -> "< 1.0 m"
            estimatedDistanceMeters > 20.0 -> "> 20.0 m"
            else -> String.format(java.util.Locale.US, "%.1f m", estimatedDistanceMeters)
        }
}
