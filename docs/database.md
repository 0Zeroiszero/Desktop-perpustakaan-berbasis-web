# Database `optl_pustaka`

Aplikasi desktop dan proyek web PHP memakai **satu database yang sama**: `optl_pustaka` di `localhost:3306` (`root`, tanpa password). Database lama `db_pustaka` tidak dipakai lagi untuk menjalankan aplikasi ini.

File dump web ada di proyek web PHP: `database/optl_pustaka.sql`. Untuk clone baru, impor dump itu lalu jalankan aplikasi.

## Tabel

| Tabel | Isi |
|---|---|
| `users` | akun: `user_id`, `email`, `password_hash`, `display_name`, `role` (`admin`/`member`), `is_active` |
| `publishers` | penerbit: `publisher_id`, `name`, `slug`, `city` |
| `genres` | kategori buku: `genre_id`, `name`, `description` |
| `authors` | penulis: `author_id`, `name`, `slug`, `country`, `bio` |
| `books` | buku: `book_id`, `author_id`, `publisher_id`, `genre_id`, `title`, `slug`, `isbn`, `storage_path`, `page_count`, `cover_path`, `is_active` |
| `collections` | rak baca: `status` (`to_read`/`reading`/`finished`/`not_interested`), `status_source`, unik per `(user_id, book_id)` |
| `reading_progress` | posisi baca: `last_page`, unik per `(user_id, book_id)` |
| `v_books_catalog` | view gabungan buku + penulis + genre + penerbit |

Semua primary key memakai `INT AUTO_INCREMENT`. Semua query ada di folder `repository/` dengan prepared statement.

## Status baca

Nilai database memakai bahasa Inggris; label tampilan memakai bahasa Indonesia.

| DB | Label |
|---|---|
| `to_read` | Akan dibaca |
| `reading` | Sedang dibaca |
| `finished` | Telah dibaca |
| `not_interested` | Tidak Tertarik |

Persentase dihitung dari `last_page / books.page_count`, bukan dari status. Status hanya label dan tidak mengubah progres. Pengecualian: memilih `finished` memaksa `last_page = page_count` (persentase 100%). Status lain tidak menyentuh `last_page`. Sistem mengisi `reading` otomatis hanya bila pengguna belum punya status, dan menandai `finished` otomatis saat halaman terakhir tercapai hanya untuk baris dengan `status_source = 'system'`.

## Berkas buku

Berkas disimpan mandiri di proyek desktop:

```text
storage/ebook/{book_id}.pdf
storage/cover/{book_id}.jpg
```

Kolom `books.storage_path` berisi `storage/ebooks/1.pdf`, kolom `books.cover_path` berisi `uploads/covers/1.jpg`. `AppPaths.resolveStoredPath()` menerjemahkan path berformat web itu ke file lokal di `storage/`. Tidak ada `web.root` atau `APP_WEB_ROOT`.

Buku 84 (Atomic Habits) memakai `84.pdf` dan `84.jpg` dan tidak boleh dihapus.

## Penghapusan

Buku dan akun memakai **soft delete** lewat kolom `is_active`. Penulis, genre, dan penerbit tidak dihapus dari aplikasi. Yang boleh dihapus permanen hanya item rak (`collections`).
