# Pengujian

Jalankan dari root proyek:

```powershell
.\mvnw.cmd test
```

Test yang dijalankan surefire:

| Test | Yang dicek |
|---|---|
| `IntegrationSmokeTest` | koneksi ke `optl_pustaka`, login admin/anggota, katalog, detail buku, rak, dashboard, status baca, dan keberadaan file lokal untuk semua buku termasuk Atomic Habits (id 84) |
| `UiSmokeTest` | semua FXML dimuat, CSS terpasang, data halaman muncul, validasi form, lalu menjalankan shell aplikasi sungguhan dan berpindah ke delapan halaman admin/anggota sambil mengambil screenshot |
| `UserScenarioTest` | delapan operasi CRUD lewat klik UI sungguhan, menghitung klik per operasi, dan memastikan tidak lebih dari 3 klik |
| `ReadingRuleTest` | status hanya label; `Telah dibaca` memaksa halaman terakhir ke 100%; status lain tidak mengubah progres; cover otomatis dibuat |
| `ProfileLookupTest` | tombol `!` membaca profil penulis/penerbit/genre tanpa error query |
| `ProfileController` | isi popup profil diuji melalui `UiSmokeTest` |

`UiScreenshotTest` tetap dikecualikan dari surefire; ia hanya alat bantu visual manual.

Screenshot tersimpan di dua folder:

```text
%TEMP%\opencode\pustaka-gui-smoke\       ← delapan halaman aplikasi
%TEMP%\opencode\pustaka-user-scenarios\  ← delapan operasi CRUD
```

## Aturan hitung klik

Titik mulai adalah dashboard sesuai peran. Yang dihitung hanya klik pada kontrol aplikasi (tombol, kartu buku, konfirmasi dialog). Mengetik, memilih file di `FileChooser` sistem, dan menunggu data tidak dihitung. Batasnya 3 klik per operasi.

| Fitur | Operasi | Klik |
|---|---|---|
| Anggota | Tambah buku ke rak | 3 |
| Anggota | Baca buku | 2 |
| Anggota | Ganti status baca | 1 |
| Anggota | Hapus dari rak | 2 |
| Admin | Tambah penulis | 3 |
| Admin | Lihat buku di katalog | 2 |
| Admin | Ubah buku | 2 |
| Admin | Nonaktifkan buku | 2 |

Semua operasi lulus batas 3 klik.

## Catatan

- `IntegrationSmokeTest` hanya membaca data.
- `UiSmokeTest` dan `UserScenarioTest` membersihkan data yang mereka buat, termasuk file buku dummy. Database kembali ke 11 buku, 14 rak, dan 11 pengguna.
- Buku 84 (Atomic Habits) tidak pernah diubah atau dihapus.
- Kedua test JavaFX berbagi satu toolkit dalam satu JVM; penanda `pustaka.fx.started` mencegah `Platform.startup` dipanggil dua kali.
- Peringatan JavaFX (`Unsupported JavaFX configuration`, native access) adalah warning runtime, bukan kegagalan test.
