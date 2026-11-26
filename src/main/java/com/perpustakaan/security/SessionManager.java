package com.perpustakaan.security;

import com.perpustakaan.exception.AccessDeniedException;
import com.perpustakaan.model.User;

import java.util.Optional;

/**
 * Menyimpan pengguna yang sedang login selama aplikasi berjalan.
 */
public final class SessionManager {

    private User currentUser;

    public void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Pengguna tidak boleh null.");
        }
        this.currentUser = user;
    }

    public void updateCurrentUser(User user) {
        if (user == null || currentUser == null || !user.getIdUser().equals(currentUser.getIdUser())) {
            throw new IllegalArgumentException("Pengguna yang diperbarui tidak sesuai sesi.");
        }
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public User requireAuthenticated() {
        if (currentUser == null) {
            throw new AccessDeniedException("Silakan masuk terlebih dahulu.");
        }
        return currentUser;
    }

    public User requireAdmin() {
        User user = requireAuthenticated();
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Halaman ini hanya dapat diakses oleh admin.");
        }
        return user;
    }
}
