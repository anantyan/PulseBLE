package com.anantyan.pulseble.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anantyan.pulseble.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY lastSeenTimestamp DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<DeviceEntity>): List<Long>

    @Query("SELECT * FROM devices WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getDeviceByMac(macAddress: String): DeviceEntity?

    @Query("UPDATE devices SET customName = :customName WHERE macAddress = :macAddress")
    suspend fun updateCustomName(macAddress: String, customName: String): Int

    @Query("DELETE FROM devices")
    suspend fun clearAll(): Int

    @Query("DELETE FROM devices WHERE macAddress = :macAddress")
    suspend fun deleteDeviceByMac(macAddress: String): Int
}
