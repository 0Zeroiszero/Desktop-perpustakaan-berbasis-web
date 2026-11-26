package com.perpustakaan.security;

import com.perpustakaan.exception.AccessDeniedException;
import com.perpustakaan.model.User;

/**
 * Pemeriksaan otorisasi yang dipakai lapisan service.
 */
public final class AccessControl {

    private AccessControl() {
    }

    public static User requireAuthenticated(User user) {
        if (user == null) {
            throw new AccessDeniedException("Silakan masuk terlebih dahulu.");
        }
        return user;
    }

    public static User requireAdmin(User user) {
        requireAuthenticated(user);
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Operasi ini hanya dapat dilakukan oleh admin.");
        }
        return user;
    }

    public static User requireSelfOrAdmin(User actor, String targetUserId) {
        requireAuthenticated(actor);
        if (!actor.isAdmin() && !actor.getIdUser().equals(targetUserId)) {
            throw new AccessDeniedException("Anda tidak memiliki akses ke data pengguna lain.");
        }
        return actor;
    }
}
