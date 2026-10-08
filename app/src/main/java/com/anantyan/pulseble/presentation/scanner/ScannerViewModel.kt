package com.anantyan.pulseble.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anantyan.pulseble.domain.repository.BleDeviceRepository
import com.anantyan.pulseble.domain.usecase.GetActiveDevicesUseCase
import com.anantyan.pulseble.domain.usecase.StartScanUseCase
import com.anantyan.pulseble.domain.usecase.StopScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val startScanUseCase: StartScanUseCase,
    private val stopScanUseCase: StopScanUseCase,
    private val getActiveDevicesUseCase: GetActiveDevicesUseCase,
    private val repository: BleDeviceRepository
) : ViewModel() {

    private data class FilterConfig(
        val query: String,
        val minRssi: Int
    )

    private data class SystemStatus(
        val isBluetoothEnabled: Boolean,
        val hasPermissions: Boolean
    )

    private data class ScanControlStatus(
        val isScanning: Boolean,
        val scanError: String?
    )

    private val _searchQuery = MutableStateFlow("")
    private val _minRssiThreshold = MutableStateFlow(-100)
    private val _isBluetoothEnabled = MutableStateFlow(repository.isBluetoothEnabled())
    private val _hasPermissions = MutableStateFlow(true)

    private var wasScanningBeforeBackground: Boolean = false

    private val filterConfigFlow = combine(_searchQuery, _minRssiThreshold) { query, threshold ->
        FilterConfig(query, threshold)
    }

    private val systemStatusFlow = combine(_isBluetoothEnabled, _hasPermissions) { bt, perm ->
        SystemStatus(bt, perm)
    }

    private val scanControlStatusFlow = combine(
        repository.isScanning,
        repository.scanError
    ) { isScanning, error ->
        ScanControlStatus(isScanning, error)
    }

    val uiState: StateFlow<ScannerUiState> = combine(
        getActiveDevicesUseCase(),
        filterConfigFlow,
        scanControlStatusFlow,
        systemStatusFlow
    ) { devices, filter, scanStatus, system ->

        val filtered = devices
            .filter { it.rawRssi >= filter.minRssi }
            .filter { device ->
                if (filter.query.isBlank()) true
                else {
                    device.displayName.contains(filter.query, ignoreCase = true) ||
                            device.name.contains(filter.query, ignoreCase = true) ||
                            device.macAddress.contains(filter.query, ignoreCase = true)
                }
            }
            .sortedByDescending { it.rawRssi } // Automatically sorted by strongest RSSI descending (PRD 2.2)

        ScannerUiState(
            devices = filtered,
            isScanning = scanStatus.isScanning,
            scanError = scanStatus.scanError,
            searchQuery = filter.query,
            minRssiThreshold = filter.minRssi,
            isBluetoothEnabled = system.isBluetoothEnabled,
            hasPermissions = system.hasPermissions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState(
            isBluetoothEnabled = repository.isBluetoothEnabled()
        )
    )

    fun startScanning() {
        if (!_isBluetoothEnabled.value) return
        startScanUseCase()
    }

    fun stopScanning() {
        stopScanUseCase()
    }

    fun toggleScanning() {
        if (uiState.value.isScanning) {
            stopScanning()
        } else {
            startScanning()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onMinRssiThresholdChanged(threshold: Int) {
        _minRssiThreshold.value = threshold
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        _isBluetoothEnabled.value = enabled
        if (!enabled) {
            stopScanning()
        }
    }

    fun setPermissionsGranted(granted: Boolean) {
        _hasPermissions.value = granted
        if (!granted) {
            stopScanning()
        }
    }

    fun updateCustomName(macAddress: String, customName: String) {
        viewModelScope.launch {
            repository.updateCustomDeviceName(macAddress, customName)
        }
    }

    fun dismissError() {
        repository.clearScanError()
    }

    /**
     * App Lifecycle Handler (PRD Section 4.3):
     * Pauses BLE scanning when app goes to background (onStop)
     */
    fun onAppBackgrounded() {
        if (repository.isScanning.value) {
            wasScanningBeforeBackground = true
            stopScanning()
        }
    }

    /**
     * App Lifecycle Handler (PRD Section 4.3):
     * Resumes BLE scanning when app returns to foreground (onResume)
     */
    fun onAppForegrounded() {
        if (wasScanningBeforeBackground) {
            wasScanningBeforeBackground = false
            startScanning()
        }
    }
}
