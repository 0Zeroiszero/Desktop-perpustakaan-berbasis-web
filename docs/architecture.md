# Arsitektur dan susunan kode

Aplikasi memakai pola berlapis. Aturan utamanya: **controller tidak boleh menulis SQL**. Semua query ada di `repository/`, semua aturan bisnis ada di `service/`.

```text
Main.java / Launcher.java      titik masuk, cek koneksi, buka jendela
        │
        ▼
view/                          navigasi (Route, RootNavigator, Ui), dialog, komponen UI
        │
        ▼
controller/                    mengikat FXML ke service, mengubah input jadi data
        │
        ▼
service/                       validasi, aturan bisnis, transaksi, file
        │
        ▼
repository/                    SQL/JDBC (Jdbc, EbookRepository, CollectionRepository, ...)
        │
        ▼
model/                         Ebook, BookListItem, User, ReadingStatus, dll.
```

## Fungsi tiap folder Java

| Folder | Isi |
|---|---|
| `config/` | `AppConfig`, `AppContext` (tempat membuat service), `AppPaths`, koneksi DB, transaksi |
| `model/` | data dan enum. `BookListItem` dipakai untuk kartu/tabel; `BookDetail` untuk detail buku |
| `repository/` | satu kelas per tabel atau view; helper `Jdbc` dipakai bersama |
| `service/` | aturan bisnis. `EbookService`, `ReadingProgressService`, `AuthService`, `UserService`, `DashboardService` |
| `security/` | sesi login, `PasswordHasher`, `AccessControl` |
| `view/` | `Route`, `RootNavigator`, `Ui`, `AutoCompleteTextField`, dialog, komponen kartu |
| `controller/` | dibagi mengikuti FXML: `auth`, `shell`, `admin`, `member`, `catalog`, `reader` |
| `util/` | async, format tanggal/angka, gambar, validasi, slug |
| `exception/` | error aplikasi dan validasi |

## Sumber data

- Database: `optl_pustaka`, sama dengan proyek web PHP.
- Berkas PDF dan cover: folder `storage/` milik proyek desktop (lihat `docs/database.md`). Aplikasi tidak membaca folder web.
- Konfigurasi: `src/main/resources/config/`.

## Keputusan teknis

- PDF saja. Tidak ada format EPUB/MOBI di UI maupun database.
- Login memakai email. Hash password BCrypt kompatibel dengan `$2y$` dari PHP.
- Satu buku memiliki satu penulis. Penulis dengan nama sama memakai baris yang sama; slug unik dibuat otomatis dengan akhiran angka bila bentrok.
- Penulis, genre, dan penerbit dibuat otomatis dari form buku bila nama belum ada.
- `AutoCompleteTextField` hanya dipakai pada field yang punya master data (penulis, genre, penerbit).
- Buku dan akun dinonaktifkan, tidak dihapus.
- Tanpa connection pool: aplikasi desktop hanya melayani satu sesi.

## Menambah halaman baru

1. Buat FXML di `src/main/resources/fxml/<grup>/`.
2. Buat controller di `controller/<grup>/` dengan `fx:controller` yang sesuai.
3. Daftarkan rute di `view/Route.java`.
4. Tambah navigasi di `MainShellController` bila perlu.
5. Tambah FXML ke `UiSmokeTest` supaya tetap teruji.
