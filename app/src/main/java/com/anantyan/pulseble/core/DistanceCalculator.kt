package com.anantyan.pulseble.core

import com.anantyan.pulseble.domain.model.ProximityZone
import kotlin.math.pow
import kotlin.math.roundToInt

object DistanceCalculator {

    /**
     * Default reference RSSI at 1 meter distance (Measured Power / Tx Power).
     * Standard BLE beacons typically broadcast around -59 dBm at 1m.
     */
    const val DEFAULT_TX_POWER_1M = -59

    /**
     * Environmental path loss exponent (n).
     * 2.0 represents typical indoor line-of-sight environments.
     */
    const val DEFAULT_PATH_LOSS_EXPONENT = 2.0

    /**
     * Exponential Moving Average (EMA) smoothing factor (alpha).
     * 0.35 provides an ideal balance between filtering noisy BLE jitter
     * and maintaining fast responsiveness to real movement.
     */
    const val DEFAULT_EMA_ALPHA = 0.35

    /**
     * Calculates estimated distance in meters using the standard Log-Distance Path Loss Model:
     * Distance = 10 ^ ((TxPower - RSSI) / (10 * n))
     */
    fun calculateDistanceMeters(
        rssi: Double,
        txPower1m: Int = DEFAULT_TX_POWER_1M,
        pathLossExponent: Double = DEFAULT_PATH_LOSS_EXPONENT
    ): Double {
        if (rssi <= -95.0) return 30.0 // Out of range bound
        if (rssi >= -15.0) return 0.3 // Clamped near bound

        val effectiveTxPower = if (txPower1m in -85..-40) txPower1m else DEFAULT_TX_POWER_1M
        val exponent = (effectiveTxPower - rssi) / (10.0 * pathLossExponent)
        val rawDistance = 10.0.pow(exponent)
        return rawDistance.coerceIn(0.2, 35.0)
    }

    /**
     * Applies Exponential Moving Average (EMA) Low-Pass Filter:
     * S_t = alpha * Y_t + (1 - alpha) * S_{t-1}
     */
    fun applyEmaFilter(
        currentRawRssi: Int,
        previousSmoothedRssi: Double?,
        alpha: Double = DEFAULT_EMA_ALPHA
    ): Double {
        if (previousSmoothedRssi == null) {
            return currentRawRssi.toDouble()
        }
        val smoothed = (alpha * currentRawRssi) + ((1.0 - alpha) * previousSmoothedRssi)
        // Round to 1 decimal place to prevent floating point jitter
        return (smoothed * 10.0).roundToInt() / 10.0
    }

    /**
     * Categorizes proximity based on PRD Section 2.3 discrete table.
     */
    fun getProximityZone(rssi: Int): ProximityZone {
        return ProximityZone.fromRssi(rssi)
    }
}
