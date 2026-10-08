package com.anantyan.pulseble.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anantyan.pulseble.data.local.dao.DeviceDao
import com.anantyan.pulseble.data.local.entity.DeviceEntity

@Database(
    entities = [DeviceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PulseBleDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    companion object {
        const val DATABASE_NAME = "pulse_ble_database"
    }
}
