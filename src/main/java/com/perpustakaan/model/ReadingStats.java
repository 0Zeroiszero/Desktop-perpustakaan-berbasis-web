package com.perpustakaan.model;

import java.util.List;

/**
 * Ringkasan data untuk dashboard anggota.
 */
public record ReadingStats(
        long totalBooks,
        long shelfCount,
        long currentlyReading,
        long completed,
        BookListItem lastRead,
        List<BookListItem> latestBooks
) {

    public ReadingStats {
        latestBooks = latestBooks == null ? List.of() : List.copyOf(latestBooks);
    }
}
