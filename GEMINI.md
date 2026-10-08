# PulseBLE Memory & Configuration Playbook (GEMINI.md) 🧠
**Dokumen Referensi Konfigurasi Sistem, Arsitektur, & Riwayat Evolusi Proyek**

Dokumen ini mencatat seluruh konfigurasi, arsitektur, parameter teknis, keputusan desain, dan riwayat iterasi dari awal hingga versi **v1.0.5** pada proyek **PulseBLE**.

---

## 📌 1. Ikhtisar Proyek (Project Overview)
* **Nama Proyek**: PulseBLE
* **Package / Namespace**: `com.anantyan.pulseble`
* **Tujuan**: Aplikasi Android modern pelacak kedekatan (*proximity tracker*) dan radar taktis untuk perangkat Bluetooth Low Energy (BLE) secara *real-time*.
* **Repository Git**: `https://github.com/anantyan/PulseBLE.git`
* **Branch Utama**: `main`
* **Versi Rilis Saat Ini**: **`v1.0.5`** (`versionCode = 5`)
* **Platform Target**: **Android 17 (API 37)** | **Compile SDK: 37** | **Min SDK: 24 (Android 7.0)**

---

## ⚙️ 2. Konfigurasi Lingkungan & Toolchain

### 2.1. Android SDK & Build Tools
* **SDK Root**: `C:\Users\aryar\AppData\Local\Android\Sdk`
* **Platform SDK Terpasang**:
  * `platforms/android-37.0` (Android 17 Platform — *API 37*)
  * `platforms/android-36` (Android 16 Platform — *API 36*)
  * `platforms/android-35` (Android 15 Platform — *API 35*)
  * `platforms/android-34` (Android 14 Platform — *API 34*)
* **Build-Tools**: `37.0.0` (Terpasang di `C:\Users\aryar\AppData\Local\Android\Sdk\build-tools\37.0.0`)
* **Platform-Tools (ADB)**: `Version 37.0.1-15733141` (`localhost:5037`)
* **Standalone CLI**: Google Android CLI (`android.exe`) terpasang di `C:\Users\aryar\AppData\AndroidCLI\android.exe`
* **Android Studio IDE**: Terkelola via JetBrains Toolbox di `C:\Users\aryar\AppData\Local\Programs\Android Studio`

### 2.2. Gradle & Plugin Versions (`gradle/libs.versions.toml`)
* **Gradle Wrapper**: `9.1.0` (dikonfigurasi dengan JDK 17)
* **Android Gradle Plugin (AGP)**: `9.0.1`
* **Kotlin**: `2.3.20`
* **KSP (Kotlin Symbol Processing)**: `2.3.12`
* **Compose Compiler**: Terintegrasi via Kotlin 2.3.20 Compose Compiler plugin
* **Compose BOM**: `2024.12.01` (Material 3, Foundation, UI, Animation)
* **Room Database**: `2.7.2` (dengan KSP compiler)
* **Dagger Hilt**: `2.60.1` (dengan KSP compiler)
* **Navigation Compose**: `2.8.9`
* **AndroidX Activity Compose**: `1.10.1`
* **AndroidX Lifecycle & ViewModel**: `2.8.7`
* **Kotlinx Coroutines**: `1.10.2`
* **Turbine**: `1.2.1`

### 2.3. Properti Gradle (`gradle.properties`)
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.configuration-cache=true
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
android.suppressUnsupportedCompileSdk=37.0
```
> *Catatan*: `android.suppressUnsupportedCompileSdk=37.0` ditambahkan untuk mencegah warning kompatibilitas AGP 9.0.1 saat mengompilasi SDK 37 (Android 17).

---

## 📐 3. Model Matematika & Logika Inti

### 3.1. Log-Distance Path Loss Model (PRD 2.3)
Estimasi jarak fisik dari nilai RSSI dihitung menggunakan formula:
$$d = 10^{\frac{A - \text{RSSI}}{10 \cdot n}}$$
* $d$: Estimasi jarak dalam meter.
* $\text{RSSI}$: Nilai kekuatan sinyal yang terukur ($-\text{dBm}$).
* $A$: Nilai RSSI referensi pada jarak 1 meter (default kalibrasi: $-59\text{ dBm}$).
* $n$: Eksponen rugi propagasi jalur / *path-loss exponent* (default lingkungan dalam ruangan: $2.5$).

### 3.2. Exponential Moving Average (EMA) Smoothing Filter
Untuk meredam interferensi gelombang multipath dan fluktuasi sinyal:
$$\text{RSSI}_{\text{smoothed}} = \alpha \cdot \text{RSSI}_{\text{baru}} + (1 - \alpha) \cdot \text{RSSI}_{\text{sebelumnya}}$$
* Faktor bobot smoothing: $\alpha = 0.35$.

### 3.3. Klasifikasi 6 Zona Kedekatan PRD
1. **Sangat Dekat**: $\ge -30\text{ dBm}$ ($< 1\text{m}$) $\rightarrow$ `#10B981` (Emerald)
2. **Dekat**: $-31\text{ s/d }-50\text{ dBm}$ ($1\text{--}3\text{m}$) $\rightarrow$ `#22C55E` (Spring Green)
3. **Cukup / Baik**: $-51\text{ s/d }-70\text{ dBm}$ ($3\text{--}10\text{m}$) $\rightarrow$ `#F59E0B` (Amber Gold)
4. **Lemah**: $-71\text{ s/d }-80\text{ dBm}$ ($10\text{--}20\text{m}$) $\rightarrow$ `#F97316` (Orange)
5. **Sangat Lemah**: $-81\text{ s/d }-90\text{ dBm}$ ($> 20\text{m}$) $\rightarrow$ `#EF4444` (Rose Red)
6. **Sinyal Hilang (Lost)**: $<-90\text{ dBm}$ $\rightarrow$ `#94A3B8` (Slate)

