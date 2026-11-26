package com.perpustakaan.config;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Menentukan lokasi folder aplikasi dan storage milik proyek ini.
 * Aplikasi tidak membaca folder proyek web: semua PDF dan cover ada di
 * {@code <app.home>/storage}. Path di database tetap memakai format web
 * ({@code storage/ebooks/1.pdf}, {@code uploads/covers/1.jpg}) dan diterjemahkan
 * di sini ke file lokal.
 */
public final class AppPaths {

    private static final Path APP_HOME = resolveAppHome();
    private static final Path STORAGE_ROOT = APP_HOME.resolve("storage");
    private static final Path COVER_DIR = STORAGE_ROOT.resolve("cover");
    private static final Path EBOOK_DIR = STORAGE_ROOT.resolve("ebook");
    private static final Path LOGS_DIR = APP_HOME.resolve("logs");

    private AppPaths() {
    }

    private static Path resolveAppHome() {
        String override = System.getProperty("app.home");
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }
        try {
            URI uri = AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path location = Paths.get(uri).toAbsolutePath().normalize();
            if (Files.isDirectory(location)) {
                if ("classes".equals(fileName(location)) && "target".equals(fileName(location.getParent()))) {
                    return location.getParent().getParent();
                }
                return location;
            }
            Path parent = location.getParent();
            if (parent != null && "target".equals(fileName(parent))) {
                return parent.getParent();
            }
            return parent == null ? Paths.get("").toAbsolutePath().normalize() : parent;
        } catch (Exception e) {
            return Paths.get("").toAbsolutePath().normalize();
        }
    }

    private static String fileName(Path path) {
        return path == null || path.getFileName() == null ? "" : path.getFileName().toString();
    }

    public static void ensureDirectories() {
        try {
            Files.createDirectories(EBOOK_DIR);
            Files.createDirectories(COVER_DIR);
            Files.createDirectories(LOGS_DIR);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membuat direktori aplikasi: " + STORAGE_ROOT, e);
        }
    }

    public static Path getAppHome() {
        return APP_HOME;
    }

    public static Path getStorageRoot() {
        return STORAGE_ROOT;
    }

    public static Path getCoverDir() {
        return COVER_DIR;
    }

    public static Path getEbookDir() {
        return EBOOK_DIR;
    }

    public static Path getLogsDir() {
        return LOGS_DIR;
    }

    /**
     * Mengubah path relatif di database menjadi file di storage lokal.
     * Format web dan format lama desktop sama-sama diterima.
     */
    public static Path resolveStoredPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        String value = storedPath.trim().replace('\\', '/');
        while (value.startsWith("./")) {
            value = value.substring(2);
        }
        if (value.contains("..") || Paths.get(value).isAbsolute()) {
            return null;
        }

        String name;
        if (value.startsWith("storage/ebooks/")) {
            name = value.substring("storage/ebooks/".length());
        } else if (value.startsWith("storage/ebook/")) {
            name = value.substring("storage/ebook/".length());
        } else if (value.startsWith("uploads/covers/")) {
            name = value.substring("uploads/covers/".length());
        } else if (value.startsWith("storage/cover/")) {
            name = value.substring("storage/cover/".length());
        } else {
            name = value;
        }
        if (name.isBlank() || name.contains("/")) {
            return null;
        }

        Path directory = name.toLowerCase().endsWith(".pdf") ? EBOOK_DIR : COVER_DIR;
        Path candidate = directory.resolve(name).normalize();
        return candidate.startsWith(STORAGE_ROOT) ? candidate : null;
    }

    /** Mengubah file lokal menjadi path relatif berformat web untuk disimpan di database. */
    public static String toStoredPath(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        if (normalized.startsWith(EBOOK_DIR)) {
            return "storage/ebooks/" + EBOOK_DIR.relativize(normalized).toString().replace('\\', '/');
        }
        if (normalized.startsWith(COVER_DIR)) {
            return "uploads/covers/" + COVER_DIR.relativize(normalized).toString().replace('\\', '/');
        }
        throw new IllegalArgumentException("File berada di luar direktori storage.");
    }

    /** Path relatif storage untuk berkas PDF berdasarkan ID numerik buku. */
    public static String ebookStoredPath(String bookId) {
        return "storage/ebooks/" + bookId + ".pdf";
    }

    /** Path relatif storage untuk cover berdasarkan ID numerik buku. */
    public static String coverStoredPath(String bookId) {
        return "uploads/covers/" + bookId + ".jpg";
    }

    public static boolean exists(String storedPath) {
        Path path = resolveStoredPath(storedPath);
        return path != null && Files.isRegularFile(path);
    }

    public static String safeFileName(String name) {
        if (name == null) {
            return "berkas";
        }
        String safe = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (safe.isBlank()) {
            safe = "berkas";
        }
        return safe.length() > 120 ? safe.substring(0, 120) : safe;
    }
}
