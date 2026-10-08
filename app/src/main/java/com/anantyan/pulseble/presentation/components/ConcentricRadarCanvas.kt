package com.anantyan.pulseble.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.model.ProximityZone
import com.anantyan.pulseble.presentation.theme.DarkSurface
import com.anantyan.pulseble.presentation.theme.ElectricBlue
import com.anantyan.pulseble.presentation.theme.RadarEmerald
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ConcentricRadarCanvas(
    targetDevice: BleDevice?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweepTransition")

    // Continuous 360-degree radar beam sweep
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    // Pulsating target blip glow
    val blipPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlipPulse"
    )

    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f

            // 1. Radar background circular plate
            drawCircle(
                color = DarkSurface,
                radius = maxRadius,
                center = center
            )

            // 2. Crosshair grid lines
            val crosshairEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx(),
                pathEffect = crosshairEffect
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = crosshairEffect
            )

            // 3. 5 Concentric Range Rings according to PRD Proximity Zones
            // Ring fractions: 0.2 (<1m), 0.4 (1-3m), 0.6 (3-10m), 0.8 (10-20m), 1.0 (>20m)
            val ringFractions = floatArrayOf(0.20f, 0.40f, 0.60f, 0.80f, 1.00f)
            val ringColors = floatArrayOf(0.20f, 0.18f, 0.16f, 0.14f, 0.12f)

            ringFractions.forEachIndexed { index, fraction ->
                val ringRadius = maxRadius * fraction
                drawCircle(
                    color = ElectricBlue.copy(alpha = ringColors[index]),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = if (index == 4) 2.dp.toPx() else 1.dp.toPx())
                )
            }

            // 4. Sweeping radar beam with trailing glow
            val sweepRadians = Math.toRadians(sweepAngle.toDouble())
            val beamEnd = Offset(
                x = (center.x + maxRadius * cos(sweepRadians)).toFloat(),
                y = (center.y + maxRadius * sin(sweepRadians)).toFloat()
            )

            // Rotating sweeping line
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(RadarEmerald.copy(alpha = 0.2f), RadarEmerald),
                    start = center,
                    end = beamEnd
                ),
                start = center,
                end = beamEnd,
                strokeWidth = 2.dp.toPx()
            )

            // 5. Target Device Blip Marker (if device is being tracked)
            if (targetDevice != null) {
                val zone = targetDevice.proximityZone
                val blipColor = zone.displayColor

                // Map estimated distance or zone to normalized radial distance (0.0 to 1.0)
                val normalizedDistanceFraction = when (zone) {
                    ProximityZone.VERY_CLOSE -> 0.12f + (targetDevice.estimatedDistanceMeters.coerceIn(0.1, 1.0) / 1.0 * 0.08f).toFloat()
                    ProximityZone.CLOSE -> 0.22f + ((targetDevice.estimatedDistanceMeters.coerceIn(1.0, 3.0) - 1.0) / 2.0 * 0.16f).toFloat()
                    ProximityZone.MODERATE -> 0.42f + ((targetDevice.estimatedDistanceMeters.coerceIn(3.0, 10.0) - 3.0) / 7.0 * 0.16f).toFloat()
                    ProximityZone.WEAK -> 0.62f + ((targetDevice.estimatedDistanceMeters.coerceIn(10.0, 20.0) - 10.0) / 10.0 * 0.16f).toFloat()
                    ProximityZone.VERY_WEAK -> 0.82f + ((targetDevice.estimatedDistanceMeters.coerceIn(20.0, 30.0) - 20.0) / 10.0 * 0.14f).toFloat()
                    ProximityZone.LOST -> 0.98f
                }

                // Fix blip angle at 45 degrees north-east (or based on hash) for stable tracking visualization
                val angleDeg = 315.0 // Top-right quadrant
                val angleRad = Math.toRadians(angleDeg)
                val blipRadius = maxRadius * normalizedDistanceFraction

                val blipCenter = Offset(
                    x = (center.x + blipRadius * cos(angleRad)).toFloat(),
                    y = (center.y + blipRadius * sin(angleRad)).toFloat()
                )

                // Outer pulsating ripple ring
                drawCircle(
                    color = blipColor.copy(alpha = 0.25f),
                    radius = 16.dp.toPx() * blipPulse,
                    center = blipCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Middle glowing aura
                drawCircle(
                    color = blipColor.copy(alpha = 0.45f),
                    radius = 9.dp.toPx() * blipPulse,
                    center = blipCenter
                )

                // Solid center blip dot
                drawCircle(
                    color = blipColor,
                    radius = 5.dp.toPx(),
                    center = blipCenter
                )
            }

            // Radar Center Anchor
            drawCircle(
                color = ElectricBlue,
                radius = 3.dp.toPx(),
                center = center
            )
        }
    }
}
