package com.anantyan.pulseble.data.repository

import com.anantyan.pulseble.core.DistanceCalculator
import com.anantyan.pulseble.data.ble.BleScanResult
import com.anantyan.pulseble.data.ble.NativeBleScannerDataSource
import com.anantyan.pulseble.data.local.dao.DeviceDao
import com.anantyan.pulseble.data.local.mapper.DeviceMapper
import com.anantyan.pulseble.data.mock.MockBleScannerDataSource
import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.repository.BleDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleDeviceRepositoryImpl @Inject constructor(
    private val nativeScanner: NativeBleScannerDataSource,
    private val mockScanner: MockBleScannerDataSource,
    private val deviceDao: DeviceDao
) : BleDeviceRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _activeDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    override val activeDevices: StateFlow<List<BleDevice>> = _activeDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isMockMode = MutableStateFlow(false)
    override val isMockMode: StateFlow<Boolean> = _isMockMode.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    override val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val deviceCache = ConcurrentHashMap<String, BleDevice>()
    private val pendingDbUpserts = ConcurrentHashMap<String, BleDevice>()

    private var scanJob: Job? = null
    private var dbFlushJob: Job? = null

    init {
        startPeriodicDbFlusher()
    }

    override fun isBluetoothEnabled(): Boolean = nativeScanner.isBluetoothEnabled()

    override fun isBluetoothSupported(): Boolean = nativeScanner.isBluetoothSupported()

    override fun setMockMode(enabled: Boolean) {
        val wasScanning = _isScanning.value
        if (wasScanning) {
            stopScan()
        }
        _isMockMode.value = enabled
        deviceCache.clear()
        _activeDevices.value = emptyList()
        if (wasScanning) {
            startScan()
        }
    }

    override fun clearScanError() {
        _scanError.value = null
    }

    override fun startScan() {
        if (_isScanning.value) return
        _scanError.value = null
        _isScanning.value = true

        val scanner = if (_isMockMode.value) mockScanner else nativeScanner

        scanJob = repositoryScope.launch {
            scanner.scan()
                .catch { throwable ->
                    _scanError.value = throwable.localizedMessage ?: "Scanning failed"
                    _isScanning.value = false
                }
                .collect { result ->
                    processScanResult(result)
                }
        }
    }

    override fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
        // Flush any remaining unsaved items immediately
        flushPendingDevicesToDb()
    }

    private fun processScanResult(result: BleScanResult) {
        val existing = deviceCache[result.macAddress]
        val isNewDevice = existing == null

        val smoothedRssi = DistanceCalculator.applyEmaFilter(
            currentRawRssi = result.rssi,
            previousSmoothedRssi = existing?.smoothedRssi
        )

        val estimatedDistance = DistanceCalculator.calculateDistanceMeters(
            rssi = smoothedRssi,
            txPower1m = result.txPower ?: DistanceCalculator.DEFAULT_TX_POWER_1M
        )

        val proximityZone = DistanceCalculator.getProximityZone(result.rssi)

        // Maintain sparkline history (max 25 entries)
        val history = (existing?.rssiHistory ?: emptyList()) + result.rssi
        val trimmedHistory = if (history.size > 25) history.takeLast(25) else history

        val updateCount = (existing?.updateCount ?: 0) + 1

        val updatedDevice = BleDevice(
            macAddress = result.macAddress,
            name = if (result.name.isNotBlank()) result.name else (existing?.name ?: ""),
            rawRssi = result.rssi,
            smoothedRssi = smoothedRssi,
            estimatedDistanceMeters = estimatedDistance,
            proximityZone = proximityZone,
            lastSeenTimestamp = result.timestamp,
            txPower = result.txPower,
            rssiHistory = trimmedHistory,
            updateCount = updateCount,
            isSimulated = _isMockMode.value
        )

        deviceCache[result.macAddress] = updatedDevice
        pendingDbUpserts[result.macAddress] = updatedDevice

        // Emit active devices sorted by raw RSSI descending (PRD Section 2.2)
        _activeDevices.value = deviceCache.values.sortedByDescending { it.rawRssi }

        // If it's a newly discovered device, persist immediately to Room
        if (isNewDevice) {
            repositoryScope.launch(Dispatchers.IO) {
                deviceDao.upsertDevice(DeviceMapper.toEntity(updatedDevice))
            }
        }
    }

    private fun startPeriodicDbFlusher() {
        dbFlushJob?.cancel()
        dbFlushJob = repositoryScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2000) // Debounce 2 seconds
                flushPendingDevicesToDb()
            }
        }
    }

    private fun flushPendingDevicesToDb() {
        if (pendingDbUpserts.isEmpty()) return
        val itemsToFlush = pendingDbUpserts.values.toList()
        pendingDbUpserts.clear()

        repositoryScope.launch(Dispatchers.IO) {
            val entities = itemsToFlush.map { DeviceMapper.toEntity(it) }
            deviceDao.upsertDevices(entities)
        }
    }

    override fun trackDevice(macAddress: String): Flow<BleDevice?> {
        return activeDevices.map { devices ->
            devices.find { it.macAddress.equals(macAddress, ignoreCase = true) }
        }
    }

    override fun getHistoryDevices(): Flow<List<BleDevice>> {
        return deviceDao.getAllDevices().map { entities ->
            entities.map { DeviceMapper.toDomain(it) }
        }
    }

    override suspend fun clearHistory() {
        deviceDao.clearAll()
    }

    override suspend fun deleteHistoryDevice(macAddress: String) {
        deviceDao.deleteDeviceByMac(macAddress)
    }
}
