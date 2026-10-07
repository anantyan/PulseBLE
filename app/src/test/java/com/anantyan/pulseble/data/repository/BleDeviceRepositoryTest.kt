package com.anantyan.pulseble.data.repository

import com.anantyan.pulseble.data.ble.BleScanResult
import com.anantyan.pulseble.data.ble.NativeBleScannerDataSource
import com.anantyan.pulseble.data.local.dao.DeviceDao
import com.anantyan.pulseble.data.local.entity.DeviceEntity
import com.anantyan.pulseble.data.mock.MockBleScannerDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BleDeviceRepositoryTest {

    private class FakeDeviceDao : DeviceDao {
        val database = mutableMapOf<String, DeviceEntity>()

        override fun getAllDevices(): Flow<List<DeviceEntity>> =
            flowOf(database.values.toList().sortedByDescending { it.lastSeenTimestamp })

        override suspend fun upsertDevice(device: DeviceEntity): Long {
            database[device.macAddress] = device
            return 1L
        }

        override suspend fun upsertDevices(devices: List<DeviceEntity>): List<Long> {
            devices.forEach { database[it.macAddress] = it }
            return devices.map { 1L }
        }

        override suspend fun getDeviceByMac(macAddress: String): DeviceEntity? =
            database[macAddress]

        override suspend fun clearAll(): Int {
            val count = database.size
            database.clear()
            return count
        }

        override suspend fun deleteDeviceByMac(macAddress: String): Int {
            return if (database.remove(macAddress) != null) 1 else 0
        }
    }

    private lateinit var fakeDao: FakeDeviceDao
    private lateinit var mockScanner: MockBleScannerDataSource

    @Before
    fun setUp() {
        fakeDao = FakeDeviceDao()
        mockScanner = MockBleScannerDataSource()
    }

    @Test
    fun testMockModeActivationAndStopping() = runTest {
        val fakeNativeScanner = object : NativeBleScannerDataSource() {
            override fun scan(): Flow<BleScanResult> = flowOf()
            override fun isBluetoothEnabled(): Boolean = true
            override fun isBluetoothSupported(): Boolean = true
        }

        val repository = BleDeviceRepositoryImpl(
            nativeScanner = fakeNativeScanner,
            mockScanner = mockScanner,
            deviceDao = fakeDao
        )

        assertFalse(repository.isMockMode.value)
        repository.setMockMode(true)
        assertTrue(repository.isMockMode.value)

        repository.setMockMode(false)
        assertFalse(repository.isMockMode.value)
    }

    @Test
    fun testHistoryClearAndDeletion() = runTest {
        fakeDao.upsertDevice(
            DeviceEntity(
                macAddress = "11:22:33:44:55:66",
                name = "Test Beacon",
                lastRssi = -50,
                estimatedDistanceMeters = 2.0,
                proximityZoneName = "CLOSE",
                lastSeenTimestamp = 1000L,
                firstSeenTimestamp = 1000L
            )
        )

        assertEquals(1, fakeDao.database.size)

        fakeDao.deleteDeviceByMac("11:22:33:44:55:66")
        assertEquals(0, fakeDao.database.size)
    }
}
