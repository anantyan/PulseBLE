package com.anantyan.pulseble.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anantyan.pulseble.presentation.history.HistoryScreen
import com.anantyan.pulseble.presentation.history.HistoryViewModel
import com.anantyan.pulseble.presentation.radar.RadarScreen
import com.anantyan.pulseble.presentation.radar.RadarViewModel
import com.anantyan.pulseble.presentation.scanner.ScannerScreen
import com.anantyan.pulseble.presentation.scanner.ScannerViewModel
import com.anantyan.pulseble.presentation.theme.DarkBackground
import com.anantyan.pulseble.presentation.theme.DarkSurface
import com.anantyan.pulseble.presentation.theme.ElectricBlue
import com.anantyan.pulseble.presentation.theme.TextMuted
import com.anantyan.pulseble.presentation.theme.TextPrimary
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun PulseNavHost(
    scannerViewModel: ScannerViewModel,
    onEnableBluetooth: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation on detail Radar view
    val showBottomBar = currentRoute == NavScreen.Scanner.route || currentRoute == NavScreen.History.route

    Scaffold(
        modifier = modifier.fillMaxSize().background(DarkBackground),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    color = if (selected) ElectricBlue else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricBlue,
                                unselectedIconColor = TextMuted,
                                indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavScreen.Scanner.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen 1: Dashboard Utama (Scanner)
            composable(NavScreen.Scanner.route) {
                val scannerState by scannerViewModel.uiState.collectAsStateWithLifecycle()
                ScannerScreen(
                    state = scannerState,
                    onToggleScan = { scannerViewModel.toggleScanning() },
                    onToggleMockMode = { scannerViewModel.toggleMockMode(it) },
                    onSearchQueryChange = { scannerViewModel.onSearchQueryChanged(it) },
                    onMinRssiChange = { scannerViewModel.onMinRssiThresholdChanged(it) },
                    onDeviceClick = { mac ->
                        navController.navigate(NavScreen.Radar.createRoute(mac))
                    },
                    onEnableBluetoothClick = onEnableBluetooth,
                    onRequestPermissionsClick = onRequestPermissions,
                    onDismissError = { scannerViewModel.dismissError() },
                    onUpdateCustomName = { mac, name -> scannerViewModel.updateCustomName(mac, name) }
                )
            }

            // Screen 2: Detail Pelacakan (Radar View)
            composable(
                route = NavScreen.Radar.route,
                arguments = listOf(
                    navArgument("macAddress") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val rawMac = backStackEntry.arguments?.getString("macAddress").orEmpty()
                val decodedMac = URLDecoder.decode(rawMac, StandardCharsets.UTF_8.toString())

                val radarViewModel: RadarViewModel = hiltViewModel()
                val radarState by radarViewModel.uiState.collectAsStateWithLifecycle()

                RadarScreen(
                    state = radarState,
                    onBackClick = { navController.popBackStack() },
                    onUpdateCustomName = { name -> radarViewModel.updateCustomName(name) }
                )
            }

            // Screen 3: Riwayat Perangkat (History Log)
            composable(NavScreen.History.route) {
                val historyViewModel: HistoryViewModel = hiltViewModel()
                val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()

                HistoryScreen(
                    state = historyState,
                    onSearchQueryChange = { historyViewModel.onSearchQueryChanged(it) },
                    onTrackDeviceClick = { mac ->
                        navController.navigate(NavScreen.Radar.createRoute(mac))
                    },
                    onDeleteDeviceClick = { mac -> historyViewModel.deleteDevice(mac) },
                    onClearAllHistoryClick = { historyViewModel.clearAllHistory() }
                )
            }
        }
    }
}
