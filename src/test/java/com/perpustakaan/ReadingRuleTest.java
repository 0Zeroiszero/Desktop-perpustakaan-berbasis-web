package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.config.AppPaths;
import com.perpustakaan.config.DatabaseConnection;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.ReadingProgress;
import com.perpustakaan.model.ReadingStatus;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.Jdbc;
import com.perpustakaan.service.EbookService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Aturan progres: persentase mengikuti halaman terakhir. Status hanya label,
 * kecuali "Telah dibaca" yang memaksa progres ke 100%.
 */
class ReadingRuleTest {

    private static final String MARK = "Aturan" + System.currentTimeMillis();

    @Test
    void statusIsLabelExceptFinished() throws Exception {
        AppContext app = AppContext.get();
        User admin = app.getAuthService().login("admin@pustaka.com", "admin123");
        String title = "Buku " + MARK;
        String id = null;

        try {
            Ebook created = app.getEbookService().create(admin, new EbookService.BookFormData(
                    title, "2026", "12", null, "Penerbit " + MARK, "Genre " + MARK, "Penulis " + MARK,
                    "Uji aturan progres", null,
                    AppPaths.getEbookDir().resolve("3.pdf")));
            id = created.getIdBook();
            assertTrue(AppPaths.exists(created.getCoverPath()), "Cover otomatis harus dibuat");
            int total = created.getJumlahHalaman();
            assertEquals(12, total, "PDF uji harus punya 12 halaman");

            app.getReadingProgressService().addToShelf(admin, id);
            app.getReadingProgressService().saveProgress(admin, id, 1, total);
            assertEquals(1, app.getReadingProgressService().find(admin, id).getLastPosition());

            app.getReadingProgressService().setStatus(admin, id, ReadingStatus.SEDANG_DIBACA);
            assertEquals(1, app.getReadingProgressService().find(admin, id).getLastPosition(),
                    "Status biasa tidak boleh mengubah halaman terakhir");

            app.getReadingProgressService().setStatus(admin, id, ReadingStatus.TELAH_DIBACA);
            ReadingProgress finished = app.getReadingProgressService().find(admin, id);
            assertEquals(total, finished.getLastPosition(), "Label selesai harus memaksa halaman terakhir");
            assertEquals(100, finished.getProgressInt(), "Persentase harus 100%");

            app.getReadingProgressService().setStatus(admin, id, ReadingStatus.AKAN_DIBACA);
            assertEquals(total, app.getReadingProgressService().find(admin, id).getLastPosition(),
                    "Mengubah label tidak boleh menurunkan progres");

            System.out.println("ATURAN PROGRES OK: status label tidak mengubah progres, selesai memaksa 100%.");
        } finally {
            if (id != null) {
                cleanup(id);
            }
        }
    }

    private static void cleanup(String bookId) throws Exception {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Jdbc.update(connection, "DELETE FROM collections WHERE book_id = ?", List.of(Integer.valueOf(bookId)));
            Jdbc.update(connection, "DELETE FROM reading_progress WHERE book_id = ?", List.of(Integer.valueOf(bookId)));
            Jdbc.update(connection, "DELETE FROM books WHERE book_id = ?", List.of(Integer.valueOf(bookId)));
            Jdbc.update(connection, "DELETE FROM authors WHERE name LIKE ?", List.of("Penulis " + MARK + "%"));
            Jdbc.update(connection, "DELETE FROM genres WHERE name LIKE ?", List.of("Genre " + MARK + "%"));
            Jdbc.update(connection, "DELETE FROM publishers WHERE name LIKE ?", List.of("Penerbit " + MARK + "%"));
        }
        for (String stored : List.of(AppPaths.ebookStoredPath(bookId), AppPaths.coverStoredPath(bookId))) {
            Path file = AppPaths.resolveStoredPath(stored);
            if (file != null) {
                Files.deleteIfExists(file);
            }
        }
    }
}
