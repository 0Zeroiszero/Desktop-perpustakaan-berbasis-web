package com.pbo.model;

/** Data satu buku, dipetakan dari tabel ebook + kategori + author. */
public record Book(
        String id,
        String title,
        String author,
        String category,
        String cover,
        String file,
        String format,
        Integer year,
        Integer pages,
        String isbn,
        String synopsis
) {
    public String yearText() {
        return year == null ? "-" : String.valueOf(year);
    }

    public String pagesText() {
        return pages == null ? "-" : pages + " hlm";
    }

    public String isbnText() {
        return isbn == null || isbn.isBlank() ? "-" : isbn;
    }
}
