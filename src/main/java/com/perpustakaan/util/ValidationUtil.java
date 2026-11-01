package com.perpustakaan.util;

import com.perpustakaan.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.regex.Pattern;

/**
 * Validasi input terpusat. Semua pesan menggunakan bahasa Indonesia.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");
    private static final Pattern ISBN = Pattern.compile("^[0-9Xx\\-\\s]{10,20}$");

    private ValidationUtil() {
    }

    public static String required(String value, String field, String label, int maxLength) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new ValidationException(field, label + " wajib diisi.");
        }
        if (trimmed.length() > maxLength) {
            throw new ValidationException(field, label + " maksimal " + maxLength + " karakter.");
        }
        return trimmed;
    }

    public static String optional(String value, String field, String label, int maxLength) {
        String trimmed = trimToNull(value);
        if (trimmed != null && trimmed.length() > maxLength) {
            throw new ValidationException(field, label + " maksimal " + maxLength + " karakter.");
        }
        return trimmed;
    }

    public static void email(String value, String field) {
        String email = required(value, field, "Email", 100);
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException(field, "Format email tidak valid.");
        }
    }

    public static void username(String value, String field) {
        String username = required(value, field, "Username", 50);
        if (!USERNAME.matcher(username).matches()) {
            throw new ValidationException(field,
                    "Username 3-50 karakter dan hanya boleh berisi huruf, angka, titik, garis bawah, atau tanda hubung.");
        }
    }

    public static void password(String value, String field) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(field, "Password wajib diisi.");
        }
        if (value.length() < 8) {
            throw new ValidationException(field, "Password minimal 8 karakter.");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ValidationException(field, "Password terlalu panjang (maksimal 72 byte).");
        }
    }

    public static void passwordConfirmation(String password, String confirmation, String field) {
        if (password == null || !password.equals(confirmation)) {
            throw new ValidationException(field, "Konfirmasi password tidak sama.");
        }
    }

    public static String phone(String value, String field) {
        String phone = optional(value, field, "Nomor ponsel", 20);
        if (phone != null && !PHONE.matcher(phone).matches()) {
            throw new ValidationException(field, "Format nomor ponsel tidak valid.");
        }
        return phone;
    }

    public static String isbn(String value, String field) {
        String isbn = optional(value, field, "ISBN", 20);
        if (isbn != null && !ISBN.matcher(isbn).matches()) {
            throw new ValidationException(field, "Format ISBN tidak valid (10-20 digit, boleh mengandung tanda hubung).");
        }
        return isbn;
    }

    public static Integer parseInteger(String value, String field, String label,
                                       int min, int max, boolean required) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            if (required) {
                throw new ValidationException(field, label + " wajib diisi.");
            }
            return null;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            if (parsed < min || parsed > max) {
                throw new ValidationException(field,
                        label + " harus antara " + min + " dan " + max + ".");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new ValidationException(field, label + " harus berupa angka.");
        }
    }

    public static void year(Integer year, String field) {
        if (year == null) {
            return;
        }
        int current = Year.now().getValue();
        if (year < 1000 || year > current + 1) {
            throw new ValidationException(field, "Tahun terbit harus antara 1000 dan " + (current + 1) + ".");
        }
    }

    public static void pages(Integer pages, String field) {
        if (pages == null) {
            return;
        }
        if (pages < 1 || pages > 100_000) {
            throw new ValidationException(field, "Jumlah halaman harus antara 1 dan 100.000.");
        }
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
