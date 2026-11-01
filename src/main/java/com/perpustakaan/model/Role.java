package com.perpustakaan.model;

import java.util.Locale;

/**
 * Peran pengguna. Database menyimpan "Admin" dan "anggota" (huruf kecil).
 */
public enum Role {
    ADMIN("Admin", "Admin"),
    MEMBER("anggota", "Anggota");

    private final String databaseValue;
    private final String displayName;

    Role(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public String displayName() {
        return displayName;
    }

    public static Role fromDatabase(String value) {
        if (value != null && value.trim().toLowerCase(Locale.ROOT).equals("admin")) {
            return ADMIN;
        }
        return MEMBER;
    }
}
