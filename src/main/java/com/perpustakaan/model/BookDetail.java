package com.perpustakaan.model;

/**
 * Detail lengkap satu buku, termasuk penulis, penerbit, genre, dan progres.
 */
public record BookDetail(
        Ebook ebook,
        Kategori kategori,
        Author author,
        String publisherName,
        ReadingProgress progress
) {

    public String penulis() {
        return author == null ? "Tanpa penulis" : author.getNamaLengkap();
    }

    public boolean hasProgress() {
        return progress != null;
    }
}
