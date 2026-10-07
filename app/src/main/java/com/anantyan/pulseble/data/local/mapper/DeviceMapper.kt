package com.anantyan.pulseble.data.local.mapper

import com.anantyan.pulseble.data.local.entity.DeviceEntity
import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.model.ProximityZone

object DeviceMapper {

    fun toDomain(entity: DeviceEntity): BleDevice {
        val zone = try {
            ProximityZone.valueOf(entity.proximityZoneName)
        } catch (_: Exception) {
            ProximityZone.fromRssi(entity.lastRssi)
        }

        return BleDevice(
            macAddress = entity.macAddress,
            name = entity.name,
            rawRssi = entity.lastRssi,
            smoothedRssi = entity.lastRssi.toDouble(),
            estimatedDistanceMeters = entity.estimatedDistanceMeters,
            proximityZone = zone,
            lastSeenTimestamp = entity.lastSeenTimestamp,
            updateCount = entity.totalDetections
        )
    }

    fun toEntity(domain: BleDevice, existingFirstSeen: Long? = null): DeviceEntity {
        return DeviceEntity(
            macAddress = domain.macAddress,
            name = domain.name,
            lastRssi = domain.rawRssi,
            estimatedDistanceMeters = domain.estimatedDistanceMeters,
            proximityZoneName = domain.proximityZone.name,
            lastSeenTimestamp = domain.lastSeenTimestamp,
            firstSeenTimestamp = existingFirstSeen ?: domain.lastSeenTimestamp,
            totalDetections = domain.updateCount
        )
    }
}
