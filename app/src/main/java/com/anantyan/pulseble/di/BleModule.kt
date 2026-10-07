package com.anantyan.pulseble.di

import com.anantyan.pulseble.data.repository.BleDeviceRepositoryImpl
import com.anantyan.pulseble.domain.repository.BleDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {

    @Binds
    @Singleton
    abstract fun bindBleDeviceRepository(
        impl: BleDeviceRepositoryImpl
    ): BleDeviceRepository
}
