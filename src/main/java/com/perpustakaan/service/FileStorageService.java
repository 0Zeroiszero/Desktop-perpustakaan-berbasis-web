package com.perpustakaan.service;

import com.perpustakaan.config.AppConfig;
import com.perpustakaan.config.AppPaths;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.util.AppLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Menyimpan cover dan PDF di storage milik proyek desktop dengan nama numerik sesuai ID buku.
 */
public class FileStorageService {

    private final AppConfig config;

    public FileStorageService(AppConfig config) {
        this.config = config;
    }

    public String saveEbookPdf(Path source, String bookId) {
        validateSource(source, "file", "Berkas e-book", config.getMaxEbookBytes());
        if (!extension(source).equals("pdf")) {
            throw new ValidationException("file", "Berkas e-book harus berformat PDF.");
        }
        if (!isPdf(readHeader(source, "file"))) {
            throw new ValidationException("file", "Isi berkas bukan PDF yang valid.");
        }
        Path destination = AppPaths.getEbookDir().resolve(bookId + ".pdf");
        copy(source, destination, "berkas e-book");
        return AppPaths.ebookStoredPath(bookId);
    }

    /** Cover disimpan sebagai {book_id}.jpg agar sesuai path aplikasi web. */
    public String saveCover(Path source, String bookId) {
        validateSource(source, "cover", "Cover", config.getMaxCoverBytes());
        String extension = extension(source);
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw new ValidationException("cover", "Cover harus berupa JPG atau PNG.");
        }
        byte[] header = readHeader(source, "cover");
        if (extension.equals("png") ? !isPng(header) : !isJpeg(header)) {
            throw new ValidationException("cover", "Isi berkas cover tidak sesuai dengan ekstensinya.");
        }

        Path destination = AppPaths.getCoverDir().resolve(bookId + ".jpg");
        try {
            Files.createDirectories(destination.getParent());
            if (extension.equals("png")) {
                BufferedImage image = ImageIO.read(source.toFile());
                if (image == null) {
                    throw new ValidationException("cover", "Cover tidak dapat dibaca.");
                }
                ImageIO.write(image, "jpg", destination.toFile());
            } else {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AppException("Gagal menyimpan cover.", e);
        }
        return AppPaths.coverStoredPath(bookId);
    }

    /** Ukuran dan checksum SHA-256 berkas yang akan dicatat di tabel books. */
    public FileInfo inspect(Path source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            try (InputStream input = Files.newInputStream(source)) {
                while ((read = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            StringBuilder hash = new StringBuilder();
            for (byte value : digest.digest()) {
                hash.append(String.format(Locale.ROOT, "%02x", value & 0xFF));
            }
            return new FileInfo(Files.size(source), hash.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("Algoritma SHA-256 tidak tersedia.", e);
        } catch (IOException e) {
            throw new AppException("Gagal membaca berkas e-book.", e);
        }
    }

    public boolean exists(String storedPath) {
        return AppPaths.exists(storedPath);
    }

    public void delete(String storedPath) {
        Path path = AppPaths.resolveStoredPath(storedPath);
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            AppLogger.warn("Gagal menghapus berkas storage: " + path, e);
        }
    }

    private void validateSource(Path source, String field, String label, long maxBytes) {
        if (source == null) {
            throw new ValidationException(field, label + " belum dipilih.");
        }
        if (!Files.isRegularFile(source)) {
            throw new ValidationException(field, label + " tidak ditemukan.");
        }
        try {
            long size = Files.size(source);
            if (size <= 0) {
                throw new ValidationException(field, label + " kosong.");
            }
            if (size > maxBytes) {
                throw new ValidationException(field,
                        label + " terlalu besar. Maksimal " + (maxBytes / (1024 * 1024)) + " MB.");
            }
        } catch (IOException e) {
            throw new ValidationException(field, label + " tidak dapat dibaca.");
        }
    }

    private void copy(Path source, Path destination, String label) {
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException("Gagal menyimpan " + label + ".", e);
        }
    }

    private String extension(Path source) {
        String name = source.getFileName() == null ? "" : source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private byte[] readHeader(Path source, String field) {
        byte[] header = new byte[8];
        try (InputStream input = Files.newInputStream(source)) {
            int read = input.read(header);
            if (read < 4) {
                throw new ValidationException(field, "Berkas terlalu kecil atau rusak.");
            }
            return header;
        } catch (IOException e) {
            throw new ValidationException(field, "Berkas tidak dapat dibaca.");
        }
    }

    private boolean isJpeg(byte[] header) {
        return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] header) {
        return (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A;
    }

    private boolean isPdf(byte[] header) {
        return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46
                && header[4] == 0x2D;
    }

    public record FileInfo(long size, String sha256) {
    }
}
