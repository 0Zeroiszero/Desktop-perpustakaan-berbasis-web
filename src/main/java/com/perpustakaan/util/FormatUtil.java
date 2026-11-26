package com.perpustakaan.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Format tampilan yang konsisten untuk seluruh UI.
 */
public final class FormatUtil {

    private static final Locale INDONESIAN = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", INDONESIAN);
    private static final DateTimeFormatter DATE_LONG_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", INDONESIAN);

    private FormatUtil() {
    }

    public static String date(LocalDate date) {
        return date == null ? "-" : DATE_FORMAT.format(date);
    }

    public static String dateLong(LocalDate date) {
        return date == null ? "-" : DATE_LONG_FORMAT.format(date);
    }

    public static String percent(BigDecimal value) {
        if (value == null) {
            return "0%";
        }
        BigDecimal clamped = value.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        return clamped.setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    public static String percentDecimal(BigDecimal value) {
        if (value == null) {
            return "0,0%";
        }
        BigDecimal clamped = value.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        return clamped.setScale(1, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + "%";
    }

    public static String number(long value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(INDONESIAN);
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols).format(value);
    }

    public static String fileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(INDONESIAN, "%.1f KB", kb);
        }
        return String.format(INDONESIAN, "%.1f MB", kb / 1024.0);
    }

    public static String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 11) {
            return "Selamat pagi";
        }
        if (hour < 15) {
            return "Selamat siang";
        }
        if (hour < 19) {
            return "Selamat sore";
        }
        return "Selamat malam";
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }

    public static String year(Integer year) {
        return year == null ? "-" : String.valueOf(year);
    }

    public static String pages(Integer pages) {
        return pages == null || pages <= 0 ? "-" : number(pages) + " halaman";
    }

    public static String genderLabel(String code) {
        if ("L".equalsIgnoreCase(code)) {
            return "Laki-laki";
        }
        if ("P".equalsIgnoreCase(code)) {
            return "Perempuan";
        }
        return "Tidak disebutkan";
    }

    public static String genderCode(String label) {
        if (label == null) {
            return null;
        }
        String normalized = label.trim().toLowerCase(INDONESIAN);
        if (normalized.startsWith("laki")) {
            return "L";
        }
        if (normalized.startsWith("perempuan")) {
            return "P";
        }
        return null;
    }

    public static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.substring(0, 1).toUpperCase(INDONESIAN) + trimmed.substring(1);
    }
}
