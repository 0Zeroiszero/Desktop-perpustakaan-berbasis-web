package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.config.DatabaseConnection;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.DashboardStats;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.ReadingStats;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.service.ReadingProgressService;
import com.perpustakaan.util.TextPaginator;
import com.perpustakaan.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationSmokeTest {

    @Test
    void databaseAndServicesWork() {
        DatabaseConnection.testConnection();
        AppContext app = AppContext.get();

        User admin = app.getAuthService().login("adminpustaka", "admin123");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("Admin Pustaka", admin.getNamaLengkap());

        User member = app.getAuthService().login("andi@email.com", "andi123");
        assertEquals(Role.MEMBER, member.getRole());

        PageResult<BookListItem> catalog = app.getEbookService()
                .listCatalog(member, "", null, null, "terbaru", 1, 8);
        assertTrue(catalog.totalItems() >= 10);
        assertTrue(catalog.items().size() >= 8);

        BookDetail detail = app.getEbookService().detail(member, "BK001");
        assertNotNull(detail);
        assertEquals("Dasar-Dasar Basis Data", detail.ebook().getJudul());
        assertNotNull(detail.progress());

        PageResult<BookListItem> shelf = app.getReadingProgressService()
                .shelf(member, "", "terakhir", 1, 7);
        assertTrue(shelf.totalItems() >= 1);
        assertNotNull(shelf.items().get(0).status());

        DashboardStats adminStats = app.getDashboardService().admin(admin);
        assertTrue(adminStats.totalBooks() >= 10);
        assertTrue(adminStats.totalMembers() >= 9);

        ReadingStats memberStats = app.getDashboardService().member(member);
        assertTrue(memberStats.totalBooks() >= 10);
        assertTrue(memberStats.shelfCount() >= 1);
        assertNotNull(memberStats.lastRead());

        assertEquals(0, ReadingProgressService.calculatePercent(250, 250).compareTo(BigDecimal.valueOf(100)));
        assertEquals(250, ReadingProgressService.clampPage(500, 250));
        assertEquals(1, ReadingProgressService.clampPage(0, 250));

        List<String> pages = TextPaginator.paginate(
                "satu dua tiga empat lima enam tujuh delapan sembilan sepuluh ".repeat(10), 80);
        assertTrue(pages.size() > 1);

        assertThrows(RuntimeException.class, () -> ValidationUtil.email("bukan-email", "email"));
    }
}
