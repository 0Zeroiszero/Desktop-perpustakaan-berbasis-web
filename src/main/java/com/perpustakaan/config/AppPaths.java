package com.perpustakaan.config;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Menentukan lokasi direktori aplikasi dan storage secara aman.
 */
public final class AppPaths {

    private static final Path APP_HOME = resolveAppHome();
    private static final Path STORAGE_ROOT = resolveStorageRoot();
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

    private static Path resolveStorageRoot() {
        String configured = AppConfig.getInstance().getStorageDir();
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = APP_HOME.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    public static void ensureDirectories() {
        try {
            Files.createDirectories(STORAGE_ROOT);
            Files.createDirectories(COVER_DIR);
            Files.createDirectories(EBOOK_DIR);
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
     * Mengubah path relatif dari database menjadi path absolut di dalam storage.
     * Mengembalikan null bila path tidak aman atau berada di luar storage.
     */
    public static Path resolveStoredPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        try {
            Path candidate = STORAGE_ROOT.resolve(storedPath).normalize();
            if (!candidate.startsWith(STORAGE_ROOT) || candidate.equals(STORAGE_ROOT)) {
                return null;
            }
            return candidate;
        } catch (Exception e) {
            return null;
        }
    }

    public static String toStoredPath(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        if (!normalized.startsWith(STORAGE_ROOT)) {
            throw new IllegalArgumentException("File berada di luar direktori storage.");
        }
        return STORAGE_ROOT.relativize(normalized).toString().replace('\\', '/');
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
