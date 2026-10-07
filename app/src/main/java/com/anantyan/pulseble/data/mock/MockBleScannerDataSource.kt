package com.anantyan.pulseble.data.mock

import com.anantyan.pulseble.data.ble.BleScanResult
import com.anantyan.pulseble.data.ble.BleScannerDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockBleScannerDataSource @Inject constructor() : BleScannerDataSource {

    private data class SimulatedBeacon(
        val macAddress: String,
        val name: String,
        var currentRssi: Int,
        val minRssi: Int,
        val maxRssi: Int,
        val txPower: Int
    )

    private val initialBeacons = listOf(
        SimulatedBeacon("C4:7D:4F:1A:3B:88", "Galaxy SmartTag2 Pro", -25, -30, -18, -59),
        SimulatedBeacon("E2:15:B0:9C:F1:42", "Sony WH-1000XM5", -42, -49, -34, -59),
        SimulatedBeacon("AA:BB:CC:DD:EE:01", "Apple AirTag (Beacon)", -63, -69, -52, -59),
        SimulatedBeacon("3F:8A:2C:9E:04:1B", "Mi Band 8 Active", -75, -79, -71, -59),
        SimulatedBeacon("7C:91:22:FE:5D:80", "Tile Mate Tracker", -85, -89, -81, -59),
        SimulatedBeacon("D0:03:4B:89:12:34", "ESP32 Telemetry Node", -92, -96, -88, -59)
    )

    override fun scan(): Flow<BleScanResult> = flow {
        // Clone beacons so drift persists over time
        val beacons = initialBeacons.map { it.copy() }

        while (true) {
            // Pick a random beacon to emit an advertising packet
            val beacon = beacons.random()

            // Random walk jitter: +/- 1 to 3 dBm with bounds clamping
            val jitter = Random.nextInt(-3, 4)
            beacon.currentRssi = (beacon.currentRssi + jitter).coerceIn(beacon.minRssi, beacon.maxRssi)

            emit(
                BleScanResult(
                    macAddress = beacon.macAddress,
                    name = beacon.name,
                    rssi = beacon.currentRssi,
                    txPower = beacon.txPower,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Emit packets at realistic BLE intervals (150ms - 350ms)
            delay(Random.nextLong(150, 350))
        }
    }
}
