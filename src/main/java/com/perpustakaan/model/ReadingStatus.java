package com.perpustakaan.model;

import java.util.Locale;

/**
 * Status baca sesuai nilai yang sudah dipakai database.
 */
public enum ReadingStatus {
    AKAN_DIBACA("Akan dibaca", "badge-purple"),
    SEDANG_DIBACA("Sedang dibaca", "badge-blue"),
    TELAH_DIBACA("Telah dibaca", "badge-green"),
    TIDAK_SELESAI("Tidak selesai", "badge-red");

    private final String databaseValue;
    private final String styleClass;

    ReadingStatus(String databaseValue, String styleClass) {
        this.databaseValue = databaseValue;
        this.styleClass = styleClass;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public String styleClass() {
        return styleClass;
    }

    public boolean isCompleted() {
        return this == TELAH_DIBACA;
    }

    public boolean isActive() {
        return this == SEDANG_DIBACA || this == TIDAK_SELESAI;
    }

    public static ReadingStatus fromDatabase(String value) {
        ReadingStatus status = fromDatabaseNullable(value);
        return status == null ? AKAN_DIBACA : status;
    }

    public static ReadingStatus fromDatabaseNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("tidak")) {
            return TIDAK_SELESAI;
        }
        if (normalized.contains("selesai")) {
            return TELAH_DIBACA;
        }
        if (normalized.contains("sedang")) {
            return SEDANG_DIBACA;
        }
        return AKAN_DIBACA;
    }
}
