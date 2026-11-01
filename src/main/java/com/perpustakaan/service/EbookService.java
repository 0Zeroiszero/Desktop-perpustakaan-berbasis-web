package com.perpustakaan.service;

import com.perpustakaan.config.AppPaths;
import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookFormat;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.repository.EbookAuthorRepository;
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.IdSequenceRepository;
import com.perpustakaan.repository.KategoriRepository;
import com.perpustakaan.repository.ReadingProgressRepository;
import com.perpustakaan.security.AccessControl;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.ValidationUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Operasi katalog dan pengelolaan e-book.
 */
public class EbookService {

    private final EbookRepository ebookRepository;
    private final EbookAuthorRepository ebookAuthorRepository;
    private final KategoriRepository kategoriRepository;
    private final AuthorRepository authorRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final IdSequenceRepository idSequenceRepository;
    private final TransactionManager transactionManager;
    private final FileStorageService fileStorageService;
    private final PdfService pdfService;

    public EbookService(EbookRepository ebookRepository, EbookAuthorRepository ebookAuthorRepository,
                        KategoriRepository kategoriRepository, AuthorRepository authorRepository,
                        ReadingProgressRepository readingProgressRepository,
                        IdSequenceRepository idSequenceRepository, TransactionManager transactionManager,
                        FileStorageService fileStorageService, PdfService pdfService) {
        this.ebookRepository = ebookRepository;
        this.ebookAuthorRepository = ebookAuthorRepository;
        this.kategoriRepository = kategoriRepository;
        this.authorRepository = authorRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.idSequenceRepository = idSequenceRepository;
        this.transactionManager = transactionManager;
        this.fileStorageService = fileStorageService;
        this.pdfService = pdfService;
    }

    public PageResult<BookListItem> listCatalog(User actor, String search, String kategoriId,
                                                BookFormat format, String sort, int page, int pageSize) {
        AccessControl.requireAuthenticated(actor);
        return ebookRepository.findBooks(search, kategoriId, format, sort, page, pageSize, actor.getIdUser());
    }

    public PageResult<BookListItem> listAdmin(User actor, String search, String kategoriId,
                                              BookFormat format, String sort, int page, int pageSize) {
        AccessControl.requireAdmin(actor);
        return ebookRepository.findBooks(search, kategoriId, format, sort, page, pageSize, null);
    }

