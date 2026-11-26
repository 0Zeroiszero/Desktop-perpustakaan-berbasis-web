---
name: mui-mcp
description: Akses dokumentasi resmi Material UI dan MUI X serta generate komponen React + MUI melalui MCP. Pakai saat proyek memakai atau akan menambah antarmuka React/MUI; jangan pakai untuk proyek JavaFX yang sekarang.
version: 1.0.0
sources:
  - https://mui.com/material-ui/getting-started/mcp/
  - https://mui.com/material-ui/llms.txt
tools:
  - fetchDocs
  - useMuiDocs
  - generateReactCode
---

# MUI MCP Skill

Skill ini menghubungkan agen ke dokumentasi resmi Material UI/MUI X dan ke generator kode React + MUI. Tool tersedia di **Code Mode** sebagai `tools["mui-mcp"]`.

## Kapan dipakai

- Menjawab pertanyaan API Material UI/MUI X (komponen, theming, aksesibilitas, migrasi versi).
- Menulis atau mereview kode React yang memakai `@mui/material`, `@mui/icons-material`, atau `@mui/x-data-grid`.
- Membandingkan versi MUI (v5, v6, v7, v9).

Proyek ini saat ini berbasis **JavaFX + Maven**, bukan React. Skill ini hanya relevan jikaLater ada frontend React/MUI; untuk styling JavaFX tetap pakai `ui-styling` dan CSS proyek.

## Sumber dokumentasi

- Panduan MCP: <https://mui.com/material-ui/getting-started/mcp/>
- Indeks dokumen: <https://mui.com/material-ui/llms.txt>

## Tools

### `useMuiDocs({ sources })`

Mengambil indeks/llms.txt dari paket atau URL. Contoh:

```js
await tools["mui-mcp"].useMuiDocs({
  sources: ["@mui/material", "@mui/x-data-grid"],
});
```

### `fetchDocs({ urls })`

Mengambil satu atau beberapa halaman dokumentasi yang sudah diketahui URL-nya:

```js
await tools["mui-mcp"].fetchDocs({
  urls: [
    "https://mui.com/material-ui/react-data-grid.md",
    "https://mui.com/material-ui/customization/theming.md",
  ],
});
```

### `generateReactCode({ prompt, muiPairing?, threadId? })`

Menghasilkan file React + MUI dari deskripsi singkat. Contoh:

```js
const result = await tools["mui-mcp"].generateReactCode({
  prompt: "Halaman katalog buku dengan DataGrid, filter kategori, dan dialog detail",
  muiPairing: { material: "v9", muiX: "v9" },
});
```

- `threadId` dari hasil dipakai lagi untuk melanjutkan percakapan generator.
- Tentukan `muiPairing` dari `package.json` (`@mui/material` dan `@mui/x-data-grid`). Tanpa `package.json`, default-nya Material UI v9 + MUI X v9.
- Tool ini memerlukan `MUI_RECIPES_API_KEY`. Kalau tidak tersedia, sampaikan ke pengguna daripada mengarang konfigurasi.

## Aturan penggunaan

- Dokumentasi dari MCP adalah sumber kebenaran; utamakan daripada ingatan model.
- Jangan menginstal `@mui/*` ke proyek JavaFX ini hanya karena skill ini ada.
- Jika hasil generator dipakai, taruh di proyek React terpisah dan sesuaikan dengan struktur proyek tersebut.
