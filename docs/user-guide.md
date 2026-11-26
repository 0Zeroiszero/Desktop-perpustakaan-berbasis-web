# Panduan pengguna

## Masuk dan daftar

Aplikasi langsung membuka halaman **Masuk**. Isi email dan password. Tombol Masuk aktif setelah kedua kolom terisi.

Akun demo:

| Peran | Email | Password |
|---|---|---|
| Admin | `admin@pustaka.com` | `admin123` |
| Anggota | `andi@email.com` | `andi123` |

Halaman **Daftar** meminta email, nama lengkap, password, dan konfirmasi password. Kolom wajib ditandai `*`. Akun yang dinonaktifkan admin tidak bisa masuk.

## Shell utama

Setelah masuk, sidebar berisi Dashboard, Katalog Buku, Kelola Koleksi (admin), Kelola Pengguna (admin), Rak Saya, dan Keluar. Menu Rak Saya tersedia untuk admin maupun anggota.

## Katalog dan detail buku

Katalog bisa mencari judul/penulis/ISBN, memilih kategori, mengurutkan, dan berpaginasi. Klik kartu buku selalu membuka detail: sinopsis, penulis, kategori, penerbit, tahun, halaman, dan ISBN. Bila kartu punya tombol **Lanjutkan**, tombol itulah yang membuka pembaca. Nama berkas PDF tidak ditampilkan.

Di detail tersedia dropdown status rak dan tombol baca. Bila status buku `Telah dibaca`, tombol berubah menjadi **Baca Ulang**.

## Rak Saya

Empat tab/status: Akan dibaca, Sedang dibaca, Telah dibaca, Tidak Tertarik. Tiap baris menampilkan progres `halaman saat ini / total halaman` dan dropdown status. Mengubah status langsung disimpan. Buku bisa dihapus dari rak dengan konfirmasi.

Persentase selalu dihitung dari halaman terakhir yang dibaca. Status hanya label dan tidak mengubah progres, kecuali label **Telah dibaca**: memilihnya memaksa halaman terakhir ke jumlah halaman buku sehingga persentase menjadi 100%. Memilih status lain tidak menurunkan progres yang sudah tercapai. Sistem tetap mengisi `Sedang dibaca` saat buku pertama kali dibaca dan belum punya status, serta menandai `Telah dibaca` otomatis saat halaman terakhir tercapai untuk status yang diisi sistem.

## Pembaca PDF

Tombol Sebelumnya/Berikutnya memindah halaman. Persentase dan status baca tampil di bagian atas dekat judul buku. Tombol Kembali menuju halaman sebelumnya. Tidak ada fitur zoom, bookmark, quote, atau daftar bab.

## Halaman admin

### Dashboard

Dashboard menampilkan jumlah buku, anggota, kategori, dan penulis. Di bawahnya ada empat kartu ringkasan, masing-masing lima item teratas:

- **Buku terbaru** (berdasarkan waktu tambah)
- **Genre terbanyak**
- **Penulis terbanyak**
- **Penerbit terbanyak**

### Kelola Koleksi

Satu halaman dengan tiga tab:

- **Buku**: tambah/ubah buku, unggah cover dan PDF. Kolom Judul, Penulis, Genre, Penerbit, dan berkas PDF wajib diisi. Ketik nama penulis/genre/penerbit; bila belum ada, data dibuat otomatis saat simpan. Tombol `!` di sebelah saran membuka popup profil lengkap, termasuk untuk nama yang sama.
- **Penulis**: tambah/ubah nama, negara asal, dan bio.
- **Kategori**: tambah/ubah nama dan deskripsi.

### Kelola Pengguna

Tambah/ubah akun (email, nama lengkap, role, password) dan aktifkan/nonaktifkan. Tidak ada tombol hapus akun.

## Tips

- Kolom bertanda `*` wajib diisi; tombol simpan tetap nonaktif sampai lengkap.
- Jika PDF gagal dibuka, cek berkas di `storage/ebook/` proyek desktop dan pastikan path di database sesuai.
- Saat menambah buku tanpa cover, aplikasi membuat cover otomatis 600×800.
- Di form buku, tombol `!` di saran penulis/penerbit/genre membuka popup profil lengkap, berguna untuk membedakan nama yang sama.
- Buku yang dinonaktifkan admin hilang dari katalog tetapi tetap ada di rak dengan pesan "Buku tidak tersedia". Admin dapat mengaktifkannya kembali dari Kelola Koleksi.
- Penulis dengan nama yang sama akan memakai satu data yang sama, bukan duplikat.
