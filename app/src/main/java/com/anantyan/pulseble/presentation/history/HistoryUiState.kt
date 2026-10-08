package com.anantyan.pulseble.presentation.history

import com.anantyan.pulseble.domain.model.BleDevice

data class HistoryUiState(
    val historyDevices: List<BleDevice> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
) {
    val totalCount: Int
        get() = historyDevices.size
}
