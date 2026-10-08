package com.anantyan.pulseble.presentation.radar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anantyan.pulseble.domain.usecase.TrackDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val trackDeviceUseCase: TrackDeviceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val macAddress: String = checkNotNull(savedStateHandle.get<String>("macAddress"))

    private var lastPacketTimestamp: Long = 0L
    private val updateIntervals = mutableListOf<Long>()

    val uiState: StateFlow<RadarUiState> = trackDeviceUseCase(macAddress)
        .combine(MutableStateFlow(macAddress)) { device, mac ->
            // Calculate packet reception rate (Hz)
            val now = System.currentTimeMillis()
            var rateHz = 0.0
            if (lastPacketTimestamp > 0L) {
                val interval = now - lastPacketTimestamp
                updateIntervals.add(interval)
                if (updateIntervals.size > 10) updateIntervals.removeAt(0)
                val avgIntervalMs = updateIntervals.average()
                if (avgIntervalMs > 0) {
                    rateHz = 1000.0 / avgIntervalMs
                }
            }
            if (device != null) {
                lastPacketTimestamp = now
            }

            // Calculate signal jitter / variance
            val jitter = if (device != null && device.rssiHistory.size >= 2) {
                val history = device.rssiHistory.takeLast(10)
                val mean = history.average()
                history.map { abs(it - mean) }.average()
            } else 0.0

            RadarUiState(
                device = device,
                macAddress = mac,
                packetReceptionRateHz = rateHz,
                signalJitter = jitter,
                isTracking = true
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RadarUiState(macAddress = macAddress)
        )
}
