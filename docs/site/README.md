# Situs dokumentasi

Situs ini berisi dokumentasi PustakaDigital Desktop. Isinya dibangun dengan React, TypeScript, dan Material UI.

## Menjalankan di komputer

```bash
cd docs/site
npm install
npm run dev
```

Buka alamat yang muncul di terminal. Untuk membuat versi produksi:

```bash
npm run build
```

Hasil build ada di folder `dist`.

## Vercel

Saat membuat project di Vercel, pilih **Root Directory** = `docs/site`. Vercel akan membaca `vercel.json`, lalu menjalankan `npm run build` dan memakai `dist` sebagai hasil deploy.

## Isi

Bagian dokumentasi berasal dari file Markdown di folder `docs`. Teks sudah ditulis ulang dengan bahasa sehari-hari. Istilah yang tampil di antarmuka aplikasi diberi penanda ungu.

Path kode di situs ini terhubung ke branch `main` di GitHub. Tautan akan aktif setelah branch `dev` di-merge ke `main`.
