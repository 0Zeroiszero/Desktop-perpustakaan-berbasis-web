package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.KategoriRepository;
import com.perpustakaan.security.AccessControl;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.ValidationUtil;

import java.util.List;

/**
 * Operasi genre/kategori untuk admin dan filter katalog.
 */
public class KategoriService {

    private final KategoriRepository kategoriRepository;
    private final TransactionManager transactionManager;

    public KategoriService(KategoriRepository kategoriRepository, TransactionManager transactionManager) {
        this.kategoriRepository = kategoriRepository;
        this.transactionManager = transactionManager;
    }

    public PageResult<Kategori> list(User actor, String search, int page, int pageSize) {
        AccessControl.requireAdmin(actor);
        return kategoriRepository.findAll(search, page, pageSize);
    }

    public List<Kategori> listAll(User actor) {
        AccessControl.requireAuthenticated(actor);
        return kategoriRepository.findAll();
    }

    public Kategori create(User actor, KategoriFormData data) {
        AccessControl.requireAdmin(actor);
        String nama = ValidationUtil.required(data.namaKategori(), "namaKategori", "Nama kategori", 100);
        String deskripsi = ValidationUtil.optional(data.deskripsi(), "deskripsi", "Deskripsi", 2000);
        if (kategoriRepository.nameExists(nama, null)) {
            throw new ValidationException("namaKategori", "Nama kategori sudah digunakan.");
        }
        Kategori kategori = new Kategori();
        kategori.setNamaKategori(nama);
        kategori.setDeskripsi(deskripsi);
        transactionManager.execute(connection -> {
            kategoriRepository.insert(connection, kategori);
            return kategori;
        });
        AppLogger.info("Admin " + actor.getEmail() + " membuat kategori " + nama);
        return kategori;
    }

    public Kategori update(User actor, String idKategori, KategoriFormData data) {
        AccessControl.requireAdmin(actor);
        Kategori existing = kategoriRepository.findById(idKategori);
        if (existing == null) {
            throw new AppException("Kategori tidak ditemukan.");
        }
        String nama = ValidationUtil.required(data.namaKategori(), "namaKategori", "Nama kategori", 100);
        String deskripsi = ValidationUtil.optional(data.deskripsi(), "deskripsi", "Deskripsi", 2000);
        if (kategoriRepository.nameExists(nama, idKategori)) {
            throw new ValidationException("namaKategori", "Nama kategori sudah digunakan.");
        }
        existing.setNamaKategori(nama);
        existing.setDeskripsi(deskripsi);
        transactionManager.execute(connection -> {
            kategoriRepository.update(connection, existing);
            return null;
        });
        AppLogger.info("Admin " + actor.getEmail() + " memperbarui kategori " + nama);
        return existing;
    }

    /** Genre tidak dihapus fisik agar relasi buku tetap valid. */
    public void delete(User actor, String idKategori) {
        AccessControl.requireAdmin(actor);
        throw new AppException("Kategori tidak dapat dihapus. Silakan ubah data kategori tersebut.");
    }

    public record KategoriFormData(String namaKategori, String deskripsi) {
    }
}