---

## 🏛 4. Arsitektur Kode & Paket Proyek

```
com.anantyan.pulseble/
├── core/
│   └── DistanceCalculator.kt           # Log-distance path loss, EMA filter, & PRD mapping
├── data/
│   ├── ble/
│   │   ├── BleScanResult.kt            # Raw BLE packet data class
│   │   ├── BleScannerDataSource.kt     # Abstraksi data source scanning
│   │   └── NativeBleScannerDataSource.kt # BluetoothLeScanner via coroutine callbackFlow
│   ├── mock/
│   │   └── MockBleScannerDataSource.kt # Realistic beacon mock untuk emulator
│   ├── local/
│   │   ├── PulseBleDatabase.kt         # Room Database SQLite lokal
│   │   ├── dao/DeviceDao.kt            # Room DAO (insert, update, delete, get, flow)
│   │   ├── entity/DeviceEntity.kt      # Room Entity
│   │   └── mapper/DeviceMapper.kt      # Mapper dua arah Domain <-> Entity
│   └── repository/
│       └── BleDeviceRepositoryImpl.kt  # StateFlow cache, debounce persistence ke Room
├── di/
│   ├── BleModule.kt                    # Hilt binding datasource & repository
│   └── DatabaseModule.kt               # Hilt provider Room DB & DeviceDao
├── domain/
│   ├── model/
│   │   ├── BleDevice.kt                # Rich domain model dengan RSSI history & custom name
│   │   └── ProximityZone.kt            # Enum 6 zona sinyal PRD
│   ├── repository/
│   │   └── BleDeviceRepository.kt      # Repository interface contract
│   └── usecase/
│       └── ScanUseCases.kt             # Domain Use Cases
└── presentation/
    ├── MainActivity.kt                 # Lifecycle observer, BluetoothManager modern, permission flow
    ├── components/
    │   ├── BleDeviceCard.kt            # Kartu perangkat dengan 4-bar signal meter
    │   ├── ConcentricRadarCanvas.kt    # Sweeping radar 360°, blip marker, aura pulse
    │   ├── HapticProximityEngine.kt    # Getaran adaptif berbasis jarak
    │   ├── RenameDeviceDialog.kt       # Modal dialog ganti nama perangkat konsisten M3
    │   ├── RssiSparklineChart.kt       # Grafik sparkline kurva tren RSSI real-time
    │   ├── SignalBadge.kt              # Badge indikator zona tanpa background/outline kaku
    │   └── StatusBanner.kt             # Banner izin bertingkat & pemulihan Bluetooth
    ├── navigation/
    │   ├── NavScreen.kt                # Rute navigasi (Scanner, Radar, History)
    │   └── PulseNavHost.kt             # Single-top Jetpack Navigation anti-looping
    ├── splash/
    │   └── SplashScreen.kt             # Splash screen taktis interaktif & dynamic version
    ├── scanner/
    │   ├── ScannerScreen.kt            # Dashboard Scanner dengan search pill 44dp & snap scroll
    │   ├── ScannerUiState.kt           # UI State Scanner
    │   └── ScannerViewModel.kt         # Logic scanning, filter RSSI, & search
    ├── radar/
    │   ├── RadarScreen.kt              # Layar pelacakan radar detail
    │   ├── RadarUiState.kt             # UI State Radar
    │   └── RadarViewModel.kt           # Logic tracking, Hz update rate, stability jitter
    ├── history/
    │   ├── HistoryScreen.kt            # Layar riwayat perangkat Room DB
    │   ├── HistoryUiState.kt           # UI State History
    │   └── HistoryViewModel.kt         # Logic pencarian, hapus individual, hapus semua
    └── theme/
        ├── Color.kt                    # Token warna taktis & PRD
        ├── Theme.kt                    # Material 3 dark theme
        └── Type.kt                     # Token tipografi
```

---

## 📜 5. Riwayat Evolusi & Keputusan Teknis (Changelog)

