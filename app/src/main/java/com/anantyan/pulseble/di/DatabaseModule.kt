package com.anantyan.pulseble.di

import android.content.Context
import androidx.room.Room
import com.anantyan.pulseble.data.local.PulseBleDatabase
import com.anantyan.pulseble.data.local.dao.DeviceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePulseBleDatabase(
        @ApplicationContext context: Context
    ): PulseBleDatabase {
        return Room.databaseBuilder(
            context,
            PulseBleDatabase::class.java,
            PulseBleDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(database: PulseBleDatabase): DeviceDao {
        return database.deviceDao()
    }
}
