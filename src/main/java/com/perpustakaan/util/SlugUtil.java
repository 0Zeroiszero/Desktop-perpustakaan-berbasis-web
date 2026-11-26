package com.perpustakaan.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Pembuatan slug URL yang unik (huruf kecil, tanda hubung, tanpa aksen).
 */
public final class SlugUtil {

    private SlugUtil() {
    }

    public static String slugify(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        return normalized.trim().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-").replaceAll("^-|-$", "");
    }

    /** Menambahkan -2, -3, ... bila slug sudah dipakai. */
    public static String unique(String base, Predicate<String> taken) {
        String root = base.isBlank() ? "item" : base;
        String candidate = root;
        int suffix = 2;
        while (taken.test(candidate)) {
            candidate = root + "-" + suffix++;
        }
        return candidate;
    }
}
