package com.perpustakaan.service;

import com.perpustakaan.config.AppPaths;
import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.Publisher;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.KategoriRepository;
import com.perpustakaan.repository.PublisherRepository;
import com.perpustakaan.security.AccessControl;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.SlugUtil;
import com.perpustakaan.util.ValidationUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Operasi katalog dan pengelolaan e-book pada skema optl_pustaka.
 */
public class EbookService {

    private final EbookRepository ebookRepository;
    private final KategoriRepository kategoriRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final TransactionManager transactionManager;
    private final FileStorageService fileStorageService;
    private final PdfService pdfService;
    private final CoverService coverService;

    public EbookService(EbookRepository ebookRepository, KategoriRepository kategoriRepository,
                        AuthorRepository authorRepository, PublisherRepository publisherRepository,
                        TransactionManager transactionManager, FileStorageService fileStorageService,
                        PdfService pdfService, CoverService coverService) {
        this.ebookRepository = ebookRepository;
        this.kategoriRepository = kategoriRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.transactionManager = transactionManager;
        this.fileStorageService = fileStorageService;
        this.pdfService = pdfService;
        this.coverService = coverService;
    }

    public PageResult<BookListItem> listCatalog(User actor, String search, String genreId,
                                                String sort, int page, int size) {
        AccessControl.requireAuthenticated(actor);
        return ebookRepository.findBooks(search, genreId, sort, page, size, actor.getIdUser());
    }

