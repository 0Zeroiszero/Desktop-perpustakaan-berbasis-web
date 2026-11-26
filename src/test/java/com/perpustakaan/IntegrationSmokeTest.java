package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.config.AppPaths;
import com.perpustakaan.config.DatabaseConnection;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.DashboardStats;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.ReadingStats;
import com.perpustakaan.model.ReadingStatus;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.service.ReadingProgressService;
import com.perpustakaan.util.TextPaginator;
import com.perpustakaan.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uji koneksi dan alur baca memakai database optl_pustaka yang sudah diisi.
 * Test ini sengaja hanya membaca data, tidak mengubah database.
 */
class IntegrationSmokeTest {

    @Test
    void databaseAndServicesWork() {
        DatabaseConnection.testConnection();
        AppContext app = AppContext.get();

        User admin = app.getAuthService().login("admin@pustaka.com", "admin123");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("Admin Pustaka", admin.getNamaLengkap());

        User member = app.getAuthService().login("andi@email.com", "andi123");
        assertEquals(Role.MEMBER, member.getRole());

        PageResult<BookListItem> catalog = app.getEbookService()
                .listCatalog(member, "", null, "terbaru", 1, 8);
        assertTrue(catalog.totalItems() > 0);
        assertFalse(catalog.items().isEmpty());

        PageResult<BookListItem> allBooks = app.getEbookService()
                .listCatalog(admin, "", null, "terbaru", 1, 100);
        assertFalse(allBooks.items().isEmpty());
        for (BookListItem item : allBooks.items()) {
            assertTrue(AppPaths.exists(item.coverPath()), "Cover lokal tidak ada: " + item.judul());
        }

        BookDetail atomic = app.getEbookService().detail(admin, "84");
        assertTrue(atomic.ebook().getJudul().startsWith("Atomic Habits"), "Buku Atomic Habits harus tetap ada");
        assertTrue(AppPaths.exists(atomic.ebook().getFilePath()), "PDF Atomic Habits harus ada di storage lokal");
        assertTrue(AppPaths.exists(atomic.ebook().getCoverPath()), "Cover Atomic Habits harus ada di storage lokal");
        assertTrue(AppPaths.resolveStoredPath("storage/ebooks/84.pdf").startsWith(AppPaths.getEbookDir()),
                "Path database harus diarahkan ke storage desktop, bukan folder web");

        BookDetail detail = app.getEbookService().detail(member, "1");
        assertEquals("Dasar-Dasar Basis Data", detail.ebook().getJudul());
        assertNotNull(detail.kategori());
        assertNotNull(detail.author());
        assertNotNull(detail.publisherName());

        PageResult<BookListItem> shelf = app.getReadingProgressService()
                .shelf(member, "", null, "terbaru", 1, 7);
        assertTrue(shelf.totalItems() >= 1);
        assertNotNull(shelf.items().get(0).status());

        DashboardStats adminStats = app.getDashboardService().admin(admin);
        assertTrue(adminStats.totalBooks() > 0);
        assertTrue(adminStats.totalMembers() > 0);

        ReadingStats memberStats = app.getDashboardService().member(member);
        assertTrue(memberStats.totalBooks() > 0);
        assertTrue(memberStats.shelfCount() >= 1);
        assertFalse(memberStats.latestBooks().isEmpty());
        assertNotNull(memberStats.lastRead());

        assertEquals(ReadingStatus.TELAH_DIBACA, ReadingStatus.fromDatabase("finished"));
        assertEquals(ReadingStatus.SEDANG_DIBACA, ReadingStatus.fromDatabase("reading"));
        assertEquals(ReadingStatus.TIDAK_TERTARIK, ReadingStatus.fromDatabase("not_interested"));
        assertEquals(ReadingStatus.AKAN_DIBACA, ReadingStatus.fromDatabase("to_read"));
        assertEquals(0, ReadingProgressService.calculatePercent(250, 250).compareTo(BigDecimal.valueOf(100)));
        assertEquals(250, ReadingProgressService.clampPage(500, 250));
        assertEquals(1, ReadingProgressService.clampPage(0, 250));

        List<String> pages = TextPaginator.paginate(
                "satu dua tiga empat lima enam tujuh delapan sembilan sepuluh ".repeat(10), 80);
        assertTrue(pages.size() > 1);

        assertThrows(RuntimeException.class, () -> ValidationUtil.email("bukan-email", "email"));
    }
}
