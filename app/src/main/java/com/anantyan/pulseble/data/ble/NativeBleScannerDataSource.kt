package com.anantyan.pulseble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.anantyan.pulseble.core.BleCompanyIdentifiers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NativeBleScannerDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context?
) : BleScannerDataSource {

    // Secondary constructor for testing without Android Context
    constructor() : this(null)

    private val bluetoothManager: BluetoothManager? by lazy {
        context?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    open fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    open fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    override fun scan(): Flow<BleScanResult> = callbackFlow {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            close(IllegalStateException("Bluetooth is not enabled or not supported"))
            return@callbackFlow
        }

        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("Bluetooth LE Scanner is unavailable"))
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    val device = it.device
                    val address = device.address ?: return

                    // 1. Check user-assigned alias on Android 11+ (API 30+)
                    var aliasName: String? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            aliasName = device.alias?.takeIf { a -> a.isNotBlank() }
                        } catch (_: SecurityException) {
                            // Safe fallback if permission is missing
                        }
                    }

                    // 2. Check bonded devices for custom alias or saved name
                    if (aliasName == null) {
                        try {
                            val bonded = adapter.bondedDevices?.firstOrNull { b ->
                                b.address.equals(address, ignoreCase = true)
                            }
                            if (bonded != null) {
                                aliasName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    bonded.alias?.takeIf { a -> a.isNotBlank() } ?: bonded.name
                                } else {
                                    bonded.name
                                }
                            }
                        } catch (_: SecurityException) {
                            // Safe fallback
                        }
                    }

                    // 3. Check advertised device name (device.name, scanRecord.deviceName, or raw AD bytes)
                    val advertisedName = try {
                        device.name?.takeIf { n -> n.isNotBlank() }
                    } catch (_: SecurityException) {
                        null
                    }
                        ?: it.scanRecord?.deviceName?.takeIf { n -> n.isNotBlank() }
                        ?: BleCompanyIdentifiers.extractAdvertisedNameFromBytes(it.scanRecord?.bytes)

                    // 4. Resolve vendor identity (e.g., Apple, Samsung, Xiaomi) from manufacturer data or services
                    val vendorName = BleCompanyIdentifiers.resolveVendor(it.scanRecord)

                    val resolvedName = aliasName ?: advertisedName ?: ""
                    val txPower = it.scanRecord?.txPowerLevel.takeIf { p -> p != Int.MIN_VALUE }

                    trySend(
                        BleScanResult(
                            macAddress = address,
                            name = resolvedName,
                            rssi = it.rssi,
                            txPower = txPower,
                            timestamp = System.currentTimeMillis(),
                            vendorName = vendorName
                        )
                    )
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("BLE scan failed with error code: $errorCode"))
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            try {
                if (adapter.isEnabled) {
                    scanner.stopScan(scanCallback)
                }
            } catch (_: Exception) {
                // Safe ignore on teardown
            }
        }
    }
}
