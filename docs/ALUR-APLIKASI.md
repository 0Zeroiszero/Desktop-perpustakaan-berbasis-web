# Alur Aplikasi PustakaDigital Desktop

Dokumen ini menjelaskan aplikasi dari awal sampai bisa dibaca. Ditulis dengan bahasa sederhana agar mudah diikuti.

## 1. Dari tombol "Run" sampai muncul halaman login

```text
Launcher.main(args)
   └─> Main.main(args)              → JavaFX memanggil Main.start(stage)
         └─> Main.start(stage)
               1. pasang penanggal error umum
               2. AppPaths.ensureDirectories()  → siapkan folder log + storage
               3. AppLogger.init()              → siapkan file log
               4. DatabaseConnection.testConnection() → cek koneksi ke MySQL
               5. new RootNavigator(stage)      → siapkan scene, CSS, dan Ui host
               6. Ui.setHost(navigator)
               7. navigator.start()             → tampilkan jendela
                     └─> navigate(Route.LOGIN, NavigationContext.empty())
```

Jika `DatabaseConnection.testConnection()` gagal, aplikasi menampilkan pesan error dan langsung tutup. Pesannya menyarankan untuk memeriksa MySQL dan file konfigurasi database.

`Launcher` ada karena aplikasi dikemas sebagai JAR. Tanpa kelas ini, JavaFX bisa mengeluh "JavaFX runtime components are missing".

## 2. Peta folder

```text
src/main/java/com/perpustakaan/
├── Main.java, Launcher.java     → titik masuk aplikasi
├── config/                      → AppConfig, AppContext, koneksi DB, AppPaths
├── model/                       → Ebook, Author, Kategori, User, BookListItem, dll.
├── repository/                  → semua query SQL/JDBC
├── security/                    → SessionManager, PasswordHasher, AccessControl
├── service/                     → aturan bisnis: AuthService, EbookService, dll.
├── util/                        → Async, FormatUtil, ImageUtil, ValidationUtil
├── exception/                   → AppException, ValidationException, dll.
├── view/                        → Route, RootNavigator, Ui, dialog, komponen UI
└── controller/                  → controller FXML, dibagi per halaman:
    ├── auth/                    → Login, Register
    ├── shell/                   → MainShell (sidebar + header + area konten)
    ├── admin/                   → Dashboard, Users, UserForm, BookForm, CollectionManagement
    ├── member/                  → Dashboard anggota, Rak Saya
    ├── catalog/                 → Katalog, Detail Buku
    └── reader/                  → Pembaca PDF

src/main/resources/
├── fxml/auth/                   → login.fxml, register.fxml
├── fxml/shell/                  → main-shell.fxml
├── fxml/admin/                  → dashboard, users, user-form, book-form,
│                                  collection-management
├── fxml/member/                 → dashboard, my-shelf
├── fxml/catalog/                → catalog, book-detail
├── fxml/reader/                 → reader
├── css/                         → theme, layout, components, auth, dashboard, catalog, reader
└── config/                      → app.properties, database.properties
```

Aturan penting: controller **hanya** menghubungkan FXML dengan service. Controller tidak boleh menulis SQL. Semua query ada di `repository/`.

## 3. Alur login sampai membaca buku

### 3.1 Login

1. `login.fxml` menampilkan kolom **Email** dan **Password**. Tombol Masuk hanya aktif jika dua kolom terisi.
2. `LoginController.handleLogin()` memanggil `app.getAuthService().login(email, password)`.
3. Jika berhasil, `SessionManager.login(user)` menyimpan user di memori.
4. `Ui.navigate(...)` memeriksa peran:
   - Admin → `Route.ADMIN_DASHBOARD`
   - Anggota → `Route.MEMBER_DASHBOARD`

Halaman register hanya meminta email, nama lengkap, password, dan konfirmasi password. Setelah daftar, pengguna dikembalikan ke halaman login.

### 3.2 Shell utama

`RootNavigator` memuat `main-shell.fxml` sekali saja. `MainShellController` mengganti isi area tengah dengan FXML sesuai `Route`. Sidebar berisi:

