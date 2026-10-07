package com.anantyan.pulseble.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    val macAddress: String,
    val name: String,
    val lastRssi: Int,
    val estimatedDistanceMeters: Double,
    val proximityZoneName: String,
    val lastSeenTimestamp: Long,
    val firstSeenTimestamp: Long,
    val totalDetections: Int = 1
)
