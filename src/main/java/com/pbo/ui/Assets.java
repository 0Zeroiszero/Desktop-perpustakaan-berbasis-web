package com.pbo.ui;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/** Membaca berkas yang dicatat database (cover/…, ebook/…) dari folder assets. */
public final class Assets {

    private static final Path SOURCE = Path.of("src", "main", "resources", "assets");
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Assets() {
    }

    public static BufferedImage image(String relative) {
        if (relative == null || relative.isBlank()) {
            return null;
        }
        return CACHE.computeIfAbsent(relative, Assets::readImage);
    }

    public static void open(String relative) throws IOException {
        if (relative == null || relative.isBlank() || "-".equals(relative)) {
            throw new IOException("Berkas e-book belum dicatat untuk buku ini.");
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new IOException("Sistem tidak mendukung pembukaan berkas secara langsung.");
        }

        InputStream stream = resource(relative);
        if (stream == null) {
            Path source = SOURCE.resolve(relative);
            if (!Files.exists(source)) {
                throw new IOException("Berkas tidak ditemukan: " + relative);
            }
            Desktop.getDesktop().open(source.toFile());
            return;
        }

        int dot = relative.lastIndexOf('.');
        String suffix = dot >= 0 ? relative.substring(dot) : "";
        Path temp = Files.createTempFile("pustaka-", suffix);
        temp.toFile().deleteOnExit();
        try (stream) {
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
        }
        Desktop.getDesktop().open(temp.toFile());
    }

    private static BufferedImage readImage(String relative) {
        InputStream stream = resource(relative);
        if (stream != null) {
            try (stream) {
                return ImageIO.read(stream);
            } catch (IOException e) {
                System.err.println("Gagal membaca gambar " + relative + ": " + e.getMessage());
            }
        }
        Path source = SOURCE.resolve(relative);
        if (Files.exists(source)) {
            try {
                return ImageIO.read(source.toFile());
            } catch (IOException e) {
                System.err.println("Gagal membaca gambar " + relative + ": " + e.getMessage());
            }
        }
        System.err.println("Gambar tidak ditemukan: " + relative);
        return null;
    }

    private static InputStream resource(String relative) {
        return Assets.class.getResourceAsStream("/assets/" + relative.replace('\\', '/'));
    }
}
