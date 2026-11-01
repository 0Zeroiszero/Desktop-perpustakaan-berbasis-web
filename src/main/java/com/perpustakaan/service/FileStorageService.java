package com.perpustakaan.service;

import com.perpustakaan.config.AppConfig;
import com.perpustakaan.config.AppPaths;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.util.AppLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Menyimpan cover dan berkas PDF ke storage dengan nama UUID.
 */
public class FileStorageService {

    private final AppConfig config;

    public FileStorageService(AppConfig config) {
        this.config = config;
    }

    public String saveCover(Path source) {
        validateSource(source, "cover", "Cover", config.getMaxCoverBytes());
        String extension = extension(source);
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw new ValidationException("cover", "Cover harus berupa JPG atau PNG.");
        }
        byte[] header = readHeader(source, "cover");
        boolean jpeg = isJpeg(header);
        boolean png = isPng(header);
        if ((extension.equals("png") && !png) || (!extension.equals("png") && !jpeg)) {
            throw new ValidationException("cover", "Isi berkas cover tidak sesuai dengan ekstensinya.");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destination = AppPaths.getCoverDir().resolve(fileName);
        copy(source, destination, "cover");
        return AppPaths.toStoredPath(destination);
    }

    public String saveEbookPdf(Path source) {
        validateSource(source, "file", "Berkas e-book", config.getMaxEbookBytes());
        if (!extension(source).equals("pdf")) {
            throw new ValidationException("file", "Berkas e-book harus berformat PDF.");
        }
        byte[] header = readHeader(source, "file");
        if (!isPdf(header)) {
            throw new ValidationException("file", "Isi berkas bukan PDF yang valid.");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
        Path destination = AppPaths.getEbookDir().resolve(fileName);
        copy(source, destination, "file");
        return AppPaths.toStoredPath(destination);
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

    private void copy(Path source, Path destination, String field) {
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException("Gagal menyimpan " + (field.equals("cover") ? "cover" : "berkas e-book") + ".", e);
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
}
