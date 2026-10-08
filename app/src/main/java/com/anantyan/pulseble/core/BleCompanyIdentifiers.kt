package com.anantyan.pulseble.core

import android.bluetooth.le.ScanRecord
import android.util.SparseArray

object BleCompanyIdentifiers {

    // Standard Bluetooth SIG 16-bit Company Identifiers
    private val COMPANY_NAMES = mapOf(
        0x004C to "Apple",
        0x0075 to "Samsung",
        0x038F to "Xiaomi",
        0x0157 to "Amazfit",
        0x00E0 to "Google",
        0x0006 to "Microsoft",
        0x0059 to "Nordic",
        0x02E5 to "Espressif (ESP32)",
        0x0046 to "Sony",
        0x009E to "Bose",
        0x0087 to "Garmin",
        0x01DA to "Logitech",
        0x005D to "Realtek",
        0x027D to "Huawei",
        0x0822 to "Realme / Oppo",
        0x0269 to "OnePlus",
        0x0440 to "Anker Soundcore",
        0x039B to "JBL / Harman",
        0x0399 to "Tuya Smart",
        0x0499 to "Ruuvi",
        0x00D2 to "Dialog Semi",
        0x0131 to "Cypress",
        0x000D to "Texas Instruments",
        0x0211 to "Telink",
        0x00B5 to "Tile",
        0x00C4 to "LG Electronics",
        0x00DF to "Motorola",
        0x0171 to "Amazon Echo",
        0x0001 to "Nokia",
        0x0002 to "Intel",
        0x0003 to "IBM",
        0x000A to "Qualcomm",
        0x000F to "Broadcom",
        0x0382 to "Fitbit",
        0x02D3 to "Beats",
        0x05A7 to "Nothing"
    )

    fun resolveVendor(scanRecord: ScanRecord?): String? {
        if (scanRecord == null) return null

        val manufacturerData: SparseArray<ByteArray>? = scanRecord.manufacturerSpecificData
        if (manufacturerData != null && manufacturerData.size() > 0) {
            val companyId = manufacturerData.keyAt(0)
            val data = manufacturerData.valueAt(0)

            // Detailed sub-type resolution for Apple Continuity / iBeacon / AirTag
            if (companyId == 0x004C && data != null && data.isNotEmpty()) {
                val subType = data[0].toInt() and 0xFF
                return when (subType) {
                    0x02 -> "Apple iBeacon"
                    0x05 -> "Apple AirTag"
                    0x07 -> "Apple AirPods"
                    0x09 -> "Apple MacBook"
                    0x10 -> "Apple Nearby Device"
                    0x12 -> "Apple Watch"
                    else -> "Apple Device"
                }
            }

            COMPANY_NAMES[companyId]?.let { companyName ->
                return when (companyId) {
                    0x0075 -> "Samsung Device"
                    0x038F -> "Xiaomi Device"
                    0x0157 -> "Amazfit Device"
                    0x00E0 -> "Google Device"
                    0x0006 -> "Microsoft Device"
                    0x0059 -> "Nordic BLE Beacon"
                    0x02E5 -> "ESP32 BLE Device"
                    0x0046 -> "Sony Audio Device"
                    0x009E -> "Bose Audio Device"
                    0x0087 -> "Garmin Device"
                    0x01DA -> "Logitech Peripheral"
                    0x027D -> "Huawei Device"
                    0x0822 -> "Realme Device"
                    0x0269 -> "OnePlus Device"
                    0x0440 -> "Anker Soundcore"
                    0x0399 -> "Tuya Smart Device"
                    0x0499 -> "Ruuvi Tag"
                    0x00B5 -> "Tile Tracker"
                    0x0382 -> "Fitbit Tracker"
                    0x02D3 -> "Beats Audio"
                    0x05A7 -> "Nothing Ear/Phone"
                    else -> "$companyName Device"
                }
            }
        }

        // Check 16-bit Service UUIDs as secondary vendor/type indicator
        val serviceUuids = scanRecord.serviceUuids
        if (serviceUuids != null) {
            for (parcelUuid in serviceUuids) {
                val uuidStr = parcelUuid.uuid.toString().uppercase()
                when {
                    uuidStr.startsWith("0000FE2C") -> return "Google Fast Pair"
                    uuidStr.startsWith("0000FEAA") -> return "Eddystone Beacon"
                    uuidStr.startsWith("0000180D") -> return "Heart Rate Monitor"
                    uuidStr.startsWith("0000180F") -> return "Battery Service"
                    uuidStr.startsWith("00001812") -> return "BLE HID Device"
                    uuidStr.startsWith("0000180A") -> return "Device Info Service"
                    uuidStr.startsWith("0000FD6F") -> return "Exposure Notification"
                }
            }
        }

        return null
    }

    /**
     * Fallback parser for EIR (Extended Inquiry Response) / Advertising Data structures.
     * Extracts AD Type 0x09 (Complete Local Name) or 0x08 (Shortened Local Name)
     * if Android's ScanRecord.getDeviceName() returns null.
     */
    fun extractAdvertisedNameFromBytes(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        var index = 0
        while (index < bytes.size) {
            val length = bytes[index].toInt() and 0xFF
            if (length == 0 || index + 1 + length > bytes.size) break
            val type = bytes[index + 1].toInt() and 0xFF
            if (type == 0x08 || type == 0x09) {
                val nameLength = length - 1
                if (nameLength > 0) {
                    val nameBytes = bytes.copyOfRange(index + 2, index + 2 + nameLength)
                    val parsed = String(nameBytes, Charsets.UTF_8).trim()
                    if (parsed.isNotBlank()) return parsed
                }
            }
            index += length + 1
        }
        return null
    }
}
