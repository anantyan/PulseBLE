# PulseBLE 📡
**Real-Time Bluetooth LE Proximity Tracker & Tactical Radar**

[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-brightgreen.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-navy.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-orange.svg)]()
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)]()

> **Study Case: Mobile Engineer Take-Home Challenge**  
> PulseBLE adalah aplikasi Android modern untuk mendeteksi sinyal periklanan (*advertising packets*) perangkat Bluetooth Low Energy (BLE) di sekitar secara *real-time*, memantau stabilitas kekuatan sinyal (RSSI), dan melacak kedekatan target menggunakan visualisasi radar konsentris taktis (*Tactical Concentric Radar View*).

---

## 📑 Daftar Isi
- [Fitur Utama](#-fitur-utama)
- [Arsitektur & Pola Desain](#-arsitektur--pola-desain)
- [Model Matematika & Pemetaan Jarak PRD](#-model-matematika--pemetaan-jarak-prd)
- [Struktur Proyek](#-struktur-proyek)
- [Teknologi & Library yang Digunakan](#-teknologi--library-yang-digunakan)
- [Instruksi Setup & Menjalankan Aplikasi](#-instruksi-setup--menjalankan-aplikasi)
- [Dual-Mode: Real Hardware vs Demo Emulator](#-dual-mode-real-hardware-vs-demo-emulator)
- [Asumsi Teknis & Batasan (Known Issues)](#-asumsi-teknis--batasan-known-issues)
- [Build Artifact (APK)](#-build-artifact-apk)

---

## 🌟 Fitur Utama

### 1. Dashboard Utama (Scanner)
- **Real-Time Scanning**: Mendeteksi sinyal beacon BLE aktif dengan native `BluetoothLeScanner` menggunakan coroutine `callbackFlow`.
- **Informasi Lengkap**: Menampilkan Nama Perangkat, MAC Address/UUID, RSSI mentah (*raw dBm*), estimasi jarak (*meter*), dan meteran sinyal 4-bar visual.
- **Kontrol Manual**: Tombol *Mulai Scan* dan *Hentikan Scan* yang responsif.
- **Filter & Pengurutan Cerdas (PRD 2.2)**:
  - Otomatis mengurutkan daftar berdasarkan sinyal terkuat (RSSI tertinggi descending).
  - Kolom pencarian instan berdasarkan **Nama Perangkat** atau **MAC Address**.
  - Slider ambang batas sinyal minimum (*Minimum RSSI Threshold Filter*, misal $\ge -80\text{ dBm}$).

### 2. Detail Pelacakan (Tactical Radar View)
- **Concentric Radar Canvas**: Visualisasi radar taktis dengan 5 lingkaran jarak konsentris (<1m, 1-3m, 3-10m, 10-20m, >20m), animasi sapuan berkas radar berputar (*sweeping beam*), dan penanda target (*blip marker*) dengan efek denyut aura (*pulsating aura*).
- **Indikator Kategori Sinyal Dinamis**: Badge visual yang berubah warna dan bentuk secara dinamis sesuai kategori zona kedekatan PRD.
- **Real-Time RSSI Sparkline Graph**: Grafik riwayat tren kekuatan sinyal 25 paket terakhir untuk memantau apakah perangkat sedang mendekat atau menjauh.
- **Metrik Kestabilan Koneksi**: Pemantauan frekuensi penerimaan paket (*Update Rate* dalam Hz) dan tingkat fluktuasi sinyal (*Signal Jitter*).
- **Haptic Proximity Ping**: Getaran taktil adaptif yang berdenyut lebih rapat dan intens saat target mendekat (<1 meter atau 1-3 meter).

### 3. Riwayat Perangkat (History Log)
- **Penyimpanan Lokal Persisten**: Menggunakan database SQLite lokal (**Room Database**) untuk menyimpan riwayat seluruh perangkat yang pernah terdeteksi agar tidak hilang saat aplikasi ditutup.
- **Waktu Relatif**: Menampilkan waktu penemuan dalam format ramah pengguna (*"Baru saja"*, *"3 menit lalu"*, *"Kemarin"*).
- **Aksi Track Again**: Pengguna dapat mengetuk item riwayat kapan saja untuk langsung membuka Radar View dan melanjutkan pelacakan perangkat tersebut.
- **Manajemen Riwayat**: Opsi hapus item individual dan dialog konfirmasi *Hapus Semua Riwayat*.

### 4. Error Handling & Resilience
- **Bluetooth State Monitor**: Menggunakan `BroadcastReceiver` untuk memantau sakelar Bluetooth fisik secara *real-time*. Jika Bluetooth dimatikan pengguna, banner peringatan muncul dengan tombol 1-klik untuk menyalakannya kembali.
- **Runtime Permissions Graceful Handling**: Mendukung izin modern Android 12+ (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`) dan Android lawas (`ACCESS_FINE_LOCATION`).
- **Lifecycle-Aware Scanning (PRD 4.3)**: Terintegrasi dengan `DefaultLifecycleObserver`. Pemindaian otomatis di-**Pause** saat aplikasi masuk ke latar belakang (*background* / `onStop`) guna menghemat baterai, dan otomatis di-**Resume** saat aplikasi dibuka kembali (*foreground* / `onResume`).

---

## 🏛 Arsitektur & Pola Desain

Aplikasi dibangun menggunakan prinsip **Clean Architecture** dan **MVVM (Model-View-ViewModel)** dengan pemisahan dependensi secara ketat:

```
                  ┌─────────────────────────────────────────┐
                  │           PRESENTATION LAYER            │
                  │   Jetpack Compose UI  •  ViewModels     │
                  │   (ScannerScreen, RadarScreen, History) │
                  └────────────────────┬────────────────────┘
                                       │ (StateFlow)
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │              DOMAIN LAYER               │
                  │   Use Cases  •  Models  •  Repository   │
                  │   (StartScan, StopScan, TrackDevice)    │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │               DATA LAYER                │
                  │  BleDeviceRepositoryImpl                │
                  │  ┌──────────────────┐ ┌───────────────┐ │
                  │  │ Native & Mock    │ │ Room Database │ │
                  │  │ BLE Scanner Flow │ │ (DeviceDao)   │ │
                  │  └──────────────────┘ └───────────────┘ │
                  └─────────────────────────────────────────┘
```

### Mengapa Arsitektur Ini Dipilih?
1. **Clean Architecture**: Memisahkan logika bisnis (Domain) dari detail implementasi Android (BLE hardware, Room SQLite, dan Compose UI). Sangat mempermudah pembuatan Unit Tests tanpa ketergantungan pada Android OS mocks.
2. **High-Throughput StateFlow Buffer**: Paket periklanan BLE di dunia nyata dapat masuk puluhan kali per detik (*high burst rate*). Menulis setiap paket langsung ke SQLite Room akan menyebabkan I/O churn dan penurunan frame rate (*jank*). Repository menggunakan in-memory cache StateFlow untuk UI 60fps, dikombinasikan dengan *debounced background flusher* (2 detik) ke Room DB.
3. **Dependency Injection dengan Dagger Hilt**: Standar resmi Google Android yang menjamin *loose coupling*, mempermudah mock injection saat pengujian, dan mengelola lifecycle dependensi secara otomatis.

---

## 📐 Model Matematika & Pemetaan Jarak PRD

### 1. Log-Distance Path Loss Model
Kekuatan sinyal BLE berbanding terbalik secara logaritmik terhadap jarak fisik. Estimasi jarak dihitung menggunakan formula propagasi radio:

$$d = 10^{\frac{\text{TxPower} - \text{RSSI}}{10 \cdot n}}$$

- $\text{TxPower}$: Kekuatan sinyal acuan pada jarak 1 meter (default terkalibrasi: $-59\text{ dBm}$).
- $\text{RSSI}$: Nilai kekuatan sinyal yang diterima perangkat (dBm).
- $n$: Eksponen rugi jalur lingkungan (*path loss exponent*, default ruangan indoor: $2.0$).

### 2. Exponential Moving Average (EMA) Low-Pass Filter
Sinyal radio BLE di dunia nyata sangat rentan terhadap *multipath fading*, pantulan dinding, dan interferensi frekuensi 2.4 GHz, yang menghasilkan fluktuasi *noise* $\pm 10\text{--}15\text{ dBm}$.  
Untuk menjaga agar jarum radar dan grafik sparkline tidak berkedip-kedip secara agresif (*jitter*), kami menerapkan filter penghalus frekuensi rendah:

$$S_t = \alpha \cdot Y_t + (1 - \alpha) \cdot S_{t-1}$$

Dengan faktor bobot $\alpha = 0.35$, filter memberikan keseimbangan optimal antara stabilitas pembacaan dan ketanggapan terhadap perpindahan fisik pengguna.

### 3. Pemetaan 6 Kategori Sinyal PRD (Section 2.3)
Estimasi jarak dikelompokkan secara ketat mengikuti tabel spesifikasi PRD:

| Nilai RSSI (dBm) | Kategori Sinyal | Perkiraan Jarak (Meter) | Aksen Visual |
| :--- | :--- | :--- | :--- |
| **$-10\text{ s/d }-30\text{ dBm}$** | Sangat Kuat (Sangat Dekat) | $< 1\text{ meter}$ | Emerald Green (`#10B981`) |
| **$-30\text{ s/d }-50\text{ dBm}$** | Kuat (Dekat) | $1\text{ -- }3\text{ meter}$ | Spring Green (`#22C55E`) |
| **$-50\text{ s/d }-70\text{ dBm}$** | Cukup / Baik | $3\text{ -- }10\text{ meter}$ | Amber Gold (`#F59E0B`) |
| **$-70\text{ s/d }-80\text{ dBm}$** | Lemah | $10\text{ -- }20\text{ meter}$ | Orange (`#F97316`) |
| **$-80\text{ s/d }-90\text{ dBm}$** | Sangat Lemah / Putus-putus | $> 20\text{ meter}$ (Batas jangkauan) | Rose Red (`#EF4444`) |
| **$<-90\text{ dBm}$** | Sinyal Hilang (Lost) | Terputus / Di luar jangkauan | Muted Slate (`#94A3B8`) |

---

## 📁 Struktur Proyek

```
com.anantyan.pulseble/
├── core/
│   └── DistanceCalculator.kt           # Log-distance path loss, EMA filter, & PRD mapping
├── data/
│   ├── ble/
│   │   ├── BleScanResult.kt            # Raw scan packet model
│   │   ├── BleScannerDataSource.kt     # Data source interface
│   │   └── NativeBleScannerDataSource.kt # BluetoothLeScanner + callbackFlow
│   ├── mock/
│   │   └── MockBleScannerDataSource.kt # Realistic beacon generator for emulator
│   ├── local/
│   │   ├── PulseBleDatabase.kt         # Room Database
│   │   ├── dao/DeviceDao.kt            # Room DAO (CRUD & flow queries)
│   │   ├── entity/DeviceEntity.kt      # Room entity
│   │   └── mapper/DeviceMapper.kt      # Domain <-> Entity mapper
│   └── repository/
│       └── BleDeviceRepositoryImpl.kt  # StateFlow cache + debounced Room persistence
├── di/
│   ├── BleModule.kt                    # Hilt binding for repository
│   └── DatabaseModule.kt               # Hilt provider for Room DB & DAO
├── domain/
│   ├── model/
│   │   ├── BleDevice.kt                # Rich domain model with RSSI history
│   │   └── ProximityZone.kt            # 6 PRD Proximity Zones enum
│   ├── repository/
│   │   └── BleDeviceRepository.kt      # Repository contract
│   └── usecase/
│       └── ScanUseCases.kt             # Domain Use Cases
└── presentation/
    ├── MainActivity.kt                 # Lifecycle observer, permissions, & bluetooth receivers
    ├── components/
    │   ├── BleDeviceCard.kt            # Tactical card with signal bars
    │   ├── ConcentricRadarCanvas.kt    # Sweeping radar canvas & blip pulse
    │   ├── HapticProximityEngine.kt    # Tactile proximity vibration
    │   ├── RssiSparklineChart.kt       # Real-time RSSI stability sparkline
    │   ├── SignalBadge.kt              # PRD zone badge
    │   └── StatusBanner.kt             # Bluetooth & permission recovery banner
    ├── navigation/
    │   ├── NavScreen.kt                # Routes & bottom nav items
    │   └── PulseNavHost.kt             # Jetpack Navigation Compose host
    ├── scanner/
    │   ├── ScannerScreen.kt            # Dashboard Scanner UI
    │   ├── ScannerUiState.kt           # MVI/MVVM UI state
    │   └── ScannerViewModel.kt         # Search, RSSI slider filter, & sorting logic
    ├── radar/
    │   ├── RadarScreen.kt              # Detail Pelacakan (Radar View) UI
    │   ├── RadarUiState.kt             # Radar UI state
    │   └── RadarViewModel.kt           # Target tracking, Hz rate, & stability logic
    ├── history/
    │   ├── HistoryScreen.kt            # History Log UI with Track Again action
    │   ├── HistoryUiState.kt           # History UI state
    │   └── HistoryViewModel.kt         # Room history query & clear logic
    └── theme/
        ├── Color.kt                    # Tactical dark & PRD signal colors
        ├── Theme.kt                    # Material 3 Edge-to-Edge Theme
        └── Type.kt                     # Typography tokens
```

---

## 🛠 Teknologi & Library yang Digunakan

| Library / Komponen | Versi | Alasan Pemilihan |
| :--- | :--- | :--- |
| **Kotlin** | `2.3.20` | Bahasa modern dengan fitur coroutines flow tingkat lanjut dan type-safety penuh. |
| **Jetpack Compose + Material 3** | `BOM 2024.12` | Framework UI deklaratif modern resmi Google untuk performa render 60fps tanpa XML layout. |
| **Dagger Hilt** | `2.60.1` | Dependency Injection standar industri dengan KSP code generation. |
| **KSP (Kotlin Symbol Processing)** | `2.3.12` | Alternatif kapt modern dengan kecepatan kompilasi hingga 2x lebih cepat. |
| **Room Database** | `2.7.2` | Abstraksi SQLite tipe aman resmi Google untuk persistence lokal riwayat perangkat. |
| **Jetpack Navigation Compose** | `2.8.9` | Manajemen navigasi deklaratif antar layar dengan parameter transfer tipe aman. |
| **Kotlinx Coroutines & Flow** | `1.10.2` | Pengelolaan background threading reaktif untuk streaming data BLE tanpa blocking UI thread. |
| **Turbine & JUnit4** | `1.2.1` | Pengujian reaktif untuk state flow dan unit test matematika. |

---

## 🚀 Instruksi Setup & Menjalankan Aplikasi

### Kebutuhan Sistem:
- **Android Studio**: Ladybug / Meerkat (atau versi terbaru).
- **JDK**: Java 17 atau 21.
- **Android SDK**: `compileSdk = 36`, `targetSdk = 34`, `minSdk = 24` (Mendukung Android 7.0 Nougat hingga Android 14+).

### Langkah-langkah:
1. **Clone Repository**:
   ```bash
   git clone https://github.com/anantyan/PulseBLE.git
   cd PulseBLE
   ```
2. **Buka di Android Studio**:
   Buka direktori proyek `PulseBLE` di Android Studio dan biarkan Gradle menyelesaikan sync dependencies.
3. **Jalankan Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```
   *Seluruh unit test matematika (DistanceCalculatorTest), mapper (DeviceMapperTest), repository (BleDeviceRepositoryTest), dan viewmodel (ScannerViewModelTest) akan dieksekusi.*
4. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   File APK siap diuji akan dihasilkan pada: `app/build/outputs/apk/debug/app-debug.apk`.
5. **Jalankan di Perangkat**:
   Pilih target perangkat (HP Android fisik atau Emulator) lalu klik tombol **Run** (`Shift + F10`).

---

## 📡 Hardware & E2E Validation

Aplikasi telah diuji dan divalidasi langsung pada perangkat keras fisik **Xiaomi POCO (Android 16 / API 36)** via ADB:

- **100% Native BLE Stack**: Terintegrasi langsung dengan Android `BluetoothLeScanner` native, menangkap ribuan advertising packets dari beragam periferal (earphone TWS, laptop, smartwatch, beacons, HP lain).
- **Resolver Nama Cerdas**: Resolusi nama multi-layer (GATT Local Name, Scan Record AD Structures, Custom User Alias) dengan dukungan ganti nama kustom secara persisten.
- **Hemat Daya & Lifecycle Resilient**: Otomatis menghentikan pemindaian di latar belakang (`onStop`) untuk mencegah kebocoran baterai (*zero background drain*).
- **Uji Coba E2E Lengkap**:
  - **TC-1 Runtime Permissions**: LULUS (Android 12–16 BLE Scan/Connect flow).
  - **TC-2 BLE Discovery**: LULUS (Pencarian real-time, RSSI sorting & dynamic refresh).
  - **TC-3 Detail & Signal Gauge**: LULUS (Akurasi kalkulasi jarak, formula path loss, nama kustom).
  - **TC-4 Tactical Radar Canvas**: LULUS (Sweep radar 360°, zona sinyal interaktif, pelacakan target).
  - **TC-5 Room Database Persistence**: LULUS (Penyimpanan lokal SQLite, filter riwayat, process-death resilience, dan hapus riwayat).
  - **TC-6 Lifecycle & Battery Safety**: Sedang Berjalan (Background pause/resume & rotation resilience).

---

## ⚠️ Asumsi Teknis & Batasan (Known Issues)

1. **Akurasi RSSI di Lingkungan Fisik**:
   - *Keterbatasan*: Sinyal Bluetooth 2.4 GHz dipengaruhi oleh absorpsi tubuh manusia, dinding beton, dan interferensi Wi-Fi. Oleh karena itu, estimasi jarak berbasis RSSI merupakan pendekatan (*approximation*), bukan posisi absolut berbasis Ultra-Wideband (UWB).
   - *Solusi*: Kami menerapkan **EMA Low-Pass Filter** dan mengelompokkannya ke dalam zona rentang diskrit sesuai PRD untuk memberikan pembacaan yang stabil dan bermakna bagi pengguna.
2. **Perilaku Izin pada Android 12+ vs Android Lawas**:
   - *Keterbatasan*: Android 12 (API 31+) memperkenalkan izin `BLUETOOTH_SCAN` dan `BLUETOOTH_CONNECT`, sedangkan Android 11 ke bawah mewajibkan `ACCESS_FINE_LOCATION` dan GPS harus aktif.
   - *Solusi*: Aplikasi secara otomatis mendeteksi versi OS perangkat dan meminta bundel izin yang tepat sesuai level API.
3. **OS Throttling pada Background Scanning**:
   - *Keterbatasan*: Android OS menerapkan pembatasan agresif terhadap pemindaian BLE tanpa henti di background untuk mencegah boros baterai.
   - *Solusi*: Mengikuti best-practice Android modern dan poin PRD 4.3, aplikasi mem-pause pemindaian saat aplikasi di-*minimize* dan menyambungnya kembali secara otomatis saat aplikasi kembali ke latar depan.

---

## 📦 Build Artifact (APK)

Build artifact APK debug telah dikompilasi dan siap diuji langsung tanpa perlu build ulang:
- **Lokasi File**: `app/build/outputs/apk/debug/app-debug.apk`
- **Application ID**: `com.anantyan.pulseble`
- **Ukuran File**: $\approx 27.3\text{ MB}$

---
*Dibuat dengan dedikasi tinggi terhadap kualitas kode, arsitektur bersih, dan pengalaman pengguna interaktif.*
