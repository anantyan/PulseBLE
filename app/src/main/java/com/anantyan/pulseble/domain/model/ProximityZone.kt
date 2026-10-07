package com.anantyan.pulseble.domain.model

import androidx.compose.ui.graphics.Color
import com.anantyan.pulseble.presentation.theme.SignalClose
import com.anantyan.pulseble.presentation.theme.SignalLost
import com.anantyan.pulseble.presentation.theme.SignalModerate
import com.anantyan.pulseble.presentation.theme.SignalVeryClose
import com.anantyan.pulseble.presentation.theme.SignalVeryWeak
import com.anantyan.pulseble.presentation.theme.SignalWeak

/**
 * Strict 6-Zone Proximity mapping based on PRD Study Case Mobile Engineer Section 2.3
 */
enum class ProximityZone(
    val categoryName: String,
    val distanceDescription: String,
    val minRssi: Int,
    val maxRssi: Int,
    val displayColor: Color,
    val ringIndex: Int // 0 (innermost) to 4 (outermost), 5 for lost
) {
    VERY_CLOSE(
        categoryName = "Sangat Kuat (Sangat Dekat)",
        distanceDescription = "< 1 meter",
        minRssi = -30,
        maxRssi = -10,
        displayColor = SignalVeryClose,
        ringIndex = 0
    ),
    CLOSE(
        categoryName = "Kuat (Dekat)",
        distanceDescription = "1 – 3 meter",
        minRssi = -50,
        maxRssi = -30,
        displayColor = SignalClose,
        ringIndex = 1
    ),
    MODERATE(
        categoryName = "Cukup / Baik",
        distanceDescription = "3 – 10 meter",
        minRssi = -70,
        maxRssi = -50,
        displayColor = SignalModerate,
        ringIndex = 2
    ),
    WEAK(
        categoryName = "Lemah",
        distanceDescription = "10 – 20 meter",
        minRssi = -80,
        maxRssi = -70,
        displayColor = SignalWeak,
        ringIndex = 3
    ),
    VERY_WEAK(
        categoryName = "Sangat Lemah / Putus-putus",
        distanceDescription = "> 20 meter (Batas jangkauan)",
        minRssi = -90,
        maxRssi = -80,
        displayColor = SignalVeryWeak,
        ringIndex = 4
    ),
    LOST(
        categoryName = "Sinyal Hilang (Lost)",
        distanceDescription = "Terputus / Di luar jangkauan",
        minRssi = Int.MIN_VALUE,
        maxRssi = -90,
        displayColor = SignalLost,
        ringIndex = 5
    );

    companion object {
        fun fromRssi(rssi: Int): ProximityZone {
            return when {
                rssi >= -30 -> VERY_CLOSE
                rssi >= -50 -> CLOSE
                rssi >= -70 -> MODERATE
                rssi >= -80 -> WEAK
                rssi >= -90 -> VERY_WEAK
                else -> LOST
            }
        }
    }
}
