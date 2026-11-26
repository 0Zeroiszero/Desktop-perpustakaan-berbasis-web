package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.model.User;
import com.perpustakaan.service.ProfileService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Memastikan tombol "!" pada saran penulis/penerbit/genre bisa membuka data.
 */
class ProfileLookupTest {

    @Test
    void authorPublisherAndGenreProfilesLoad() {
        AppContext app = AppContext.get();
        User admin = app.getAuthService().login("admin@pustaka.com", "admin123");
        ProfileService service = app.getProfileService();

        ProfileService.ProfileInfo author = service.load(admin, "author", "1");
        assertNotNull(author.nama());
        assertFalse(author.books().isEmpty(), "Profil penulis harus memuat buku");

        ProfileService.ProfileInfo publisher = service.load(admin, "publisher", "1");
        assertNotNull(publisher.nama());

        ProfileService.ProfileInfo genre = service.load(admin, "genre", "1");
        assertNotNull(genre.nama());
        System.out.println("PROFIL OK: penulis, penerbit, dan genre terbaca tanpa error query.");
    }
}
