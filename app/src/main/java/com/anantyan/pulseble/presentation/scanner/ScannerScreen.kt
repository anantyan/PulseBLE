package com.anantyan.pulseble.presentation.scanner

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.presentation.components.BleDeviceCard
import com.anantyan.pulseble.presentation.components.RenameDeviceDialog
import com.anantyan.pulseble.presentation.components.StatusBanner
import com.anantyan.pulseble.presentation.theme.DarkBackground
import com.anantyan.pulseble.presentation.theme.DarkSurface
import com.anantyan.pulseble.presentation.theme.DarkSurfaceElevated
import com.anantyan.pulseble.presentation.theme.ElectricBlue
import com.anantyan.pulseble.presentation.theme.NeonCyan
import com.anantyan.pulseble.presentation.theme.RadarEmerald
import com.anantyan.pulseble.presentation.theme.TextMuted
import com.anantyan.pulseble.presentation.theme.TextPrimary
import com.anantyan.pulseble.presentation.theme.TextSecondary
import kotlin.math.roundToInt

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    state: ScannerUiState,
    onToggleScan: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onMinRssiChange: (Int) -> Unit,
    onDeviceClick: (String) -> Unit,
    onEnableBluetoothClick: () -> Unit,
    onRequestPermissionsClick: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
    onUpdateCustomName: (String, String) -> Unit = { _, _ -> }
) {
    var showFilterPanel by remember { mutableStateOf(false) }
    var renamingDevice by remember { mutableStateOf<BleDevice?>(null) }
    var isSearchVisible by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    var isAtTop by remember { mutableStateOf(true) }
    var anchoredKey by remember { mutableStateOf<String?>(null) }
    var anchoredOffset by remember { mutableIntStateOf(0) }

    // Track user scroll position without fighting manual dragging
    LaunchedEffect(listState) {
        snapshotFlow {
            val inProgress = listState.isScrollInProgress
            val isTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            val topVisibleKey = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key as? String
            val offset = listState.firstVisibleItemScrollOffset
            Triple(inProgress, isTop, Pair(topVisibleKey, offset))
        }.collect { (inProgress, isTop, keyAndOffset) ->
            isAtTop = isTop
            if (!isTop && keyAndOffset.first != null) {
                anchoredKey = keyAndOffset.first
                anchoredOffset = keyAndOffset.second
            }
        }
    }

    // Lock and anchor scroll position across real-time list updates
    LaunchedEffect(state.devices) {
        if (listState.isScrollInProgress || state.devices.isEmpty()) return@LaunchedEffect

        if (isAtTop) {
            // Posisi on-top tetap terkunci pada item teratas (index 0, offset 0)
            // Setiap item baru/lebih kuat langsung muncul di posisi atas dan item terdorong ke bawah
            // Tanpa ada item tersembunyi di atas layar yang memaksa user scroll ke atas
            if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
                listState.scrollToItem(0, 0)
            }
        } else {
            // Posisi on-middle / on-bottom tetap terkunci pada item yang sedang dilihat pengguna
            // sehingga konten tidak meloncat ketika terjadi pergeseran ranking RSSI
            anchoredKey?.let { targetKey ->
                val targetIndex = state.devices.indexOfFirst { it.macAddress == targetKey }
                if (targetIndex >= 0) {
                    if (listState.firstVisibleItemIndex != targetIndex || listState.firstVisibleItemScrollOffset != anchoredOffset) {
                        listState.scrollToItem(targetIndex, anchoredOffset)
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    BackHandler {
        context.findActivity()?.finish()
    }

    // Nested scroll connection for snap collapsing/expanding search & filter
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -10f && isSearchVisible) {
                    isSearchVisible = false
                } else if (delta > 10f && !isSearchVisible) {
                    isSearchVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Auto-reveal search bar when reaching top of list
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
            isSearchVisible = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Tactical Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PulseBLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Live Status Indicator (Dot + text, no background or border, dynamic green / grey)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.isScanning) RadarEmerald.copy(alpha = pulseAlpha)
                                    else TextMuted
                                )
                        )
                        Text(
                            text = if (state.isScanning) "SCANNING" else "STANDBY",
                            color = if (state.isScanning) RadarEmerald else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            },
            actions = {
                // Sleek primary scan toggle button right inside top app bar
                Button(
                    onClick = onToggleScan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isScanning) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else ElectricBlue.copy(alpha = 0.2f),
                        contentColor = if (state.isScanning) MaterialTheme.colorScheme.error else ElectricBlue
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (state.isScanning) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        else ElectricBlue.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (state.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.isScanning) "Hentikan" else "Pindai",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
        )

        // Bluetooth / Permission Alert Banner
        StatusBanner(
            isBluetoothEnabled = state.isBluetoothEnabled,
            hasPermissions = state.hasPermissions,
            errorMessage = state.scanError,
            onEnableBluetoothClick = onEnableBluetoothClick,
            onRequestPermissionsClick = onRequestPermissionsClick,
            onDismissError = onDismissError
        )

        // Snap Search & Filter Bar (Hidden when scrolling down, revealed when scrolling up)
        AnimatedVisibility(
            visible = isSearchVisible || state.searchQuery.isNotEmpty() || showFilterPanel,
            enter = expandVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(220)),
            exit = shrinkVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(220))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Full-pill compact search bar (height = 44.dp matching filter button)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari nama atau MAC...",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = state.searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(ElectricBlue),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Filter Slider Toggle Button (Full-pill circular 44.dp)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (showFilterPanel || state.minRssiThreshold > -100) ElectricBlue.copy(alpha = 0.2f)
                                else DarkSurface
                            )
                            .border(
                                1.dp,
                                if (showFilterPanel || state.minRssiThreshold > -100) ElectricBlue
                                else Color.White.copy(alpha = 0.12f),
                                CircleShape
                            )
                            .clickable { showFilterPanel = !showFilterPanel },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter RSSI",
                            tint = if (showFilterPanel || state.minRssiThreshold > -100) ElectricBlue else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Expandable RSSI Slider Filter Panel (PRD 2.2)
                AnimatedVisibility(visible = showFilterPanel) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ambang Batas Sinyal Minimum (RSSI)",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (state.minRssiThreshold <= -100) "Semua Sinyal"
                                    else "≥ ${state.minRssiThreshold} dBm",
                                    color = ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Slider(
                                value = state.minRssiThreshold.toFloat(),
                                onValueChange = { onMinRssiChange(it.roundToInt()) },
                                valueRange = -100f..-30f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = ElectricBlue,
                                    activeTrackColor = ElectricBlue,
                                    inactiveTrackColor = DarkSurfaceElevated
                                )
                            )

                            Text(
                                text = "Hanya menampilkan perangkat dengan kekuatan sinyal di atas ambang batas.",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Counter Header & Active Filter Reset (Always visible for clarity)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ditemukan: ${state.activeDeviceCount} perangkat",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (state.minRssiThreshold > -100) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricBlue.copy(alpha = 0.15f))
                        .clickable { onMinRssiChange(-100) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "≥ ${state.minRssiThreshold} dBm",
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Reset filter",
                        tint = ElectricBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Device List or Empty State
        if (state.devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (state.isScanning) "Memindai frekuensi BLE sekitar..."
                        else "Pemindaian Dihentikan",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (state.isScanning) "Mendeteksi beacon & advertising packets secara real-time."
                        else "Tekan tombol 'Pindai' di atas untuk mulai mencari perangkat Bluetooth di sekitar.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.devices,
                    key = { it.macAddress }
                ) { device ->
                    BleDeviceCard(
                        modifier = Modifier.animateItem(),
                        device = device,
                        onClick = { onDeviceClick(device.macAddress) },
                        onRenameClick = { renamingDevice = device }
                    )
                }
            }
        }

        // Rename Device Dialog
        renamingDevice?.let { dev ->
            RenameDeviceDialog(
                macAddress = dev.macAddress,
                currentDisplayName = dev.customName ?: dev.displayName,
                onConfirm = { newName ->
                    onUpdateCustomName(dev.macAddress, newName)
                    renamingDevice = null
                },
                onDismiss = { renamingDevice = null }
            )
        }
    }
}