- Dashboard
- Katalog Buku
- Kelola Koleksi (hanya admin; berisi tab Buku, Penulis, Kategori)
- Kelola Pengguna (hanya admin)
- Rak Saya (hanya anggota)
- Keluar

### 3.3 Katalog → Detail → Reader

```text
Katalog Buku (CatalogController)
   └─> klik kartu buku
         └─> Detail Buku (BookDetailController)
               ├─> "Tambah ke Rak"  → ReadingProgressService.addToShelf()
               ├─> pilih status     → ReadingProgressService.setStatus()
               └─> "Baca Sekarang" / "Baca Ulang"
                     └─> Reader (ReaderController)
                           1. EbookService.openPdf()  → buka file PDF
                           2. ReadingProgressService.startReading()
                           3. mulai dari lastPosition (atau halaman 1 untuk "Baca Ulang")
                           4. setiap pindah halaman → saveProgress()
                           5. tombol Kembali → detail atau halaman asal
```

### 3.4 Rak Saya

`MyShelfController` menampilkan buku yang sudah disimpan anggota. Di setiap baris ada:

- progres dalam bentuk **halaman saat ini / total halaman**
- dropdown status: Akan dibaca, Sedang dibaca, Telah dibaca, Tidak Tertarik
- tombol lanjut baca, detail, dan hapus dari rak

Perubahan status langsung dipanggil ke `ReadingProgressService.setStatus(...)`.

### 3.5 Halaman admin

- **Dashboard**: ringkasan buku, anggota, kategori, penulis, aktivitas baca, dan distribusi kategori.
- **Kelola Koleksi**: satu halaman dengan tiga tab. Tab Buku untuk tambah/ubah/hapus buku (form berisi autocomplete Penulis, Genre, Penerbit, serta pemilih cover dan PDF). Tab Penulis dan Kategori berisi tabel + dialog tambah/ubah sederhana.
- **Kelola Pengguna**: tambah/ubah pengguna dan aktifkan/nonaktifkan akun. Tidak ada penghapusan akun.

## 4. Di mana mengubah konfigurasi database

File: `src/main/resources/config/database.properties`

```properties
db.host=localhost
db.port=3306
db.name=optl_pustaka
db.user=root
db.password=
```

Nilai di file bisa ditimpa dengan environment variable: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

Nama database yang dipakai aplikasi desktop adalah **`optl_pustaka`**, yaitu database yang sama dengan proyek web PHP. Aplikasi tidak memakai dump lama `db_pustaka` sebagai sumber kebenaran.

## 5. Dari mana file PDF dan cover diambil

File disimpan **di dalam proyek desktop sendiri**, bukan di folder web PHP:

```text
storage/cover/{id}.jpg   → cover buku
storage/ebook/{id}.pdf   → berkas PDF
```

Lokasinya dihitung otomatis dari posisi aplikasi (`app.home`), tidak perlu diatur di `app.properties` dan tidak memakai env `APP_WEB_ROOT`. Karena itu, tiap tim bisa menyimpan folder web di tempat dan dengan nama apa pun.

Kolom di database (`books.cover_path`, `books.storage_path`) tetap memakai format web, misalnya `uploads/covers/12.jpg` dan `storage/ebooks/12.pdf`. `AppPaths.resolveStoredPath()` menerjemahkannya ke file lokal di `storage/`. Nama lama seperti `bk001.pdf` juga masih dikenali.

Saat admin memilih cover atau PDF lewat form buku, `FileStorageService` menyalin berkas ke `storage/` desktop dan menyimpan path relatifnya ke database.

Yang bersama antar aplikasi hanya database. Berkas yang diunggah lewat web tidak otomatis tersedia di desktop, dan sebaliknya.

## 6. Ringkasan aturan yang sering tertukar

- Format e-book di UI hanya **PDF**. Tidak ada pilihan format EPUB/MOBI di katalog, form, maupun detail.
- Login memakai **email**, bukan username.
- Status baca memakai empat nilai web: `to_read`, `reading`, `finished`, `not_interested`.
- Semua daftar memakai paginasi; halaman tidak memiliki scrollbar panjang.
- Progres disimpan per akun. Akun lain yang membaca buku yang sama tidak mengubah progres Anda.
