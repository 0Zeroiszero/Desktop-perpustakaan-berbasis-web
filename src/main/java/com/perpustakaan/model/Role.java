package com.perpustakaan.model;

import java.util.Locale;

/**
 * Peran pengguna sesuai ENUM('admin','member') pada tabel users.
 */
public enum Role {
    ADMIN("admin", "Admin"),
    MEMBER("member", "Anggota");

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
        if (value != null && value.trim().equalsIgnoreCase(ADMIN.databaseValue)) {
            return ADMIN;
        }
        return MEMBER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
