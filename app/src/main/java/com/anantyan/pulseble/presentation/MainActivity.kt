package com.anantyan.pulseble.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.anantyan.pulseble.presentation.navigation.PulseNavHost
import com.anantyan.pulseble.presentation.scanner.ScannerViewModel
import com.anantyan.pulseble.presentation.theme.PulseBLETheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val scannerViewModel: ScannerViewModel by viewModels()

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkBluetoothStatus()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        scannerViewModel.setPermissionsGranted(allGranted)
        if (allGranted) {
            scannerViewModel.startScanning()
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                val isEnabled = state == BluetoothAdapter.STATE_ON
                scannerViewModel.setBluetoothEnabled(isEnabled)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Lifecycle-aware scanning management (PRD Section 4.3)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                checkPermissionsAndBluetooth()
                scannerViewModel.onAppForegrounded()
            }

            override fun onStop(owner: LifecycleOwner) {
                scannerViewModel.onAppBackgrounded()
            }
        })

        // Register broadcast receiver for Bluetooth ON/OFF hardware events
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)

        setContent {
            PulseBLETheme {
                PulseNavHost(
                    scannerViewModel = scannerViewModel,
                    onEnableBluetooth = { requestEnableBluetooth() },
                    onRequestPermissions = { requestRequiredPermissions() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (_: Exception) {
            // Safe ignore if receiver was not registered
        }
    }

    private fun checkPermissionsAndBluetooth() {
        val permissionsGranted = hasRequiredPermissions()
        scannerViewModel.setPermissionsGranted(permissionsGranted)
        checkBluetoothStatus()
    }

    private fun checkBluetoothStatus() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter?.isEnabled == true
        scannerViewModel.setBluetoothEnabled(isEnabled)
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun requestRequiredPermissions() {
        permissionLauncher.launch(getRequiredPermissions())
    }

    private fun requestEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        try {
            bluetoothEnableLauncher.launch(enableBtIntent)
        } catch (_: Exception) {
            // Intent not supported on some emulators
        }
    }
}