    public PageResult<BookListItem> listAdmin(User actor, String search, String genreId,
                                              String sort, int page, int size) {
        AccessControl.requireAdmin(actor);
        return ebookRepository.findBooks(search, genreId, sort, page, size, null, true);
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

    public List<Publisher> publishers(User actor) {
        AccessControl.requireAuthenticated(actor);
        return publisherRepository.findAll();
    }

    public List<BookListItem> latest(User actor, int limit) {
        AccessControl.requireAuthenticated(actor);
        return ebookRepository.findRecent(actor.getIdUser(), limit);
    }

    public Ebook create(User actor, BookFormData data) {
        AccessControl.requireAdmin(actor);
        ValidatedBook validated = validate(data);
        if (data.ebookSource() == null) {
            throw new ValidationException("file", "Berkas PDF wajib dipilih.");
        }
        if (ebookRepository.isbnExists(validated.isbn(), null)) {
            throw new ValidationException("isbn", "ISBN sudah digunakan oleh buku lain.");
        }

        FileStorageService.FileInfo fileInfo = fileStorageService.inspect(data.ebookSource());
        int actualPages = pdfService.getPageCount(data.ebookSource());

        Ebook created = transactionManager.execute(connection -> {
            Author author = ensureAuthor(connection, validated.penulisNama());
            Kategori genre = ensureGenre(connection, validated.genreNama());
            Publisher publisher = ensurePublisher(connection, validated.penerbitNama());

            Ebook ebook = new Ebook();
            ebook.setJudul(validated.judul());
            ebook.setSlug(uniqueBookSlug(connection, validated.judul(), null));
            ebook.setTahunTerbit(validated.tahunTerbit());
            ebook.setIsbn(validated.isbn());
            ebook.setSinopsis(validated.sinopsis());
            ebook.setIdAuthor(author.getIdAuthor());
            ebook.setIdGenre(genre.getIdKategori());
            ebook.setIdPublisher(publisher.getIdPublisher());
            ebook.setKategoriId(genre.getIdKategori());
            ebook.setPenerbit(publisher.getNama());
            ebook.setFileSize(fileInfo.size());
            ebook.setChecksumSha256(fileInfo.sha256());
            ebook.setJumlahHalaman(actualPages);
            // sementara agar storage_path unik sebelum ID auto-increment diketahui
            ebook.setFilePath(AppPaths.ebookStoredPath("tmp-" + UUID.randomUUID().toString().replace("-", "")));
            ebook.setCoverPath("");

            ebookRepository.insert(connection, ebook);
            ebook.setFilePath(fileStorageService.saveEbookPdf(data.ebookSource(), ebook.getIdBook()));
            if (data.coverSource() != null) {
                ebook.setCoverPath(fileStorageService.saveCover(data.coverSource(), ebook.getIdBook()));
            } else {
                ebook.setCoverPath(coverService.generate(ebook.getIdBook(), validated.judul(),
                        validated.penulisNama(), validated.genreNama()));
            }
            ebookRepository.update(connection, ebook);
            return ebook;
        });
        AppLogger.info("Admin " + actor.getEmail() + " menambahkan buku " + created.getJudul());
        return created;
    }

    public Ebook update(User actor, String idBook, BookFormData data) {
        AccessControl.requireAdmin(actor);
        Ebook existing = ebookRepository.findById(idBook);
        if (existing == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        ValidatedBook validated = validate(data);
        if (ebookRepository.isbnExists(validated.isbn(), idBook)) {
            throw new ValidationException("isbn", "ISBN sudah digunakan oleh buku lain.");
        }

        Ebook updated = transactionManager.execute(connection -> {
            Author author = ensureAuthor(connection, validated.penulisNama());
            Kategori genre = ensureGenre(connection, validated.genreNama());
            Publisher publisher = ensurePublisher(connection, validated.penerbitNama());

            existing.setJudul(validated.judul());
            existing.setSlug(uniqueBookSlug(connection, validated.judul(), idBook));
            existing.setTahunTerbit(validated.tahunTerbit());
            existing.setIsbn(validated.isbn());
            existing.setSinopsis(validated.sinopsis());
            existing.setIdAuthor(author.getIdAuthor());
            existing.setIdGenre(genre.getIdKategori());
            existing.setIdPublisher(publisher.getIdPublisher());
            existing.setKategoriId(genre.getIdKategori());
            existing.setPenerbit(publisher.getNama());

            if (data.ebookSource() != null) {
                FileStorageService.FileInfo fileInfo = fileStorageService.inspect(data.ebookSource());
                existing.setFileSize(fileInfo.size());
                existing.setChecksumSha256(fileInfo.sha256());
                existing.setJumlahHalaman(pdfService.getPageCount(data.ebookSource()));
                existing.setFilePath(fileStorageService.saveEbookPdf(data.ebookSource(), idBook));
            } else if (validated.jumlahHalaman() != null && validated.jumlahHalaman() > 0) {
                existing.setJumlahHalaman(validated.jumlahHalaman());
            }
            if (data.coverSource() != null) {
                existing.setCoverPath(fileStorageService.saveCover(data.coverSource(), idBook));
            }
            ebookRepository.update(connection, existing);
            return existing;
        });
        AppLogger.info("Admin " + actor.getEmail() + " memperbarui buku " + updated.getJudul());
        return updated;
    }

    /** Hapus logis: buku tidak dihapus dari database, hanya dinonaktifkan. */
    public void delete(User actor, String idBook) {
        AccessControl.requireAdmin(actor);
        Ebook existing = ebookRepository.findById(idBook);
        if (existing == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        transactionManager.execute(connection -> {
            ebookRepository.softDelete(connection, idBook);
            return null;
        });
        AppLogger.info("Admin " + actor.getEmail() + " menonaktifkan buku " + existing.getJudul());
    }

    public void setActive(User actor, String bookId, boolean active) {
        AccessControl.requireAdmin(actor);
        if (ebookRepository.findById(bookId) == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        transactionManager.execute(connection -> {
            ebookRepository.setActive(connection, bookId, active);
            return null;
        });
        AppLogger.info("Admin " + actor.getEmail() + (active ? " mengaktifkan buku " : " menonaktifkan buku ") + bookId);
    }

    public PdfService.PdfSession openPdf(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        Ebook ebook = ebookRepository.findById(bookId);
        if (ebook == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        if (!ebook.isActive()) {
            throw new AppException("Buku tidak tersedia.");
        }
        if (!ebook.isReaderSupported()) {
            throw new AppException("Format buku belum didukung oleh pembaca PDF.");
        }
        Path file = AppPaths.resolveStoredPath(ebook.getFilePath());
        if (file == null || !Files.isRegularFile(file)) {
            throw new AppException("Berkas PDF tidak ditemukan di storage.");
        }
        return pdfService.open(file);
    }

    public boolean isFileAvailable(BookDetail detail) {
        return detail != null && detail.ebook() != null
                && AppPaths.exists(detail.ebook().getFilePath());
    }

    public int getPdfPageCount(Path file) {
        return pdfService.getPageCount(file);
    }

    /** Memakai penulis yang sudah ada (abaikan huruf besar/kecil) atau membuatnya. */
    public Author ensureAuthor(String name) {
        String trimmed = ValidationUtil.required(name, "penulisNama", "Penulis", 100);
        return transactionManager.execute(connection -> ensureAuthor(connection, trimmed));
    }

    public Kategori ensureGenre(String name) {
        String trimmed = ValidationUtil.required(name, "genreNama", "Genre", 100);
        return transactionManager.execute(connection -> ensureGenre(connection, trimmed));
    }

    public Publisher ensurePublisher(String name) {
        String trimmed = ValidationUtil.required(name, "penerbitNama", "Penerbit", 100);
        return transactionManager.execute(connection -> ensurePublisher(connection, trimmed));
    }

    private Author ensureAuthor(Connection connection, String name) {
        Author existing = authorRepository.findByName(connection, name);
        if (existing != null) {
            return existing;
        }
        Author author = new Author();
        author.setNamaLengkap(name);
        author.setSlug(SlugUtil.unique(SlugUtil.slugify(name),
                candidate -> authorRepository.slugExists(connection, candidate, null)));
        author.setNegaraAsal("");
        authorRepository.insert(connection, author);
        return author;
    }

    private Kategori ensureGenre(Connection connection, String name) {
        Kategori existing = kategoriRepository.findByName(connection, name);
        if (existing != null) {
            return existing;
        }
        Kategori genre = new Kategori();
        genre.setNamaKategori(name);
        genre.setDeskripsi(null);
        kategoriRepository.insert(connection, genre);
        return genre;
    }

    private Publisher ensurePublisher(Connection connection, String name) {
        Publisher existing = publisherRepository.findByName(connection, name);
        if (existing != null) {
            return existing;
        }
        Publisher publisher = new Publisher();
        publisher.setNama(name);
        publisher.setSlug(SlugUtil.unique(SlugUtil.slugify(name),
                candidate -> publisherRepository.slugExists(connection, candidate, null)));
        publisher.setKota("");
        publisherRepository.insert(connection, publisher);
        return publisher;
    }

    private String uniqueBookSlug(Connection connection, String title, String excludeId) {
        return SlugUtil.unique(SlugUtil.slugify(title),
                candidate -> ebookRepository.slugExists(connection, candidate, excludeId));
    }

    private ValidatedBook validate(BookFormData data) {
        String judul = ValidationUtil.required(data.judul(), "judul", "Judul buku", 200);
        Integer tahun = ValidationUtil.parseInteger(data.tahunTerbit(), "tahunTerbit", "Tahun terbit",
                1000, Year.now().getValue() + 1, false);
        Integer pages = ValidationUtil.parseInteger(data.jumlahHalaman(), "jumlahHalaman", "Jumlah halaman",
                1, 100_000, false);
        String isbn = ValidationUtil.isbn(data.isbn(), "isbn");
        String penulis = ValidationUtil.required(data.penulisNama(), "penulisNama", "Penulis", 100);
        String genre = ValidationUtil.required(data.genreNama(), "genreNama", "Genre", 100);
        String penerbit = ValidationUtil.required(data.penerbitNama(), "penerbitNama", "Penerbit", 100);
        String sinopsis = ValidationUtil.optional(data.sinopsis(), "sinopsis", "Sinopsis", 2000);
        return new ValidatedBook(judul, tahun, pages, isbn, penulis, genre, penerbit, sinopsis);
    }

    public record BookFormData(
            String judul,
            String tahunTerbit,
            String jumlahHalaman,
            String isbn,
            String penerbitNama,
            String genreNama,
            String penulisNama,
            String sinopsis,
            Path coverSource,
            Path ebookSource
    ) {
        public BookFormData(String judul, String tahunTerbit, String jumlahHalaman, String isbn,
                            String penerbitNama, String genreNama, String penulisNama, String sinopsis) {
            this(judul, tahunTerbit, jumlahHalaman, isbn, penerbitNama, genreNama, penulisNama,
                    sinopsis, null, null);
        }
    }

    private record ValidatedBook(String judul, Integer tahunTerbit, Integer jumlahHalaman, String isbn,
                                 String penulisNama, String genreNama, String penerbitNama, String sinopsis) {
    }
}
