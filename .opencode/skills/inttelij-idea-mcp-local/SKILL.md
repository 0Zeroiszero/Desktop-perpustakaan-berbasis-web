---
name: intellij-mcp-server
description: Integrasi lengkap alat MCP Server IntelliJ IDEA untuk analisis kode, kompilasi proyek, inspeksi linting, navigasi simbol, manajemen basis data, dan penanganan kontrol versi (VCS).
version: 1.0.0
tools:
  - analyze_calls
  - build_project
  - get_file_problems
  - get_project_dependencies
  - get_project_modules
  - lint_files
  - get_symbol_info
  - cancel_sql_query
  - create_database_connection
  - edit_database_connection
  - test_database_connection
  - get_database_schema
  - execute_sql_query
  - get_repositories
  - git_status
---

# IntelliJ IDEA MCP Server Skill

Skill ini menyediakan akses penuh ke fitur dan perkakas IDE melalui [IntelliJ IDEA MCP Server](https://www.jetbrains.com/help/idea/mcp-server.html) untuk membantu analisis kode, manipulasi basis data, dan pelacakan status proyek.

## Ringkasan Kategori Tools

### 1. Code Analysis Tools
- **`analyze_calls`**: Membangun pohon hierarki pemanggilan fungsi/metode (`INCOMING_CALLS` atau `OUTGOING_CALLS`).
- **`build_project`**: Mengompilasi berkas spesifik atau seluruh proyek untuk memeriksa error kompilasi.
- **`get_file_problems`**: Menganalisis satu berkas untuk error dan peringatan menggunakan inspeksi IntelliJ.
- **`get_project_dependencies`**: Mengambil daftar pustaka dan dependensi proyek.
- **`get_project_modules`**: Mengambil daftar modul proyek beserta tipenya.
- **`lint_files`**: Melakukan pemindaian linting pada beberapa berkas sekaligus.

### 2. Code Insight Tools
- **`get_symbol_info`**: Mengambil informasi deklarasi, tipe, dan dokumentasi ringkas (*Quick Documentation*) berdasarkan posisi baris dan kolom berkas.

### 3. Database Tools
- **`create_database_connection`**: Membuat koneksi *data source* baru menggunakan URL JDBC dan DBMS target.
- **`edit_database_connection`**: Mengubah driver DBMS atau URL JDBC pada *data source* yang ada.
- **`test_database_connection`**: Menguji kelayakan dan status koneksi basis data.
- **`get_database_schema`**: Membaca skema basis data (tabel, kolom, dan relasi).
- **`execute_sql_query`**: Menjalankan kueri SQL pada koneksi yang aktif.
- **`cancel_sql_query`**: Membatalkan eksekusi kueri SQL yang sedang berjalan via `sessionId`.

### 4. Version Control System (VCS) Tools
- **`get_repositories`**: Menampilkan daftar repositori VCS yang terdeteksi dalam proyek.
- **`git_status`**: Memeriksa status Git (berkas yang diubah, untracked, atau staged).

## Aturan Penggunaan

- **Parameter `projectPath`**: Selalu sertakan `projectPath` jika diketahui untuk memastikan instruksi dijalankan pada proyek yang tepat.
- **Validasi Kompilasi**: Jalankan `build_project` atau `lint_files` setelah melakukan pengeditan berkas kode.
- **Keamanan Basis Data**: Pastikan koneksi basis data yang dikonfigurasi menggunakan hak akses sesuai (*read-only* jika hanya membaca data/skema).