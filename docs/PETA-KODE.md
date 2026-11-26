# Peta Kode

Tabel ini membantu mencari "di mana kode ini seharusnya ada". Baca [ALUR-APLIKASI.md](ALUR-APLIKASI.md) untuk alur aplikasinya.

| Package / folder | Tanggung jawab | File contoh |
|---|---|---|
| `com.perpustakaan` (root) | Titik masuk aplikasi dan shutdown | `Main.java`, `Launcher.java` |
| `config` | Konfigurasi, koneksi database, transaksi, lokasi file | `AppConfig.java`, `DatabaseConnection.java`, `AppContext.java`, `AppPaths.java` |
| `model` | Data yang dikirim antar lapisan (DTO/entity) dan enum | `Ebook.java`, `BookListItem.java`, `BookDetail.java`, `ReadingStatus.java`, `User.java` |
| `repository` | Satu-satunya tempat query SQL/JDBC dan mapping baris database | `EbookRepository.java`, `UserRepository.java`, `ReadingProgressRepository.java` |
| `security` | Sesi login, hash BCrypt, pemeriksaan akses | `SessionManager.java`, `PasswordHasher.java`, `AccessControl.java` |
| `service` | Aturan bisnis, validasi, transaksi, orkestrasi file + database | `AuthService.java`, `EbookService.java`, `ReadingProgressService.java`, `DashboardService.java` |
| `exception` | Jenis error aplikasi dan pesan validasi | `AppException.java`, `ValidationException.java` |
| `util` | Fungsi bantu: async, tanggal/angka, gambar, paginasi teks | `Async.java`, `FormatUtil.java`, `ImageUtil.java`, `TextPaginator.java` |
| `view` | Navigasi, host UI, toast, dialog, komponen tampilan | `Route.java`, `RootNavigator.java`, `Ui.java`, `FormDialogUtil.java`, `AutoCompleteTextField.java` |
| `controller.auth` | Halaman login dan daftar akun | `LoginController.java`, `RegisterController.java` |
| `controller.shell` | Kerangka aplikasi: sidebar, header, konten, toast, busy overlay | `MainShellController.java` |
| `controller.admin` | Halaman admin | `AdminDashboardController.java`, `UsersController.java`, `CollectionManagementController.java`, `BookFormController.java` |
| `controller.member` | Dashboard anggota dan rak baca | `MemberDashboardController.java`, `MyShelfController.java` |
| `controller.catalog` | Katalog dan detail buku | `CatalogController.java`, `BookDetailController.java` |
| `controller.reader` | Pembaca PDF | `ReaderController.java` |
| `resources/fxml` | Deklarasi tampilan JavaFX; `fx:controller` menunjuk controller di atas | `auth/login.fxml`, `admin/collection-management.fxml`, `reader/reader.fxml` |
| `resources/css` | Token visual dan style komponen | `theme.css`, `components.css`, `catalog.css`, `reader.css` |
| `resources/config` | Nilai konfigurasi yang bisa diubah tanpa kode | `database.properties`, `app.properties` |
| `database` | Skema, dump, dan migrasi (untuk proyek web) | `schema.sql`, `migration-user-status-activity.sql` |
| `storage` | File PDF dan cover milik proyek desktop | `cover/{id}.jpg`, `ebook/{id}.pdf` |
| `src/test/java` | Smoke test integrasi dan GUI (screenshot) | `IntegrationSmokeTest.java`, `UiSmokeTest.java` |

## Aturan cepat

- Menambah halaman baru: buat FXML → daftarkan di `Route` → buat controller di package yang sesuai → tambah tombol di `main-shell.fxml` bila perlu.
- Mengubah query: ubah di `repository/`, lalu tambah method di `service/` bila controller memerlukannya.
- Mengubah tampilan: ubah FXML atau CSS. Jangan menaruh logika bisnis di controller.
- Mengubah koneksi database: `resources/config/database.properties` atau environment variable `DB_*`.
- Mengubah lokasi file buku/cover: taruh file di `storage/ebook` dan `storage/cover` proyek ini; tidak perlu env.
