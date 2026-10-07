package com.anantyan.pulseble.core

import com.anantyan.pulseble.domain.model.ProximityZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun testPrdProximityZoneMapping() {
        // -10 s/d -30 dBm -> Sangat Kuat (Sangat Dekat) < 1m
        assertEquals(ProximityZone.VERY_CLOSE, DistanceCalculator.getProximityZone(-10))
        assertEquals(ProximityZone.VERY_CLOSE, DistanceCalculator.getProximityZone(-25))
        assertEquals(ProximityZone.VERY_CLOSE, DistanceCalculator.getProximityZone(-30))

        // -30 s/d -50 dBm -> Kuat (Dekat) 1 - 3m
        assertEquals(ProximityZone.CLOSE, DistanceCalculator.getProximityZone(-31))
        assertEquals(ProximityZone.CLOSE, DistanceCalculator.getProximityZone(-45))
        assertEquals(ProximityZone.CLOSE, DistanceCalculator.getProximityZone(-50))

        // -50 s/d -70 dBm -> Cukup / Baik 3 - 10m
        assertEquals(ProximityZone.MODERATE, DistanceCalculator.getProximityZone(-51))
        assertEquals(ProximityZone.MODERATE, DistanceCalculator.getProximityZone(-65))
        assertEquals(ProximityZone.MODERATE, DistanceCalculator.getProximityZone(-70))

        // -70 s/d -80 dBm -> Lemah 10 - 20m
        assertEquals(ProximityZone.WEAK, DistanceCalculator.getProximityZone(-71))
        assertEquals(ProximityZone.WEAK, DistanceCalculator.getProximityZone(-79))
        assertEquals(ProximityZone.WEAK, DistanceCalculator.getProximityZone(-80))

        // -80 s/d -90 dBm -> Sangat Lemah / Putus-putus > 20m
        assertEquals(ProximityZone.VERY_WEAK, DistanceCalculator.getProximityZone(-81))
        assertEquals(ProximityZone.VERY_WEAK, DistanceCalculator.getProximityZone(-88))
        assertEquals(ProximityZone.VERY_WEAK, DistanceCalculator.getProximityZone(-90))

        // < -90 dBm -> Sinyal Hilang (Lost)
        assertEquals(ProximityZone.LOST, DistanceCalculator.getProximityZone(-91))
        assertEquals(ProximityZone.LOST, DistanceCalculator.getProximityZone(-100))
    }

    @Test
    fun testLogDistanceFormulaDistanceEstimation() {
        // At TxPower (-59 dBm), estimated distance should be ~1.0 meter
        val distanceAt1m = DistanceCalculator.calculateDistanceMeters(
            rssi = -59.0,
            txPower1m = -59,
            pathLossExponent = 2.0
        )
        assertEquals(1.0, distanceAt1m, 0.05)

        // Stronger signal (-40 dBm) should result in < 1 meter
        val closeDistance = DistanceCalculator.calculateDistanceMeters(
            rssi = -40.0,
            txPower1m = -59,
            pathLossExponent = 2.0
        )
        assertTrue(closeDistance < 1.0)

        // Weaker signal (-79 dBm) should result in > 5 meters
        val farDistance = DistanceCalculator.calculateDistanceMeters(
            rssi = -79.0,
            txPower1m = -59,
            pathLossExponent = 2.0
        )
        assertTrue(farDistance > 5.0)
    }

    @Test
    fun testEmaLowPassFilterSmoothsNoise() {
        val initialRssi = -50
        val smoothed1 = DistanceCalculator.applyEmaFilter(initialRssi, null)
        assertEquals(-50.0, smoothed1, 0.01)

        // Sharp noise spike from -50 to -70
        val spikeRssi = -70
        val smoothed2 = DistanceCalculator.applyEmaFilter(spikeRssi, smoothed1, alpha = 0.35)

        // Rather than jumping straight to -70, smoothed value should be:
        // 0.35 * -70 + 0.65 * -50 = -24.5 + -32.5 = -57.0
        assertEquals(-57.0, smoothed2, 0.1)

        // Another reading at -70 continues trending towards -70
        val smoothed3 = DistanceCalculator.applyEmaFilter(spikeRssi, smoothed2, alpha = 0.35)
        assertEquals(-61.5, smoothed3, 0.2)
    }
}
