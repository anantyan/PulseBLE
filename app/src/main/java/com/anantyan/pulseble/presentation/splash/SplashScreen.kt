package com.anantyan.pulseble.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantyan.pulseble.presentation.theme.DarkBackground
import com.anantyan.pulseble.presentation.theme.DarkSurface
import com.anantyan.pulseble.presentation.theme.ElectricBlue
import com.anantyan.pulseble.presentation.theme.ElectricBlueLight
import com.anantyan.pulseble.presentation.theme.NeonCyan
import com.anantyan.pulseble.presentation.theme.RadarEmerald
import com.anantyan.pulseble.presentation.theme.TextMuted
import com.anantyan.pulseble.presentation.theme.TextPrimary
import com.anantyan.pulseble.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    var statusText by remember { mutableStateOf("INISIALISASI ENGINE...") }
    val interactionSource = remember { MutableInteractionSource() }

    // Automatic sequence with delay before proceeding to main
    LaunchedEffect(Unit) {
        delay(800)
        statusText = "MEMERIKSA SUBSISTEM BLUETOOTH..."
        delay(800)
        statusText = "RADAR TAKTIS SIAP"
        delay(700)
        onNavigateToMain()
    }

    // Infinite animations for interactive tactical radar effect
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimations")

    // Radar 360 sweeping angle
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    // Pulsing central radar scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Expanding concentric ripples
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleProgress"
    )

    // Status beacon pulse
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BeaconAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // Interactive tap to skip immediately
                onNavigateToMain()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Tactical Radar Logo Graphic Container
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Canvas with concentric rings and sweeping beam
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.minDimension / 2f

                    // Concentric rings
                    val ringSteps = 4
                    for (i in 1..ringSteps) {
                        val radius = maxRadius * (i.toFloat() / ringSteps)
                        drawCircle(
                            color = ElectricBlue.copy(alpha = 0.12f * (i.toFloat() / ringSteps)),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Expanding ripple ring
                    val rippleRadius = maxRadius * rippleProgress
                    val rippleAlpha = (1f - rippleProgress).coerceIn(0f, 1f)
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.4f * rippleAlpha),
                        radius = rippleRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Sweeping radar beam line
                    val angleRad = Math.toRadians(sweepAngle.toDouble())
                    val endX = center.x + maxRadius * Math.cos(angleRad).toFloat()
                    val endY = center.y + maxRadius * Math.sin(angleRad).toFloat()

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.8f),
                                NeonCyan.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            start = center,
                            end = Offset(endX, endY)
                        ),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                // Central Tactical Radar Icon Container (uses Icons.Default.Radar from App Bar)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    DarkSurface,
                                    DarkBackground
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(ElectricBlue, NeonCyan)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "PulseBLE Radar Logo",
                        tint = ElectricBlueLight,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Title
            Text(
                text = "PulseBLE",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                letterSpacing = 1.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tactical Subtitle
            Text(
                text = "TACTICAL BLE SCANNER & TRACKER",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Tactical Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing green radar indicator dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(RadarEmerald.copy(alpha = beaconAlpha))
                    )
                    Text(
                        text = statusText,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive hint
            Text(
                text = "Ketuk untuk masuk langsung",
                color = TextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
