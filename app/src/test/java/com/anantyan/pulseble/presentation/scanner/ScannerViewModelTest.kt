package com.anantyan.pulseble.presentation.scanner

import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.model.ProximityZone
import com.anantyan.pulseble.domain.repository.BleDeviceRepository
import com.anantyan.pulseble.domain.usecase.GetActiveDevicesUseCase
import com.anantyan.pulseble.domain.usecase.StartScanUseCase
import com.anantyan.pulseble.domain.usecase.StopScanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeRepository : BleDeviceRepository {
        val activeDevicesFlow = MutableStateFlow<List<BleDevice>>(emptyList())
        override val activeDevices: StateFlow<List<BleDevice>> = activeDevicesFlow.asStateFlow()

        val isScanningFlow = MutableStateFlow(false)
        override val isScanning: StateFlow<Boolean> = isScanningFlow.asStateFlow()

        val scanErrorFlow = MutableStateFlow<String?>(null)
        override val scanError: StateFlow<String?> = scanErrorFlow.asStateFlow()

        override fun startScan() { isScanningFlow.value = true }
        override fun stopScan() { isScanningFlow.value = false }
        override fun clearScanError() { scanErrorFlow.value = null }
        override fun trackDevice(macAddress: String): Flow<BleDevice?> = flowOf(null)
        override fun getHistoryDevices(): Flow<List<BleDevice>> = flowOf(emptyList())
        override suspend fun clearHistory() {}
        override suspend fun deleteHistoryDevice(macAddress: String) {}
        override suspend fun updateCustomDeviceName(macAddress: String, customName: String) {}
        override fun isBluetoothEnabled(): Boolean = true
        override fun isBluetoothSupported(): Boolean = true
    }

    private lateinit var fakeRepository: FakeRepository
    private lateinit var viewModel: ScannerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeRepository()

        val startScan = StartScanUseCase(fakeRepository)
        val stopScan = StopScanUseCase(fakeRepository)
        val getActive = GetActiveDevicesUseCase(fakeRepository)

        viewModel = ScannerViewModel(
            startScanUseCase = startScan,
            stopScanUseCase = stopScan,
            getActiveDevicesUseCase = getActive,
            repository = fakeRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchFilteringByNameAndMac() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val deviceA = BleDevice("AA:11:22:33:44:55", "SmartTag", -35, -35.0, 1.2, ProximityZone.CLOSE, 1000L)
        val deviceB = BleDevice("BB:66:77:88:99:00", "Headphone", -75, -75.0, 12.0, ProximityZone.WEAK, 1000L)

        fakeRepository.activeDevicesFlow.value = listOf(deviceA, deviceB)
        advanceUntilIdle()

        // Empty query matches all
        assertEquals(2, viewModel.uiState.value.devices.size)

        // Query matching name
        viewModel.onSearchQueryChanged("Smart")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.devices.size)
        assertEquals("SmartTag", viewModel.uiState.value.devices[0].name)

        // Query matching MAC address
        viewModel.onSearchQueryChanged("BB:66")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.devices.size)
        assertEquals("Headphone", viewModel.uiState.value.devices[0].name)
    }

    @Test
    fun testSearchFilteringByVendorAndCustomName() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val vendorDevice = BleDevice(
            macAddress = "CC:11:22:33:44:55",
            name = "",
            rawRssi = -50,
            smoothedRssi = -50.0,
            estimatedDistanceMeters = 2.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L,
            vendorName = "Apple Device"
        )
        val customDevice = BleDevice(
            macAddress = "DD:66:77:88:99:00",
            name = "Generic Beacon",
            rawRssi = -55,
            smoothedRssi = -55.0,
            estimatedDistanceMeters = 3.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L,
            customName = "Arya's Tracker"
        )

        fakeRepository.activeDevicesFlow.value = listOf(vendorDevice, customDevice)
        advanceUntilIdle()

        // Search by vendor name in displayName
        viewModel.onSearchQueryChanged("Apple")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.devices.size)
        assertEquals("CC:11:22:33:44:55", viewModel.uiState.value.devices[0].macAddress)

        // Search by customName in displayName
        viewModel.onSearchQueryChanged("Arya")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.devices.size)
        assertEquals("Arya's Tracker", viewModel.uiState.value.devices[0].displayName)
    }

    @Test
    fun testRssiThresholdFiltering() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val strongDevice = BleDevice("AA:11:22:33:44:55", "Beacon Strong", -40, -40.0, 1.5, ProximityZone.CLOSE, 1000L)
        val weakDevice = BleDevice("BB:66:77:88:99:00", "Beacon Weak", -85, -85.0, 25.0, ProximityZone.VERY_WEAK, 1000L)

        fakeRepository.activeDevicesFlow.value = listOf(strongDevice, weakDevice)
        advanceUntilIdle()

        // Default threshold (-100 dBm) shows both
        assertEquals(2, viewModel.uiState.value.devices.size)

        // Filter out devices weaker than -60 dBm
        viewModel.onMinRssiThresholdChanged(-60)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.devices.size)
        assertEquals("Beacon Strong", viewModel.uiState.value.devices[0].name)
    }

    @Test
    fun testLifecycleAwareScanningPauseAndResume() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Start scanning
        viewModel.startScanning()
        advanceUntilIdle()
        assertTrue(fakeRepository.isScanning.value)

        // App backgrounded (onStop) -> Should pause scan
        viewModel.onAppBackgrounded()
        advanceUntilIdle()
        assertFalse(fakeRepository.isScanning.value)

        // App foregrounded (onResume) -> Should auto-resume scan
        viewModel.onAppForegrounded()
        advanceUntilIdle()
        assertTrue(fakeRepository.isScanning.value)
    }
}
