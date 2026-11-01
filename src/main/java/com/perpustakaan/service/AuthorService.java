package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.repository.IdSequenceRepository;
import com.perpustakaan.security.AccessControl;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.ValidationUtil;

import java.util.List;

/**
 * Operasi penulis untuk admin dan daftar penulis untuk form buku.
 */
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final IdSequenceRepository idSequenceRepository;
    private final TransactionManager transactionManager;

    public AuthorService(AuthorRepository authorRepository, IdSequenceRepository idSequenceRepository,
                         TransactionManager transactionManager) {
        this.authorRepository = authorRepository;
        this.idSequenceRepository = idSequenceRepository;
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
        if (validated.email() != null && authorRepository.emailExists(validated.email(), null)) {
            throw new ValidationException("email", "Email penulis sudah digunakan.");
        }
        Author author = new Author();
        author.setNamaDepan(validated.namaDepan());
        author.setNamaBelakang(validated.namaBelakang());
        author.setEmail(validated.email());
        author.setNegaraAsal(validated.negaraAsal());
        author.setBio(validated.bio());
        transactionManager.execute(connection -> {
            author.setIdAuthor(idSequenceRepository.nextId(
                    connection, IdSequenceRepository.EntityType.AUTHOR));
            authorRepository.insert(connection, author);
            return author;
        });
        AppLogger.info("Admin " + actor.getUsername() + " membuat penulis " + author.getNamaLengkap());
        return author;
    }

    public Author update(User actor, String idAuthor, AuthorFormData data) {
        AccessControl.requireAdmin(actor);
        Author existing = authorRepository.findById(idAuthor);
        if (existing == null) {
            throw new AppException("Penulis tidak ditemukan.");
        }
        ValidatedData validated = validate(data);
        if (validated.email() != null && authorRepository.emailExists(validated.email(), idAuthor)) {
            throw new ValidationException("email", "Email penulis sudah digunakan.");
        }
        existing.setNamaDepan(validated.namaDepan());
        existing.setNamaBelakang(validated.namaBelakang());
        existing.setEmail(validated.email());
        existing.setNegaraAsal(validated.negaraAsal());
        existing.setBio(validated.bio());
        transactionManager.execute(connection -> {
            authorRepository.update(connection, existing);
            return null;
        });
        AppLogger.info("Admin " + actor.getUsername() + " memperbarui penulis " + existing.getNamaLengkap());
        return existing;
    }

    public void delete(User actor, String idAuthor) {
        AccessControl.requireAdmin(actor);
        Author existing = authorRepository.findById(idAuthor);
        if (existing == null) {
            throw new AppException("Penulis tidak ditemukan.");
        }
        long jumlahBuku = authorRepository.countBooks(idAuthor);
        if (jumlahBuku > 0) {
            throw new ValidationException("id",
                    "Penulis masih terhubung dengan " + jumlahBuku + " buku. Lepaskan relasinya terlebih dahulu.");
        }
        transactionManager.execute(connection -> {
            authorRepository.delete(connection, idAuthor);
            return null;
        });
        AppLogger.info("Admin " + actor.getUsername() + " menghapus penulis " + existing.getNamaLengkap());
    }

    private ValidatedData validate(AuthorFormData data) {
        String namaDepan = ValidationUtil.required(data.namaDepan(), "namaDepan", "Nama depan", 50);
        String namaBelakang = ValidationUtil.optional(data.namaBelakang(), "namaBelakang", "Nama belakang", 50);
        String email = ValidationUtil.optional(data.email(), "email", "Email", 100);
        if (email != null) {
            ValidationUtil.email(email, "email");
        }
        String negara = ValidationUtil.optional(data.negaraAsal(), "negaraAsal", "Negara asal", 50);
        String bio = ValidationUtil.optional(data.bio(), "bio", "Bio", 500);
        return new ValidatedData(namaDepan, namaBelakang, email, negara, bio);
    }

    public record AuthorFormData(
            String namaDepan,
            String namaBelakang,
            String email,
            String negaraAsal,
            String bio
    ) {
    }

    private record ValidatedData(
            String namaDepan,
            String namaBelakang,
            String email,
            String negaraAsal,
            String bio
    ) {
    }
}
