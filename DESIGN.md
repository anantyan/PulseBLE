# PulseBLE Design System (DESIGN.md) 🎨
**Tactical Dark Minimalist Interface & Micro-Motion System**

Dokumen ini mendefinisikan sistem desain visual, token warna, tipografi, komponen UI, animasi, dan sensasi getar (*haptics*) untuk aplikasi **PulseBLE** versi **v1.0.5**.

---

## 🌌 1. Filosofi Desain
PulseBLE dirancang dengan pendekatan **"Tactical Radar & Ultra-Clean Minimalist"**:
1. **Utility-First**: Informasi sinyal mentah (dBm, estimasi jarak, frekuensi Hz) disajikan secara presisi tanpa dekorasi berlebihan.
2. **High-Contrast Dark Canvas**: Dominasi warna gelap pekat (`#0B0F17` dan `#111827`) untuk meminimalkan konsumsi daya layar OLED/AMOLED sekaligus memberi nuansa instrumen militer/radar taktis modern.
3. **No-Slop & Borderless Accents**: Elemen status (seperti indikator radar *Scanning*/*Standby* dan level sinyal) tidak menggunakan kotak berbingkai kaku (*no outline box, no chunky background*), melainkan berupa *blip dot* dinamis dengan teks tipografi kontras tinggi.
4. **Organic Micro-Motion**: Sapuan berkas radar halus (*360° sweeping beam*), getaran aura target (*pulsating aura*), dan *snap scroll header* yang mulus.

---

## 🎨 2. Palet Warna & Token (Color Tokens)

### 2.1. Canvas & Surface
| Token Name | Hex Code | Deskripsi & Penggunaan |
| :--- | :--- | :--- |
| `DarkBackground` | `#0B0F17` | Latar belakang kanvas aplikasi utama. |
| `DarkSurface` | `#111827` | Kontainer kartu perangkat, bottom sheet, dan modal dialog. |
| `DarkCardBorder` | `#1F2937` | Garis tepi halus (1dp) untuk membedakan kontainer kartu di atas background. |
| `DarkCardElevated` | `#1E293B` | Kontainer chip filter dan input text field. |
| `DarkDivider` | `#1E293B` | Garis pemisah antar seksi data. |

### 2.2. Aksen Taktis (Brand Accents)
| Token Name | Hex Code | Deskripsi & Penggunaan |
| :--- | :--- | :--- |
| `PrimaryCyan` | `#00E5FF` | Aksen futuristik utama (*radar sweep beam*, sorotan data penting). |
| `SecondaryBlue` | `#3B82F6` | Tombol aksi primer, tombol *Track Again*, dan icon navigasi aktif. |
| `ElectricBlue` | `#2563EB` | Aksen status aktif dan tombol aksi konfirmasi dialog. |
| `RadarGridLine` | `Color(0xFF1E3A5F).copy(alpha = 0.35f)` | Lingkaran konsentris dan sumbu silang pada radar canvas. |

### 2.3. PRD 6-Zone Signal Accents (Section 2.3)
Sistem warna indikator sinyal dan estimasi jarak terikat secara presisi dengan spesifikasi PRD:

| Zona Jarak (PRD) | Rentang RSSI | Hex Code | Nama Warna | Representasi Visual |
| :--- | :--- | :--- | :--- | :--- |
| **Zone 1: Sangat Dekat** | $\ge -30\text{ dBm}$ ($< 1\text{m}$) | `#10B981` | *Emerald Green* | Target berada tepat di genggaman / sangat dekat. |
| **Zone 2: Dekat** | $-31\text{ s/d }-50\text{ dBm}$ ($1\text{--}3\text{m}$) | `#22C55E` | *Spring Green* | Sinyal kuat, target dalam ruangan yang sama. |
| **Zone 3: Cukup / Baik** | $-51\text{ s/d }-70\text{ dBm}$ ($3\text{--}10\text{m}$) | `#F59E0B` | *Amber Gold* | Sinyal stabil, jarak sedang. |
| **Zone 4: Lemah** | $-71\text{ s/d }-80\text{ dBm}$ ($10\text{--}20\text{m}$) | `#F97316` | *Vibrant Orange* | Sinyal melemah, mendekati batas ruangan. |
| **Zone 5: Sangat Lemah** | $-81\text{ s/d }-90\text{ dBm}$ ($> 20\text{m}$) | `#EF4444` | *Rose Red* | Sinyal kritis, paket data mulai terputus-putus. |
| **Zone 6: Hilang (Lost)** | $<-90\text{ dBm}$ | `#94A3B8` | *Muted Slate* | Sinyal terputus atau target di luar jangkauan scan. |

---

## 🔤 3. Tipografi (Typography System)

Aplikasi menggunakan perpaduan **Monospace** untuk metrik numerik/data teknis dan **Sans-Serif Modern (Inter / Roboto)** untuk label antarmuka:

| Tipe Gaya | Ukuran | Weight | Letter Spacing | Penggunaan |
| :--- | :--- | :--- | :--- | :--- |
| **Display Tactical** | 28sp | Bold (`W700`) | `0.1sp` | Header judul utama, metrik jarak radar utama. |
| **Headline Medium** | 20sp | SemiBold (`W600`) | `0.15sp` | Judul Top App Bar, nama perangkat pada kartu. |
| **Title Medium** | 16sp | Medium (`W500`) | `0.15sp` | Subjudul seksi, judul dialog modal. |
| **Body Large** | 14sp | Normal (`W400`) | `0.25sp` | Deskripsi status, teks penjelasan bantuan. |
| **Data Monospace** | 13sp–15sp | SemiBold (`W600`) | `0.5sp` | RSSI mentah (`-62 dBm`), MAC Address (`42:DA:...`), frekuensi Hz. |
| **Caption Minimal** | 11sp–12sp | Medium (`W500`) | `0.4sp` | Keterangan waktu relatif (*"2 menit lalu"*), indikator level sinyal. |

---

## 🧩 4. Spesifikasi Komponen (Component Tokens)

### 4.1. Top App Bar & Status Radar Dinamis
* **Tinggi Bar**: 64dp dengan padding horizontal 16dp.
* **Indikator Status (Scanning / Standby)**:
  * **Tanpa Kotak Border/Background**: Cukup titik radar (`size = 8dp`) dengan warna dinamis (Hijau berkedip saat *SCANNING*, Abu-abu lembut saat *STANDBY*).
  * **Tipografi Status**: `Caption` 12sp Bold warna abu-abu/hijau (`#22C55E` / `#9CA3AF`).
* **Tombol Scan/Hentikan**: Tombol pill Material 3 kompak dengan rounded radius 20dp, ikon aksi, dan animasi transisi warna.

### 4.2. Search Bar & Filter Controls
* **Tinggi Simetris**: 44dp persis antara Search Bar dan Tombol Filter (mencegah *layout imbalance*).
* **Search Bar**: Full-Pill Rounded (`CircleShape` / radius 24dp), latar belakang `DarkSurface`, teks pencarian kontras, ikon pencarian dan tombol pembersih (*clear text*).
* **Tombol Filter**: Lingkaran penuh 44dp (`CircleShape`) sejajar di samping kanan kolom pencarian.
* **Snap Scroll Animation**:
  * Ketika pengguna scroll ke bawah (*scroll down*), Search Bar & Filter meluncur tersembunyi secara halus (*animated height & alpha = 0*).
  * Ketika pengguna scroll ke atas (*scroll up*), kontrol kembali muncul (*alpha = 1*), menghemat area pandang layar hingga 25%.

### 4.3. Tactical BleDeviceCard
* **Kontainer**: Rounded corner 16dp, latar `DarkSurface`, border 1dp `DarkCardBorder`.
* **Indikator Sinyal**: Titik status 6dp berwarna sesuai 6 zona PRD (tanpa bingkai kotak) berdampingan dengan label teks.
* **Signal Meter Bars**: 4 bar vertikal dinamis (tinggi 6dp s/d 18dp) yang terisi proporsional berdasarkan RSSI.
* **Aksi History Track-Again**: Tombol melingkar `36dp` berwarna `SecondaryBlue` dengan ikon panah pelacak (`Icons.Default.NearMe`) tanpa teks, memaksimalkan kerapian visual.

### 4.4. Consistent M3 Modal Dialogs
* Seluruh dialog modal (`RenameDeviceDialog` dan `ClearHistoryDialog`) mengikuti arsitektur seragam:
  * Kontainer: `DarkSurface` dengan rounded radius 24dp.
  * Tombol Aksi: Menggunakan `TextButton` standar Material 3 ("Batal" dan "Simpan" / "Hapus Semua") di pojok kanan bawah.
  * Teks Aksi: Warna tegas (`SecondaryBlue` atau `Rose Red`) dengan bobot `FontWeight.Bold`.

---

## 🌀 5. Animasi & Canvas Taktis (Micro-Motion)

1. **Radar Sweeping Beam**:
   * Sudut rotasi: `0°` hingga `360°` tak terhingga (*infinite repeatable*).
   * Durasi siklus: `2500ms` dengan `LinearEasing`.
   * Shader: `SweepGradient` dari transparan hingga `PrimaryCyan.copy(alpha = 0.45f)`.
2. **Target Blip Aura Pulse**:
   * Radius denyut target: `8dp` membesar hingga `22dp`.
   * Alpha denyut: `0.7f` meredup hingga `0f`.
   * Durasi siklus: `1200ms` dengan `EaseOutQuad`.
3. **RSSI Sparkline Graph**:
   * Menggunakan bezier kurva halus (*cubicTo*) menghubungkan 25 titik riwayat kekuatan sinyal terakhir.
   * Dilengkapi efek gradient fill vertikal di bawah garis grafik.

---

## 📳 6. Sensasi Getar Taktil (Haptic Proximity Engine)

Mesin haptik memberikan respon fisik dinamis saat pengguna mendekati target BLE:
* **Zona 1 (< 1m)**: Pola denyut getaran cepat dan tajam (setiap `300ms`, `HapticFeedbackType.LongPress`).
* **Zona 2 (1–3m)**: Pola denyut sedang (setiap `700ms`, `HapticFeedbackType.TextHandleMove`).
* **Zona 3 (3–10m)**: Denyut ketukan ringan (setiap `1500ms`).
* **Zona 4–6 (> 10m / Lost)**: Haptic nonaktif untuk kenyamanan pengguna.

---
*Dokumen ini merupakan acuan baku antarmuka PulseBLE v1.0.5.*
