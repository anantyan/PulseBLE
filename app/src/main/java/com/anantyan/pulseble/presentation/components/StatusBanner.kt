package com.anantyan.pulseble.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantyan.pulseble.presentation.theme.SignalVeryWeak
import com.anantyan.pulseble.presentation.theme.SignalWeak

@Composable
fun StatusBanner(
    isBluetoothEnabled: Boolean,
    hasPermissions: Boolean,
    errorMessage: String?,
    onEnableBluetoothClick: () -> Unit,
    onRequestPermissionsClick: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showBanner = !isBluetoothEnabled || !hasPermissions || errorMessage != null

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isBluetoothEnabled) {
                BannerItem(
                    icon = Icons.Default.BluetoothDisabled,
                    title = "Bluetooth Nonaktif",
                    description = "Nyalakan Bluetooth untuk memindai perangkat BLE di sekitar.",
                    buttonText = "Nyalakan",
                    accentColor = SignalWeak,
                    onButtonClick = onEnableBluetoothClick
                )
            }

            if (!hasPermissions && isBluetoothEnabled) {
                BannerItem(
                    icon = Icons.Default.Warning,
                    title = "Izin Diperlukan",
                    description = "Aplikasi membutuhkan izin Bluetooth dan Lokasi untuk mendeteksi beacon.",
                    buttonText = "Berikan Izin",
                    accentColor = SignalVeryWeak,
                    onButtonClick = onRequestPermissionsClick
                )
            }

            if (errorMessage != null) {
                BannerItem(
                    icon = Icons.Default.Warning,
                    title = "Kendala Pemindaian",
                    description = errorMessage,
                    buttonText = "Tutup",
                    accentColor = MaterialTheme.colorScheme.error,
                    onButtonClick = onDismissError
                )
            }
        }
    }
}

@Composable
private fun BannerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    accentColor: Color,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedButton(
            onClick = onButtonClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