### Iterasi Awal (v1.0.0 s/d v1.0.2)
* Scaffold arsitektur Clean Architecture + MVVM + Jetpack Compose.
* Implementasi native `BluetoothLeScanner` menggunakan `callbackFlow`.
* Pembuatan Room Database untuk persistensi riwayat perangkat yang terdeteksi.
* Implementasi model matematika Log-Distance Path Loss dan EMA Filter ($\alpha = 0.35$).
* Pembuatan canvas radar konsentris taktis (*Tactical Concentric Radar Canvas*) dengan 5 cincin jarak.

### Iterasi Penyempurnaan UX (v1.0.3 s/d v1.0.4)
1. **Desain Tanpa Background / Outlined**:
   * Status scanning pada Top App Bar (*SCANNING* / *STANDBY*) diubah menjadi titik radar dan teks dinamis tanpa kotak berbingkai kaku.
   * Indikator level sinyal pada kartu (*Sangat Kuat, Cukup/Baik, Lemah*) diubah menjadi titik dot warna murni tanpa kontainer pill kaku.
2. **Search Bar & Filter Kompak (44dp)**:
   * Mengubah Search Bar menjadi full-pill rounded (`CircleShape`) dengan tinggi simetris 44dp sejajar dengan tombol filter melingkar.
   * Mengimplementasikan animasi snap-scroll (Search Bar otomatis tersembunyi saat scroll ke bawah dan muncul saat scroll ke atas).
3. **Tombol Lacak Lagi (Icon-Only)**:
   * Mengganti tombol aksi pada tab riwayat menjadi tombol lingkaran 36dp beraksen Electric Blue dengan ikon `NearMe` tanpa teks panjang.
4. **Anti Backstack Looping**:
   * Navigasi bottom bar dikonfigurasi dengan `popUpTo(NavScreen.Scanner.route)` dan `singleTop = true`, sehingga tombol Back saat di Scanner langsung menutup aplikasi (*finish to launcher*).
5. **Konsistensi Modal Dialog**:
   * Menyinkronkan `RenameDeviceDialog` dengan `ClearHistoryDialog` menggunakan arsitektur Material 3 `AlertDialog` standar, kontainer `DarkSurface`, dan tombol aksi `TextButton`.
6. **Prioritas Alur Permission**:
   * Memprioritaskan penanganan izin (`!hasPermissions`) di depan pengecekan sakelar Bluetooth (`!isBluetoothEnabled`) untuk mencegah `SecurityException` pada Android 12+.

### Iterasi Terkini (v1.0.5)
1. **Migrasi ke Android 17 (SDK 37)**:
   * `compileSdk = 37`
   * `targetSdk = 37`
   * `versionCode = 5`
   * `versionName = "1.0.5"`
2. **Build-Tools & AGP Suppress Flag**:
   * Memasang SDK Platform `platforms/android-37.0` dan `build-tools/37.0.0`.
   * Menambahkan `android.suppressUnsupportedCompileSdk=37.0` pada `gradle.properties`.
3. **Modernisasi Bluetooth API**:
   * Mengganti method usang `BluetoothAdapter.getDefaultAdapter()` dengan `(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter` di `MainActivity.kt`.
4. **Pengujian Menyeluruh**:
   * 100% unit tests lolos tanpa kegagalan library.
   * E2E live validation pada Xiaomi POCO fisik via Wireless ADB.
5. **Stabilitas Posisi Scroll & Animasi Re-order Real-Time**:
   * Mengimplementasikan pelacakan `isAtTop` dan `anchoredKey` via `snapshotFlow` pada `LazyListState`.
   * Saat berada di posisi paling atas (*top*), list dikunci pada index 0 offset 0 sehingga item dengan RSSI lebih kuat langsung tampil di atas dan item terdorong ke bawah secara alami tanpa ada item tersembunyi di atas layar yang memaksa pengguna scroll ke atas.
   * Saat berada di tengah (*middle/bottom*), posisi scroll terkunci pada item yang sedang dibaca pengguna agar tampilan tidak meloncat saat ranking RSSI berganti.
   * Penyesuaian otomatis di-pause saat `listState.isScrollInProgress` aktif guna mencegah benturan (*scroll fighting*) dengan jari pengguna.
   * Menambahkan `Modifier.animateItem()` pada kartu perangkat sehingga transisi pergeseran naik-turun bergerak mengalir halus.

---

## 🧪 6. Prosedur Uji Coba & Perintah Penting (Runbook)

### Menjalankan Unit Tests:
```powershell
./gradlew testDebugUnitTest
```

### Mengompilasi Debug APK:
```powershell
./gradlew assembleDebug
```
*Hasil APK*: `app/build/outputs/apk/debug/app-debug.apk`

### Menghubungkan & Memasang ke Perangkat Fisik (Wireless ADB):
```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb connect 192.168.1.3:5555
& $adb -s 192.168.1.3:5555 install -r app\build\outputs\apk\debug\app-debug.apk
```

### Memeriksa Versi Paket di Device:
```powershell
& $adb -s 192.168.1.3:5555 shell "dumpsys package com.anantyan.pulseble | grep -E 'versionCode|versionName|targetSdk'"
```

---
*Playbook ini dikelola sebagai sumber kebenaran teknis proyek PulseBLE.*
