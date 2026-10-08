package com.anantyan.pulseble.core

import com.anantyan.pulseble.domain.model.BleDevice
import com.anantyan.pulseble.domain.model.ProximityZone
import org.junit.Assert.assertEquals
import org.junit.Test

class BleDeviceNameResolutionTest {

    @Test
    fun testDisplayNamePrioritizesCustomName() {
        val device = BleDevice(
            macAddress = "11:22:33:44:55:66",
            name = "Advertised Name",
            rawRssi = -50,
            smoothedRssi = -50.0,
            estimatedDistanceMeters = 2.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L,
            customName = "Custom Renamed Tag",
            vendorName = "Apple Device"
        )
        assertEquals("Custom Renamed Tag", device.displayName)
    }

    @Test
    fun testDisplayNameUsesAdvertisedNameWhenNoCustomName() {
        val device = BleDevice(
            macAddress = "11:22:33:44:55:66",
            name = "Smart AirTag",
            rawRssi = -50,
            smoothedRssi = -50.0,
            estimatedDistanceMeters = 2.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L,
            vendorName = "Apple Device"
        )
        assertEquals("Smart AirTag", device.displayName)
    }

    @Test
    fun testDisplayNameFallsBackToVendorWithMacSuffix() {
        val device = BleDevice(
            macAddress = "11:22:33:44:55:66",
            name = "",
            rawRssi = -50,
            smoothedRssi = -50.0,
            estimatedDistanceMeters = 2.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L,
            vendorName = "Apple Device"
        )
        assertEquals("Apple Device (55:66)", device.displayName)
    }

    @Test
    fun testDisplayNameIgnoresGenericUnknownStringsAndFallsBack() {
        val deviceWithUnknown = BleDevice(
            macAddress = "11:22:33:44:55:66",
            name = "Unknown BLE Device",
            rawRssi = -50,
            smoothedRssi = -50.0,
            estimatedDistanceMeters = 2.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L,
            vendorName = "Samsung Device"
        )
        assertEquals("Samsung Device (55:66)", deviceWithUnknown.displayName)
    }

    @Test
    fun testDisplayNameFallsBackToBlePeripheralWithMacSuffix() {
        val unnamedDevice = BleDevice(
            macAddress = "11:22:33:44:55:66",
            name = "",
            rawRssi = -50,
            smoothedRssi = -50.0,
            estimatedDistanceMeters = 2.0,
            proximityZone = ProximityZone.CLOSE,
            lastSeenTimestamp = 1000L
        )
        assertEquals("BLE Peripheral (55:66)", unnamedDevice.displayName)
    }

    @Test
    fun testExtractAdvertisedNameFromBytes() {
        // Build mock EIR payload:
        // Length 7, Type 0x09 (Complete Local Name), Data: "MyTag" (5 bytes)
        val eirBytes = byteArrayOf(
            0x06, 0x09, 'M'.code.toByte(), 'y'.code.toByte(), 'T'.code.toByte(), 'a'.code.toByte(), 'g'.code.toByte()
        )
        val parsedName = BleCompanyIdentifiers.extractAdvertisedNameFromBytes(eirBytes)
        assertEquals("MyTag", parsedName)
    }
}
