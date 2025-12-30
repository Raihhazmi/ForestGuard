# 🌲 ForestGuard - Citizen Science Forest Monitoring

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Appwrite](https://img.shields.io/badge/Backend-Appwrite-FD366E?style=for-the-badge&logo=appwrite&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

![ForestGuard Banner](app/src/main/res/drawable/logo_forest_light.jpg)

**ForestGuard** adalah aplikasi mobile berbasis komunitas (*Citizen Science*) yang dirancang untuk pelestarian lingkungan. Aplikasi ini memberdayakan pengguna untuk melaporkan kondisi hutan, memantau titik api, dan berinteraksi dengan sesama relawan lingkungan secara *real-time*.

Dibangun dengan arsitektur modern **MVVM** menggunakan **Android Jetpack Compose** dan didukung oleh keandalan backend **Appwrite**.

---

## ✨ Fitur Unggulan

### 🛡️ Fitur Utama
* 📸 **Lapor Cepat & Akurat:** Pelaporan insiden dengan bukti foto, deskripsi, tingkat keparahan, dan **Geo-Tagging Otomatis**.
* 🗺️ **Peta Interaktif:** Visualisasi persebaran laporan dan kondisi hutan dalam peta real-time.
* 🌙 **Mode Gelap (Dark Mode):** UI adaptif yang nyaman di mata, sinkron dengan pengaturan sistem.

### 🤝 Komunitas (Social Feed)
* **Feed Laporan:** Timeline laporan terkini dari seluruh relawan.
* **Interaksi Sosial:**
    * ❤️ **Like:** Dukungan instan dengan animasi toggle.
    * 💬 **Diskusi:** Sistem komentar dan *reply* antar pengguna.
    * ✅ **Verifikasi:** Tanda validasi untuk laporan yang terpercaya (Blue Tick).

### 🚀 User Experience (UX)
* **Splash Screen:** Animasi pembuka yang elegan dan branding kuat.
* **Onboarding:** Pengenalan fitur interaktif menggunakan DataStore Preferences.
* **Secure Auth:** Login dan Register aman terintegrasi dengan Appwrite Auth.

---

## 📸 Screenshots

| Splash & Onboarding | Login & Register | Home Feed |
|:---:|:---:|:---:|
| <img src="screenshots/splash.jpg" width="200"/> | <img src="screenshots/login.jpg" width="200"/> | <img src="screenshots/home.jpg" width="200"/> |

| Detail Laporan | Fitur Komentar | Dark Mode |
|:---:|:---:|:---:|
| <img src="screenshots/report.jpg" width="200"/> | <img src="screenshots/comments.jpg" width="200"/> | <img src="screenshots/darkmode.jpg" width="200"/> |

> *Catatan: Ganti path gambar di atas sesuai dengan nama file screenshot kamu.*

---

## 🛠️ Tech Stack & Libraries

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Navigation:** Jetpack Navigation Compose
* **Backend as a Service:** Appwrite (Database, Auth, Storage)
* **Concurrency:** Kotlin Coroutines & Flow
* **Image Loading:** Coil
* **Local Storage:** DataStore Preferences
* **Maps:** Google Maps Compose / OSM

---

## 📂 Struktur Proyek

Aplikasi ini diorganisir menggunakan Clean Architecture sederhana dengan pemisahan *concern* yang jelas:

```text
com.forestguard.app
├── data/                # Data Layer (Repositories & API Handling)
│   ├── AppwriteClient.kt
│   ├── AuthRepository.kt
│   ├── ReportRepository.kt
│   └── UserPreferences.kt
├── model/               # Domain Models (Data Classes)
│   ├── User.kt
│   ├── Report.kt
│   └── Comment.kt
├── ui/                  # UI Layer (Jetpack Compose)
│   ├── screen/          # Screen-level Composables
│   │   ├── SplashScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── CommunityScreen.kt
│   │   ├── ReportScreen.kt
│   │   ├── MapsScreen.kt
│   │   ├── ProfileScreen.kt
│   │   └── ForestGuardApp.kt  # Navigation Graph
│   └── theme/           # Design System
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt      # Application Entry Point
```
---
⚙️ **Konfigurasi Backend (Appwrite)**  
Untuk menjalankan aplikasi ini, Anda perlu menyiapkan project Appwrite dengan konfigurasi berikut:

---

### 🗃️ Database ID: `forestguard_db`

#### 1. Collection: `reports`  
Menyimpan data laporan kejadian hutan.

| Key         | Type    | Required | Array | Default     |
|-------------|---------|----------|-------|-------------|
| userId      | String  | Yes      | No    | —           |
| description | String  | Yes      | No    | —           |
| severity    | Integer | Yes      | No    | —           |
| imageId     | String  | Yes      | No    | —           |
| latitude    | Float   | Yes      | No    | —           |
| longitude   | Float   | Yes      | No    | —           |
| status      | String  | No       | No    | `"Pending"` |
| likedBy     | String  | No       | Yes   | —           |
| commentCount| Integer | No       | No    | `0`         |

#### 2. Collection: `comments`  
Menyimpan interaksi diskusi pada laporan.

| Key        | Type    | Required | Array | Default |
|------------|---------|----------|-------|---------|
| reportId   | String  | Yes      | No    | —       |
| userId     | String  | Yes      | No    | —       |
| userName   | String  | Yes      | No    | —       |
| content    | String  | Yes      | No    | —       |
| avatarId   | String  | No       | No    | —       |

---

### 📦 Storage Bucket  

- **Bucket ID**: `report_photos`  
- **Permissions**: Pastikan role `Any` memiliki akses `read` dan `write`.

---

## 📦 Cara Instalasi

1.  **Clone Repository:**

    ```bash

    git clone [https://github.com/username/ForestGuard.git](https://github.com/username/ForestGuard.git)

     ```

3.  **Buka di Android Studio:**

    Buka project dan biarkan Gradle melakukan sinkronisasi.

5.  **Konfigurasi API Key:**

    Buka file `data/AuthRepository.kt` dan `data/ReportRepository.kt`. Sesuaikan variabel berikut dengan Project Appwrite Anda:

    ```kotlin

    private val PROJECT_ID = "YOUR_APPWRITE_PROJECT_ID"
    private val DATABASE_ID = "YOUR_DATABASE_ID"
    private val BUCKET_ID = "YOUR_BUCKET_ID"

    ```

6.  **Run:**
    Jalankan aplikasi di Emulator atau Device fisik.

---

## 📝 Lisensi

Project ini dikembangkan oleh **Muhammad Raihan Azmi** untuk tujuan pendidikan dan pengembangan teknologi lingkungan.

---
<div align="center">
  <strong>Dipersembahkan oleh Tim ForestGuard 🌲</strong><br>
  <em>Jaga Hutan, Jaga Masa Depan.</em>
</div>
