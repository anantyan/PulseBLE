package com.anantyan.pulseble.presentation.radar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantyan.pulseble.domain.model.ProximityZone
import com.anantyan.pulseble.presentation.components.ConcentricRadarCanvas
import com.anantyan.pulseble.presentation.components.HapticProximityEngine
import com.anantyan.pulseble.presentation.components.RenameDeviceDialog
import com.anantyan.pulseble.presentation.components.RssiSparklineChart
import com.anantyan.pulseble.presentation.theme.DarkBackground
import com.anantyan.pulseble.presentation.theme.DarkSurface
import com.anantyan.pulseble.presentation.theme.ElectricBlue
import com.anantyan.pulseble.presentation.theme.NeonCyan
import com.anantyan.pulseble.presentation.theme.SignalLost
import com.anantyan.pulseble.presentation.theme.TextMuted
import com.anantyan.pulseble.presentation.theme.TextPrimary
import com.anantyan.pulseble.presentation.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    state: RadarUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onUpdateCustomName: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val device = state.device
    val zone = device?.proximityZone ?: ProximityZone.LOST
    var showRenameDialog by remember { mutableStateOf(false) }

    val animatedAccentColor by animateColorAsState(
        targetValue = zone.displayColor,
        label = "AccentColorAnimation"
    )

    // Trigger dynamic tactile haptic feedback when device proximity updates
    LaunchedEffect(device?.updateCount, zone) {
        if (device != null) {
            HapticProximityEngine.triggerProximityPing(context, zone)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tactical Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = device?.displayName ?: "Melacak Perangkat",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.macAddress,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Ubah Nama",
                        tint = TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
        )

        if (showRenameDialog && device != null) {
            RenameDeviceDialog(
                macAddress = state.macAddress,
                currentDisplayName = device.customName ?: device.displayName,
                onConfirm = { newName ->
                    onUpdateCustomName(newName)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tactical Concentric Radar Canvas
            ConcentricRadarCanvas(
                targetDevice = device,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Dynamic Signal Category & Proximity Card (PRD 2.3 & 3.2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, animatedAccentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(animatedAccentColor)
                        )
                        Text(
                            text = zone.categoryName,
                            color = animatedAccentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Main Estimated Distance Display
                    Text(
                        text = device?.formattedDistance ?: "Mencari Sinyal...",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Zona: ${zone.distanceDescription}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Connection Stability & Signal Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Raw RSSI vs Smoothed RSSI
                MetricCard(
                    icon = Icons.Default.Wifi,
                    title = "Kekuatan Sinyal",
                    primaryValue = if (device != null) "${device.rawRssi} dBm" else "--",
                    secondaryValue = if (device != null) "EMA: ${device.smoothedRssi} dBm" else "Menunggu...",
                    accentColor = animatedAccentColor,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Update Rate & Jitter Stability
                MetricCard(
                    icon = Icons.Default.Speed,
                    title = "Frekuensi Update",
                    primaryValue = if (state.packetReceptionRateHz > 0) {
                        String.format(Locale.US, "%.1f Hz", state.packetReceptionRateHz)
                    } else "0.0 Hz",
                    secondaryValue = if (state.signalJitter > 0) {
                        "Jitter: ±${String.format(Locale.US, "%.1f", state.signalJitter)} dB"
                    } else "Stabil",
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            // Real-Time RSSI Sparkline Graph
            RssiSparklineChart(
                rssiHistory = device?.rssiHistory ?: emptyList(),
                currentRssi = device?.rawRssi ?: -95,
                accentColor = animatedAccentColor
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    primaryValue: String,
    secondaryValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = primaryValue,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = secondaryValue,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
