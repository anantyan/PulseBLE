package com.anantyan.pulseble.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantyan.pulseble.domain.model.ProximityZone

@Composable
fun SignalBadge(
    zone: ProximityZone,
    modifier: Modifier = Modifier,
    showDistance: Boolean = true
) {
    val badgeColor = zone.displayColor
    val backgroundColor = badgeColor.copy(alpha = 0.15f)
    val borderColor = badgeColor.copy(alpha = 0.45f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Glowing status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(badgeColor)
        )

        Text(
            text = zone.categoryName,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (showDistance) {
            Text(
                text = "• ${zone.distanceDescription}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
