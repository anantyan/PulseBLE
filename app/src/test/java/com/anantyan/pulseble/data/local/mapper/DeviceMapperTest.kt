package com.anantyan.pulseble.data.local.mapper

import com.anantyan.pulseble.data.local.entity.DeviceEntity
import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.model.ProximityZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceMapperTest {

    @Test
    fun testDomainToEntityAndBack() {
        val domainDevice = BleDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test BLE Tag",
            rawRssi = -42,
            smoothedRssi = -42.0,
            estimatedDistanceMeters = 2.1,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1700000000000L,
            updateCount = 5
        )

        val entity = DeviceMapper.toEntity(domainDevice, existingFirstSeen = 1699990000000L)
        assertEquals("AA:BB:CC:DD:EE:FF", entity.macAddress)
        assertEquals("Test BLE Tag", entity.name)
        assertEquals(-42, entity.lastRssi)
        assertEquals(2.1, entity.estimatedDistanceMeters, 0.01)
        assertEquals("CLOSE", entity.proximityZoneName)
        assertEquals(1700000000000L, entity.lastSeenTimestamp)
        assertEquals(1699990000000L, entity.firstSeenTimestamp)
        assertEquals(5, entity.totalDetections)

        val backToDomain = DeviceMapper.toDomain(entity)
        assertEquals(domainDevice.macAddress, backToDomain.macAddress)
        assertEquals(domainDevice.name, backToDomain.name)
        assertEquals(domainDevice.rawRssi, backToDomain.rawRssi)
        assertEquals(domainDevice.proximityZone, backToDomain.proximityZone)
        assertEquals(5, backToDomain.updateCount)
    }

    @Test
    fun testEntityWithInvalidZoneFallsBackGracefully() {
        val corruptedEntity = DeviceEntity(
            macAddress = "11:22:33:44:55:66",
            name = "Corrupted Entity",
            lastRssi = -20,
            estimatedDistanceMeters = 0.5,
            proximityZoneName = "NON_EXISTENT_ZONE",
            lastSeenTimestamp = 1700000000000L,
            firstSeenTimestamp = 1700000000000L
        )

        val domain = DeviceMapper.toDomain(corruptedEntity)
        assertEquals(ProximityZone.VERY_CLOSE, domain.proximityZone)
    }
}
