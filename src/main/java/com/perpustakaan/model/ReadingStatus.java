package com.perpustakaan.model;

import java.util.Locale;

/**
 * Status baca. Nilai database memakai bahasa Inggris, sedangkan label tampilan
 * memakai bahasa Indonesia (kompatibel dengan pemanggilan lama databaseValue()).
 */
public enum ReadingStatus {
    AKAN_DIBACA("to_read", "Akan dibaca", "badge-purple"),
    SEDANG_DIBACA("reading", "Sedang dibaca", "badge-blue"),
    TELAH_DIBACA("finished", "Telah dibaca", "badge-green"),
    TIDAK_TERTARIK("not_interested", "Tidak Tertarik", "badge-red");

    private final String dbValue;
    private final String label;
    private final String styleClass;

    ReadingStatus(String dbValue, String label, String styleClass) {
        this.dbValue = dbValue;
        this.label = label;
        this.styleClass = styleClass;
    }

    /** Nilai ENUM yang tersimpan di tabel collections. */
    public String dbValue() {
        return dbValue;
    }

    /** Label Indonesia yang dipakai UI. */
    public String databaseValue() {
        return label;
    }

    public String label() {
        return label;
    }

    public String styleClass() {
        return styleClass;
    }

    public boolean isCompleted() {
        return this == TELAH_DIBACA;
    }

    public boolean isActive() {
        return this == SEDANG_DIBACA;
    }

    public static ReadingStatus fromDatabase(String value) {
        ReadingStatus status = fromDatabaseNullable(value);
        return status == null ? AKAN_DIBACA : status;
    }

    public static ReadingStatus fromDatabaseNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (normalized) {
            case "to_read", "akan_dibaca" -> AKAN_DIBACA;
            case "reading", "sedang_dibaca" -> SEDANG_DIBACA;
            case "finished", "telah_dibaca", "selesai" -> TELAH_DIBACA;
            case "not_interested", "tidak_tertarik", "tidak_minat" -> TIDAK_TERTARIK;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return label;
    }
}
