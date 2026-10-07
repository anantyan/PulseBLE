package com.anantyan.pulseble.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors
val ElectricBlue = Color(0xFF2563EB)
val ElectricBlueLight = Color(0xFF60A5FA)
val ElectricBlueDark = Color(0xFF1D4ED8)

val RadarEmerald = Color(0xFF10B981)
val RadarEmeraldLight = Color(0xFF34D399)
val NeonCyan = Color(0xFF06B6D4)

// Dark Tactical Backgrounds
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF131B2E)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkSurfaceElevated = Color(0xFF26334D)

// Text & Outline Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val BorderSubtle = Color(0xFF334155)

// PRD Proximity Zone Colors (Dynamic Signal Badges)
val SignalVeryClose = Color(0xFF10B981)  // < 1m (-10 to -30 dBm)
val SignalClose = Color(0xFF22C55E)      // 1 - 3m (-30 to -50 dBm)
val SignalModerate = Color(0xFFF59E0B)   // 3 - 10m (-50 to -70 dBm)
val SignalWeak = Color(0xFFF97316)       // 10 - 20m (-70 to -80 dBm)
val SignalVeryWeak = Color(0xFFEF4444)   // > 20m (-80 to -90 dBm)
val SignalLost = Color(0xFF94A3B8)       // Lost (< -90 dBm)
