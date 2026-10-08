package com.anantyan.pulseble.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anantyan.pulseble.domain.repository.BleDeviceRepository
import com.anantyan.pulseble.domain.usecase.ClearHistoryUseCase
import com.anantyan.pulseble.domain.usecase.GetHistoryDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryDevicesUseCase: GetHistoryDevicesUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val repository: BleDeviceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = combine(
        getHistoryDevicesUseCase(),
        _searchQuery
    ) { devices, query ->
        val filtered = if (query.isBlank()) {
            devices
        } else {
            devices.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.macAddress.contains(query, ignoreCase = true)
            }
        }
        HistoryUiState(
            historyDevices = filtered,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }

    fun deleteDevice(macAddress: String) {
        viewModelScope.launch {
            repository.deleteHistoryDevice(macAddress)
        }
    }
}
