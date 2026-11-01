package com.perpustakaan.model;

import java.util.Locale;

/**
 * Format berkas yang tercatat pada tabel ebook.
 */
public enum BookFormat {
    PDF("PDF", true),
    EPUB("EPUB", false),
    MOBI("MOBI", false),
    UNKNOWN("Lainnya", false);

    private final String displayName;
    private final boolean readerSupported;

    BookFormat(String displayName, boolean readerSupported) {
        this.displayName = displayName;
        this.readerSupported = readerSupported;
    }

    public String displayName() {
        return displayName;
    }

    public boolean supportsReader() {
        return readerSupported;
    }

    public static BookFormat fromDatabase(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "PDF" -> PDF;
            case "EPUB" -> EPUB;
            case "MOBI" -> MOBI;
            default -> UNKNOWN;
        };
    }

    public static BookFormat fromExtension(String fileName) {
        if (fileName == null) {
            return UNKNOWN;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return PDF;
        }
        if (lower.endsWith(".epub")) {
            return EPUB;
        }
        if (lower.endsWith(".mobi")) {
            return MOBI;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
