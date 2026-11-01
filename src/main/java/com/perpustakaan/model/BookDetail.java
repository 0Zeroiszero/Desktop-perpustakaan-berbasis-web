package com.perpustakaan.model;

import java.util.List;

/**
 * Detail lengkap satu buku, termasuk relasi penulis dan progres pengguna.
 */
public record BookDetail(
        Ebook ebook,
        Kategori kategori,
        List<Author> authors,
        ReadingProgress progress
) {

    public BookDetail {
        authors = authors == null ? List.of() : List.copyOf(authors);
    }

    public String penulis() {
        if (authors.isEmpty()) {
            return "Tanpa penulis";
        }
        return authors.stream().map(Author::getNamaLengkap).reduce((a, b) -> a + ", " + b).orElse("Tanpa penulis");
    }

    public boolean hasProgress() {
        return progress != null;
    }
}
