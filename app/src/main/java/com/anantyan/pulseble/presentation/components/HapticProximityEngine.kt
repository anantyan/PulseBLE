package com.anantyan.pulseble.presentation.components

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.anantyan.pulseble.domain.model.ProximityZone

object HapticProximityEngine {

    @Suppress("DEPRECATION")
    fun triggerProximityPing(context: Context, zone: ProximityZone) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (!vibrator.hasVibrator()) return

        try {
            when (zone) {
                ProximityZone.VERY_CLOSE -> {
                    // Urgent dual-pulse (< 1m)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = longArrayOf(0, 45, 60, 45)
                        val amplitudes = intArrayOf(0, 255, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                    } else {
                        vibrator.vibrate(longArrayOf(0, 45, 60, 45), -1)
                    }
                }
                ProximityZone.CLOSE -> {
                    // Firm single pulse (1 - 3m)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vibrator.vibrate(35)
                    }
                }
                ProximityZone.MODERATE -> {
                    // Light subtle tick (3 - 10m)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(15, 80))
                    } else {
                        vibrator.vibrate(15)
                    }
                }
                else -> {
                    // Silent for WEAK, VERY_WEAK, LOST to save battery
                }
            }
        } catch (_: Exception) {
            // Ignore security or hardware exceptions gracefully
        }
    }
}
