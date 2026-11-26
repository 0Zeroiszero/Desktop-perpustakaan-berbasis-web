# PustakaDigital Desktop

Aplikasi desktop perpustakaan digital berbasis **JavaFX + FXML + CSS + MySQL/MariaDB**. Aplikasi ini memakai database yang sama dengan proyek web PHP, yaitu **`optl_pustaka`**, sehingga data buku, akun, dan progres baca langsung bersama. Berkas PDF dan cover disimpan mandiri di folder `storage/` proyek desktop.

Bila baru membaca kode, mulai dari [Alur Aplikasi](docs/ALUR-APLIKASI.md) dan [Peta Kode](docs/PETA-KODE.md).

## Fitur

### Admin
- Dashboard ringkasan koleksi, anggota, penulis, kategori, dan aktivitas baca.
- **Kelola Koleksi**: satu halaman dengan tab Buku, Penulis, dan Kategori.
  - Form buku bisa mengetik penulis, genre, dan penerbit langsung (autocomplete). Penulis/genre/penerbit baru dibuat otomatis saat disimpan.
  - Unggah cover dan berkas PDF; buku dinonaktifkan, bukan dihapus.
- **Kelola Pengguna**: tambah/ubah akun, aktifkan atau nonaktifkan. Akun tidak pernah dihapus.
- Katalog dengan pencarian, filter kategori, pengurutan, dan paginasi.

### Anggota
- Login dengan email, pendaftaran dengan konfirmasi password.
- Dashboard berisi statistik, lanjutkan bacaan, dan **Buku Terbaru**.
- Katalog, detail buku, sinopsis, dan informasi penerbit.
- Rak baca dengan empat status: Akan dibaca, Sedang dibaca, Telah dibaca, Tidak Tertarik.
- Pembaca PDF dengan navigasi halaman dan progres yang tersimpan otomatis.

Semua kolom wajib ditandai bintang merah dan tombol simpan baru aktif setelah kolom terisi.

## Teknologi

Java 21, JavaFX 21.0.13 LTS, FXML + CSS, MySQL Connector/J 8.4.0, Apache PDFBox 3.0.5, BCrypt, Maven Wrapper 3.9.9, JUnit 5.11.4.

## Menjalankan

Butuh JDK 21 atau lebih baru, MySQL/MariaDB di `localhost:3306`, dan database `optl_pustaka` yang sudah berisi data.

```powershell
.\mvnw.cmd javafx:run
```

Akun demo dari proyek web:

| Peran | Email | Password |
|---|---|---|
| Admin | `admin@pustaka.com` | `admin123` |
| Anggota | `andi@email.com` | `andi123` |

Aplikasi langsung membuka halaman login; tidak ada halaman beranda publik.

## Konfigurasi

Database: `src/main/resources/config/database.properties`

```properties
db.host=localhost
db.port=3306
db.name=optl_pustaka
db.user=root
db.password=
```

Bisa ditimpa dengan `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

Berkas PDF dan cover disimpan di folder proyek desktop sendiri:

```text
storage/ebook/{id}.pdf
storage/cover/{id}.jpg
```

Aplikasi tidak membaca folder proyek web PHP, jadi setiap tim bisa menyimpan folder web di mana pun dengan nama apa pun. Database menyimpan path relatif berformat web (`storage/ebooks/1.pdf`, `uploads/covers/1.jpg`); `AppPaths` menerjemahkannya ke file lokal. Detail skema ada di [Dokumentasi database](docs/database.md).

Konsekuensinya: database tetap sinkron, tetapi berkas tidak. PDF/cover yang diunggah lewat web tidak otomatis muncul di desktop, dan sebaliknya.

## Build dan test

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Hasil build: `target/perpustakaan-desktop-1.0.0.jar`. Jalankan dengan `java -jar target\perpustakaan-desktop-1.0.0.jar`. Detail pengujian ada di [Dokumentasi testing](docs/testing.md).

## Dokumentasi

- [Alur Aplikasi](docs/ALUR-APLIKASI.md) — dari `Main.java` sampai reader.
- [Peta Kode](docs/PETA-KODE.md) — fungsi tiap folder/package.
- [Database](docs/database.md), [Arsitektur](docs/architecture.md), [Panduan pengguna](docs/user-guide.md), [Testing](docs/testing.md).

## Catatan

- Format buku hanya PDF; tidak ada pilihan EPUB/MOBI.
- Login memakai email, bukan username.
- Penulis dengan nama sama memakai satu baris yang sama; slug dibuat unik otomatis.
- `UiScreenshotTest` hanya alat bantu visual, dijalankan manual dari IDE (tidak termasuk surefire).
