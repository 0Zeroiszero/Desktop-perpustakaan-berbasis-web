package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.security.AccessControl;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.SlugUtil;
import com.perpustakaan.util.ValidationUtil;

import java.sql.Connection;
import java.util.List;

/**
 * Operasi penulis untuk admin dan daftar penulis untuk form buku.
 * Penulis tidak dihapus fisik karena memiliki relasi ke buku.
 */
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final TransactionManager transactionManager;

    public AuthorService(AuthorRepository authorRepository, TransactionManager transactionManager) {
        this.authorRepository = authorRepository;
        this.transactionManager = transactionManager;
    }

    public PageResult<Author> list(User actor, String search, int page, int pageSize) {
        AccessControl.requireAdmin(actor);
        return authorRepository.findAll(search, page, pageSize);
    }

    public List<Author> listAll(User actor) {
        AccessControl.requireAuthenticated(actor);
        return authorRepository.findAll();
    }

    public Author create(User actor, AuthorFormData data) {
        AccessControl.requireAdmin(actor);
        ValidatedData validated = validate(data);
        if (authorRepository.nameExists(validated.nama(), null)) {
            throw new ValidationException("nama", "Nama penulis sudah digunakan.");
        }
        Author author = new Author();
        author.setNamaLengkap(validated.nama());
        author.setNegaraAsal(validated.negaraAsal());
        author.setBio(validated.bio());

        transactionManager.execute(connection -> {
            author.setSlug(uniqueSlug(connection, validated.nama(), null));
            authorRepository.insert(connection, author);
            return author;
        });
        AppLogger.info("Admin " + actor.getEmail() + " membuat penulis " + author.getNamaLengkap());
        return author;
    }

    public Author update(User actor, String idAuthor, AuthorFormData data) {
        AccessControl.requireAdmin(actor);
        Author existing = authorRepository.findById(idAuthor);
        if (existing == null) {
            throw new AppException("Penulis tidak ditemukan.");
        }
        ValidatedData validated = validate(data);
        if (authorRepository.nameExists(validated.nama(), idAuthor)) {
            throw new ValidationException("nama", "Nama penulis sudah digunakan.");
        }
        existing.setNamaLengkap(validated.nama());
        existing.setNegaraAsal(validated.negaraAsal());
        existing.setBio(validated.bio());

        transactionManager.execute(connection -> {
            existing.setSlug(uniqueSlug(connection, validated.nama(), idAuthor));
            authorRepository.update(connection, existing);
            return null;
        });
        AppLogger.info("Admin " + actor.getEmail() + " memperbarui penulis " + existing.getNamaLengkap());
        return existing;
    }

    /** Penulis yang memiliki buku tidak dapat dihapus; ubah datanya saja. */
    public void delete(User actor, String idAuthor) {
        AccessControl.requireAdmin(actor);
        throw new AppException("Penulis tidak dapat dihapus. Silakan ubah data penulis tersebut.");
    }

    private String uniqueSlug(Connection connection, String name, String excludeId) {
        return SlugUtil.unique(SlugUtil.slugify(name),
                candidate -> authorRepository.slugExists(connection, candidate, excludeId));
    }

    private ValidatedData validate(AuthorFormData data) {
        String nama = ValidationUtil.required(data.nama(), "nama", "Nama penulis", 100);
        String negara = ValidationUtil.optional(data.negaraAsal(), "negaraAsal", "Negara asal", 60);
        String bio = ValidationUtil.optional(data.bio(), "bio", "Bio", 2000);
        return new ValidatedData(nama, negara == null ? "" : negara, bio);
    }

    public record AuthorFormData(String nama, String negaraAsal, String bio) {
        /** Kompatibilitas dengan form lama yang memisahkan nama depan dan belakang. */
        public AuthorFormData(String namaDepan, String namaBelakang, String email,
                              String negaraAsal, String bio) {
            this(((namaDepan == null ? "" : namaDepan) + " "
                    + (namaBelakang == null ? "" : namaBelakang)).trim(), negaraAsal, bio);
        }
    }

    private record ValidatedData(String nama, String negaraAsal, String bio) {
    }
}
