package com.anantyan.pulseble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
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
                    val name = device.name ?: it.scanRecord?.deviceName ?: ""
                    val txPower = it.scanRecord?.txPowerLevel.takeIf { p -> p != Int.MIN_VALUE }

                    trySend(
                        BleScanResult(
                            macAddress = address,
                            name = name,
                            rssi = it.rssi,
                            txPower = txPower,
                            timestamp = System.currentTimeMillis()
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
