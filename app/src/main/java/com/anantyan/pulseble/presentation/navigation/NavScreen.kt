package com.anantyan.pulseble.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Radar
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class NavScreen(val route: String) {
    data object Splash : NavScreen("splash")
    data object Scanner : NavScreen("scanner")
    data object History : NavScreen("history")

    data object Radar : NavScreen("radar/{macAddress}") {
        fun createRoute(macAddress: String): String {
            val encodedMac = URLEncoder.encode(macAddress, StandardCharsets.UTF_8.toString())
            return "radar/$encodedMac"
        }
    }
}

sealed class BottomNavItem(
    val screen: NavScreen,
    val title: String,
    val icon: ImageVector
) {
    data object Scanner : BottomNavItem(NavScreen.Scanner, "Scanner", Icons.Default.Radar)
    data object History : BottomNavItem(NavScreen.History, "Riwayat", Icons.Default.History)

    companion object {
        val items = listOf(Scanner, History)
    }
}
