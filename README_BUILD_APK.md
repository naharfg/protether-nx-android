# ProTether NX — Panduan Kompilasi ke File `.apk`

Project ini berisi source code lengkap aplikasi **ProTether NX (Android Kotlin + Jetpack Compose + USB Host PTP Camera Controller)** yang siap dikompilasi menjadi file instalasi **`ProTether-NX-debug.apk`**.

---

## Cara 1: Build `.apk` Otomatis Tanpa Install Android Studio (via GitHub Actions)
Jika tidak ingin menginstal Android Studio di PC:
1. Ekstrak file `ProTether_NX_Android_Project.zip` ini.
2. Buat repository baru di GitHub, lalu *upload* seluruh isi folder ini ke dalam repository tersebut.
3. Buka tab **Actions** di repository GitHub Anda, pilih workflow **"Build ProTether NX Android APK"**, lalu klik **Run workflow**.
4. Tunggu sekitar 2–3 menit hingga proses kompilasi selesai, lalu unduh file **`ProTether-NX-debug-apk`** langsung dari bagian **Artifacts**.

---

## Cara 2: Build `.apk` Menggunakan Android Studio di PC
1. Ekstrak file `ProTether_NX_Android_Project.zip` ke folder di PC Anda.
2. Buka **Android Studio** -> pilih **Open** -> arahkan ke folder hasil ekstrak.
3. Tunggu proses *Gradle Sync* selesai mengunduh *dependencies*.
4. Klik menu **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
5. Setelah muncul notifikasi selesai di pojok kanan bawah, klik **Locate** untuk mengambil file `app-debug.apk` di:
   `app/build/outputs/apk/debug/app-debug.apk`
