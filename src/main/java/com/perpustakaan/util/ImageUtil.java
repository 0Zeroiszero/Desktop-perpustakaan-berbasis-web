package com.perpustakaan.util;

import com.perpustakaan.config.AppPaths;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memuat gambar cover dan mengubah BufferedImage hasil render PDF ke JavaFX Image.
 */
public final class ImageUtil {

    private static final Map<String, CachedImage> CACHE = new ConcurrentHashMap<>();

    private ImageUtil() {
    }

    public static Image loadCover(String storedPath, double width, double height) {
        Path path = AppPaths.resolveStoredPath(storedPath);
        return loadImage(path, width, height);
    }

    public static Image loadImage(Path path, double width, double height) {
        if (path == null || !Files.isRegularFile(path)) {
            return null;
        }
        try {
            long modified = Files.getLastModifiedTime(path).toMillis();
            String key = path.toAbsolutePath().normalize() + "|" + Math.round(width) + "x" + Math.round(height) + "|" + modified;
            CachedImage cached = CACHE.get(key);
            if (cached != null) {
                return cached.image();
            }
            Image image = new Image(path.toUri().toString(), width, height, true, true, false);
            if (image.isError()) {
                AppLogger.warn("Gagal memuat gambar: " + path, image.getException());
                return null;
            }
            CACHE.put(key, new CachedImage(modified, image));
            return image;
        } catch (Exception e) {
            AppLogger.warn("Gagal memuat gambar: " + path, e);
            return null;
        }
    }

    public static Image toFxImage(BufferedImage image) {
        return image == null ? null : SwingFXUtils.toFXImage(image, null);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private record CachedImage(long modified, Image image) {
    }
}
