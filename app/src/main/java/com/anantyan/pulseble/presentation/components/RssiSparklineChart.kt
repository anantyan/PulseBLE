package com.anantyan.pulseble.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantyan.pulseble.presentation.theme.DarkSurface
import com.anantyan.pulseble.presentation.theme.NeonCyan
import com.anantyan.pulseble.presentation.theme.TextMuted
import com.anantyan.pulseble.presentation.theme.TextPrimary
import com.anantyan.pulseble.presentation.theme.TextSecondary

@Composable
fun RssiSparklineChart(
    rssiHistory: List<Int>,
    currentRssi: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val minRssi = (rssiHistory.minOrNull() ?: -95).coerceAtMost(-95)
    val maxRssi = (rssiHistory.maxOrNull() ?: -30).coerceAtLeast(-30)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Title and current dBm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Riwayat Kekuatan Sinyal (RSSI)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Memantau kestabilan transmisi sinyal real-time",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$currentRssi dBm",
                    color = accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false,
                    maxLines = 1
                )
            }

            // Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(vertical = 4.dp)
            ) {
                if (rssiHistory.size < 2) {
                    Text(
                        text = "Mengumpulkan sampel sinyal...",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height
                        val range = (maxRssi - minRssi).toFloat().coerceAtLeast(10f)

                        val points = rssiHistory.mapIndexed { index, rssi ->
                            val x = (index.toFloat() / (rssiHistory.size - 1)) * width
                            val normalizedY = 1f - ((rssi - minRssi) / range)
                            val y = (normalizedY * (height - 16.dp.toPx())) + 8.dp.toPx()
                            Offset(x, y)
                        }

                        // Build line path and fill path
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }

                        // Draw gradient area under curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw smooth outline
                        drawPath(
                            path = linePath,
                            color = accentColor,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Draw highlighted current point
                        val lastPoint = points.last()
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = lastPoint
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 2.dp.toPx(),
                            center = lastPoint
                        )
                    }
                }
            }

            // Footer Min / Max Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Min: $minRssi dBm",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Sampel: ${rssiHistory.size} paket",
                    color = NeonCyan,
                    fontSize = 10.sp
                )
                Text(
                    text = "Max: $maxRssi dBm",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