    public BookDetail detail(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        if (bookId == null || bookId.isBlank()) {
            throw new AppException("Buku tidak ditemukan.");
        }
        BookDetail detail = ebookRepository.findDetail(bookId, actor.getIdUser());
        if (detail == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        return detail;
    }

    public List<Kategori> categories(User actor) {
        AccessControl.requireAuthenticated(actor);
        return kategoriRepository.findAll();
    }

    public List<Author> authors(User actor) {
        AccessControl.requireAuthenticated(actor);
        return authorRepository.findAll();
    }

    public List<BookListItem> latest(User actor, int limit) {
        AccessControl.requireAuthenticated(actor);
        return ebookRepository.findBooks(null, null, null, "terbaru", 1, limit, actor.getIdUser()).items();
    }

    public Ebook create(User actor, BookFormData data) {
        AccessControl.requireAdmin(actor);
        ValidatedBookData validated = validateBookData(data, null);
        if (validated.format() != BookFormat.PDF) {
            throw new ValidationException("format", "Buku baru harus berformat PDF.");
        }
        if (data.ebookSource() == null) {
            throw new ValidationException("file", "Berkas PDF wajib dipilih.");
        }
        ensureCategory(validated.kategoriId());
        ensureAuthors(validated.authorIds());
        if (ebookRepository.isbnExists(validated.isbn(), null)) {
            throw new ValidationException("isbn", "ISBN sudah digunakan oleh buku lain.");
        }

        String newCover = null;
        String newFile = null;
        boolean coverSaved = false;
        boolean fileSaved = false;
        try {
            if (data.coverSource() != null) {
                newCover = fileStorageService.saveCover(data.coverSource());
                coverSaved = true;
            }
            newFile = fileStorageService.saveEbookPdf(data.ebookSource());
            fileSaved = true;

            int actualPages = pdfService.getPageCount(AppPaths.resolveStoredPath(newFile));
            validatePageCount(validated.jumlahHalaman(), actualPages);

            Ebook ebook = new Ebook();
            ebook.setJudul(validated.judul());
            ebook.setTahunTerbit(validated.tahunTerbit());
            ebook.setJumlahHalaman(actualPages);
            ebook.setIsbn(validated.isbn());
            ebook.setPenerbit(validated.penerbit());
            ebook.setSinopsis(validated.sinopsis());
            ebook.setCoverPath(newCover);
            ebook.setFilePath(newFile);
            ebook.setFormat(BookFormat.PDF);
            ebook.setKategoriId(validated.kategoriId());

            transactionManager.execute(connection -> {
                ebook.setIdBook(idSequenceRepository.nextId(
                        connection, IdSequenceRepository.EntityType.BOOK));
                ebookRepository.insert(connection, ebook);
                ebookAuthorRepository.replaceForBook(connection, ebook.getIdBook(), validated.authorIds());
                return ebook;
            });
            AppLogger.info("Admin " + actor.getUsername() + " menambahkan buku " + ebook.getJudul());
            return ebook;
        } catch (RuntimeException e) {
            if (coverSaved) {
                fileStorageService.delete(newCover);
            }
            if (fileSaved) {
                fileStorageService.delete(newFile);
            }
            throw e;
        }
    }

    public Ebook update(User actor, String idBook, BookFormData data) {
        AccessControl.requireAdmin(actor);
        Ebook existing = ebookRepository.findById(idBook);
        if (existing == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        ValidatedBookData validated = validateBookData(data, existing);
        ensureCategory(validated.kategoriId());
        ensureAuthors(validated.authorIds());
        if (ebookRepository.isbnExists(validated.isbn(), idBook)) {
            throw new ValidationException("isbn", "ISBN sudah digunakan oleh buku lain.");
        }

        BookFormat effectiveFormat = data.ebookSource() != null ? BookFormat.PDF : existing.getFormat();
        if (data.ebookSource() != null && data.format() != null && data.format() != BookFormat.PDF) {
            throw new ValidationException("format", "Berkas yang diunggah harus PDF.");
        }
        if (effectiveFormat == BookFormat.PDF && data.ebookSource() == null
                && !AppPaths.exists(existing.getFilePath())) {
            throw new ValidationException("file", "Berkas PDF lama tidak ditemukan. Unggah PDF baru.");
        }

        String oldCover = existing.getCoverPath();
        String oldFile = existing.getFilePath();
        String newCover = oldCover;
        String newFile = oldFile;
        boolean coverSaved = false;
        boolean fileSaved = false;
        try {
            if (data.coverSource() != null) {
                newCover = fileStorageService.saveCover(data.coverSource());
                coverSaved = true;
            }
            if (data.ebookSource() != null) {
                newFile = fileStorageService.saveEbookPdf(data.ebookSource());
                fileSaved = true;
            }

            Integer pages = validated.jumlahHalaman();
            if (effectiveFormat == BookFormat.PDF) {
                if (fileSaved) {
                    Path pdfPath = AppPaths.resolveStoredPath(newFile);
                    int actualPages = pdfService.getPageCount(pdfPath);
                    validatePageCount(pages, actualPages);
                    pages = actualPages;
                } else {
                    // Jangan memaksa pembaca placeholder/PDF lama: jumlah halaman
                    // tercatat tetap dipertahankan agar metadata bisa disunting.
                    pages = existing.getJumlahHalaman();
                }
            } else if (pages == null) {
                pages = existing.getJumlahHalaman();
            }

            existing.setJudul(validated.judul());
            existing.setTahunTerbit(validated.tahunTerbit());
            existing.setJumlahHalaman(pages);
            existing.setIsbn(validated.isbn());
            existing.setPenerbit(validated.penerbit());
            existing.setSinopsis(validated.sinopsis());
            existing.setCoverPath(newCover);
            existing.setFilePath(newFile);
            existing.setFormat(effectiveFormat);
            existing.setKategoriId(validated.kategoriId());

            transactionManager.execute(connection -> {
                ebookRepository.update(connection, existing);
                ebookAuthorRepository.replaceForBook(connection, idBook, validated.authorIds());
                return null;
            });

            if (coverSaved && !Objects.equals(oldCover, newCover)
                    && !ebookRepository.isPathReferenced(oldCover, idBook)) {
                fileStorageService.delete(oldCover);
            }
            if (fileSaved && !Objects.equals(oldFile, newFile)
                    && !ebookRepository.isPathReferenced(oldFile, idBook)) {
                fileStorageService.delete(oldFile);
            }
            AppLogger.info("Admin " + actor.getUsername() + " memperbarui buku " + existing.getJudul());
            return existing;
        } catch (RuntimeException e) {
            if (coverSaved) {
                fileStorageService.delete(newCover);
            }
            if (fileSaved) {
                fileStorageService.delete(newFile);
            }
            throw e;
        }
    }

    public void delete(User actor, String idBook) {
        AccessControl.requireAdmin(actor);
        Ebook existing = ebookRepository.findById(idBook);
        if (existing == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        transactionManager.execute(connection -> {
            readingProgressRepository.deleteByBook(connection, idBook);
            ebookAuthorRepository.deleteByBook(connection, idBook);
            ebookRepository.delete(connection, idBook);
            return null;
        });
        if (!ebookRepository.isPathReferenced(existing.getCoverPath(), null)) {
            fileStorageService.delete(existing.getCoverPath());
        }
        if (!ebookRepository.isPathReferenced(existing.getFilePath(), null)) {
            fileStorageService.delete(existing.getFilePath());
        }
        AppLogger.info("Admin " + actor.getUsername() + " menghapus buku " + existing.getJudul());
    }

    public PdfService.PdfSession openPdf(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        Ebook ebook = ebookRepository.findById(bookId);
        if (ebook == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        if (!ebook.isReaderSupported()) {
            throw new AppException("Format " + ebook.getFormat().displayName()
                    + " belum didukung oleh pembaca PDF.");
        }
        Path file = AppPaths.resolveStoredPath(ebook.getFilePath());
        if (file == null || !Files.isRegularFile(file)) {
            throw new AppException("Berkas PDF tidak ditemukan di storage.");
        }
        return pdfService.open(file);
    }

    public boolean isFileAvailable(BookDetail detail) {
        return detail != null
                && detail.ebook().isReaderSupported()
                && AppPaths.exists(detail.ebook().getFilePath());
    }

    public int getPdfPageCount(Path file) {
        return pdfService.getPageCount(file);
    }

    private ValidatedBookData validateBookData(BookFormData data, Ebook existing) {
        String judul = ValidationUtil.required(data.judul(), "judul", "Judul buku", 255);
        Integer tahun = ValidationUtil.parseInteger(data.tahunTerbit(), "tahunTerbit", "Tahun terbit",
                1000, Year.now().getValue() + 1, false);
        Integer pages = ValidationUtil.parseInteger(data.jumlahHalaman(), "jumlahHalaman", "Jumlah halaman",
                1, 100_000, false);
        String isbn = ValidationUtil.isbn(data.isbn(), "isbn");
        String penerbit = ValidationUtil.optional(data.penerbit(), "penerbit", "Penerbit", 150);
        String sinopsis = ValidationUtil.optional(data.sinopsis(), "sinopsis", "Sinopsis", 2000);
        String kategoriId = ValidationUtil.required(data.kategoriId(), "kategoriId", "Kategori", 10);
        BookFormat format = data.format() == null
                ? (existing == null ? BookFormat.PDF : existing.getFormat())
                : data.format();
        if (format == BookFormat.UNKNOWN) {
            throw new ValidationException("format", "Format buku tidak valid.");
        }
        Set<String> authorSet = new LinkedHashSet<>();
        if (data.authorIds() != null) {
            for (String authorId : data.authorIds()) {
                if (authorId != null && !authorId.isBlank()) {
                    authorSet.add(authorId.trim());
                }
            }
        }
        if (authorSet.isEmpty()) {
            throw new ValidationException("authorIds", "Pilih minimal satu penulis.");
        }
        return new ValidatedBookData(judul, tahun, pages, isbn, penerbit, sinopsis,
                format, kategoriId, List.copyOf(authorSet));
    }

    private void validatePageCount(Integer submittedPages, int actualPages) {
        if (submittedPages != null && submittedPages != actualPages) {
            throw new ValidationException("jumlahHalaman",
                    "Jumlah halaman tidak sesuai dengan PDF (" + actualPages + " halaman).");
        }
    }

    private void ensureCategory(String kategoriId) {
        if (kategoriRepository.findById(kategoriId) == null) {
            throw new ValidationException("kategoriId", "Kategori tidak ditemukan.");
        }
    }

    private void ensureAuthors(List<String> authorIds) {
        List<String> missing = new ArrayList<>();
        for (String authorId : authorIds) {
            if (authorRepository.findById(authorId) == null) {
                missing.add(authorId);
            }
        }
        if (!missing.isEmpty()) {
            throw new ValidationException("authorIds", "Salah satu penulis tidak ditemukan.");
        }
    }

    public record BookFormData(
            String judul,
            String tahunTerbit,
            String jumlahHalaman,
            String isbn,
            String penerbit,
            String sinopsis,
            BookFormat format,
            String kategoriId,
            List<String> authorIds,
            Path coverSource,
            Path ebookSource
    ) {
    }

    private record ValidatedBookData(
            String judul,
            Integer tahunTerbit,
            Integer jumlahHalaman,
            String isbn,
            String penerbit,
            String sinopsis,
            BookFormat format,
            String kategoriId,
            List<String> authorIds
    ) {
    }
}
