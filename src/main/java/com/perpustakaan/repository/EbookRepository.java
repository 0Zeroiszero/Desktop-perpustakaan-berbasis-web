package com.perpustakaan.repository;

import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.ReadingProgress;
import com.perpustakaan.model.ReadingStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repository katalog buku berbasis view v_books_catalog dan tabel books.
 */
public class EbookRepository extends RepositorySupport {

    private static final String DETAIL_COLUMNS =
            "b.book_id, b.slug, b.title, b.description, b.published_year, b.isbn, b.storage_path, "
                    + "b.file_size, b.checksum_sha256, b.page_count, b.cover_path, b.is_active, "
                    + "b.author_id, b.publisher_id, b.genre_id, "
                    + "a.name AS author_name, a.slug AS author_slug, a.country AS author_country, a.bio AS author_bio, "
                    + "g.name AS genre_name, g.description AS genre_description, "
                    + "p.name AS publisher_name";

    private static final String DETAIL_JOINS =
            " FROM books b "
                    + "JOIN authors a ON a.author_id = b.author_id "
                    + "JOIN genres g ON g.genre_id = b.genre_id "
                    + "JOIN publishers p ON p.publisher_id = b.publisher_id ";

    public PageResult<BookListItem> findBooks(String search, String genreId, String sort,
                                              int page, int pageSize, String userId) {
        return findBooks(search, genreId, sort, page, pageSize, userId, false);
    }

    public PageResult<BookListItem> findBooks(String search, String genreId, String sort,
                                              int page, int pageSize, String userId, boolean includeInactive) {
        return read(connection -> {
            List<Object> whereParameters = new ArrayList<>();
            String where = buildWhere(search, genreId, whereParameters);
            String activeFilter = includeInactive ? " WHERE 1=1" : " WHERE v.is_active = 1";
            long total = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM v_books_catalog v" + activeFilter + where,
                    whereParameters);

            List<Object> parameters = new ArrayList<>();
            parameters.add(userId);
            parameters.add(userId);
            parameters.addAll(whereParameters);
            parameters.add(pageSize);
            parameters.add((long) (page - 1) * pageSize);

            String sql = "SELECT v.book_id AS ID_BOOK, v.title AS JUDUL, v.author_name AS PENULIS, "
                    + "v.genre_name AS NAMA_KATEGORI, v.cover_path AS COVER, "
                    + "v.published_year AS TAHUN_TERBIT, v.page_count AS JUMLAH_HALAMAN, "
                    + "ROUND(rp.last_page * 100.0 / NULLIF(v.page_count, 0), 2) AS PROGRESS_PERCENT, "
                    + "rp.last_page AS LAST_POSITION, c.status AS STATUS, "
                    + "DATE(rp.updated_at) AS LAST_READ, v.is_active AS IS_ACTIVE "
                    + "FROM v_books_catalog v "
                    + "LEFT JOIN reading_progress rp ON rp.book_id = v.book_id AND rp.user_id = ? "
                    + "LEFT JOIN collections c ON c.book_id = v.book_id AND c.user_id = ? "
                    + activeFilter + where
                    + " ORDER BY " + orderBy(sort) + " LIMIT ? OFFSET ?";

            List<BookListItem> items = Jdbc.queryList(connection, sql, parameters, EbookRepository::mapListItem);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    /** Buku yang terhubung ke satu penulis, penerbit, atau genre. */
    public List<BookListItem> findByMaster(String master, String id, String userId, boolean includeInactive) {
        String column = switch (master) {
            case "author" -> "author_id";
            case "publisher" -> "publisher_id";
            case "genre" -> "genre_id";
            default -> throw new IllegalArgumentException("Jenis profil tidak dikenal: " + master);
        };
        return read(connection -> {
            List<Object> parameters = new ArrayList<>();
            parameters.add(userId);
            parameters.add(userId);
            parameters.add(id);
            String active = includeInactive ? "" : " AND v.is_active = 1";
            String sql = "SELECT v.book_id AS ID_BOOK, v.title AS JUDUL, v.author_name AS PENULIS, "
                    + "v.genre_name AS NAMA_KATEGORI, v.cover_path AS COVER, "
                    + "v.published_year AS TAHUN_TERBIT, v.page_count AS JUMLAH_HALAMAN, "
                    + "ROUND(rp.last_page * 100.0 / NULLIF(v.page_count, 0), 2) AS PROGRESS_PERCENT, "
                    + "rp.last_page AS LAST_POSITION, c.status AS STATUS, "
                    + "DATE(rp.updated_at) AS LAST_READ, v.is_active AS IS_ACTIVE "
                    + "FROM v_books_catalog v "
                    + "JOIN books b ON b.book_id = v.book_id "
                    + "LEFT JOIN reading_progress rp ON rp.book_id = v.book_id AND rp.user_id = ? "
                    + "LEFT JOIN collections c ON c.book_id = v.book_id AND c.user_id = ? "
                    + "WHERE b." + column + " = ?" + active + " ORDER BY v.title ASC";
            return Jdbc.queryList(connection, sql, parameters, EbookRepository::mapListItem);
        });
    }

    /** Buku terbaru untuk dashboard, diurutkan dari created_at. */
    public List<BookListItem> findRecent(String userId, int limit) {
        return findBooks(null, null, "terbaru", 1, limit, userId).items();
    }

    private String buildWhere(String search, String genreId, List<Object> parameters) {
        StringBuilder where = new StringBuilder();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(v.title) LIKE ? OR LOWER(COALESCE(v.isbn, '')) LIKE ?")
                    .append(" OR LOWER(v.author_name) LIKE ? OR LOWER(v.genre_name) LIKE ?")
                    .append(" OR LOWER(v.publisher_name) LIKE ?)");
            for (int i = 0; i < 5; i++) {
                parameters.add(pattern);
            }
        }
        if (genreId != null && !genreId.isBlank()) {
            where.append(" AND v.genre_id = ?");
            parameters.add(genreId);
        }
        return where.toString();
    }

    private String orderBy(String sort) {
        if (sort == null) {
            return "v.created_at DESC, v.book_id DESC";
        }
        return switch (sort) {
            case "judul_az" -> "v.title ASC";
            case "tahun_desc" -> "v.published_year DESC, v.title ASC";
            case "penulis_az" -> "v.author_name ASC, v.title ASC";
            default -> "v.created_at DESC, v.book_id DESC";
        };
    }

    public Ebook findById(String idBook) {
        return read(connection -> {
            DetailRow row = findDetailRow(connection, idBook);
            return row == null ? null : row.ebook();
        });
    }

    public Ebook findById(Connection connection, String idBook) {
        DetailRow row = findDetailRow(connection, idBook);
        return row == null ? null : row.ebook();
    }

    private DetailRow findDetailRow(Connection connection, String idBook) {
        return Jdbc.queryOne(connection,
                "SELECT " + DETAIL_COLUMNS + DETAIL_JOINS + "WHERE b.book_id = ?",
                Jdbc.params(idBook), resultSet -> {
                    Ebook ebook = mapEbook(resultSet);
                    String publisherName = resultSet.getString("publisher_name");
                    ebook.setPenerbit(publisherName);
                    Kategori kategori = new Kategori(resultSet.getString("genre_id"),
                            resultSet.getString("genre_name"), resultSet.getString("genre_description"), 0);
                    Author author = new Author(resultSet.getString("author_id"),
                            resultSet.getString("author_name"), resultSet.getString("author_slug"),
                            resultSet.getString("author_country"), resultSet.getString("author_bio"), 0);
                    return new DetailRow(ebook, kategori, author, publisherName);
                });
    }

    public BookDetail findDetail(String idBook, String userId) {
        return read(connection -> {
            DetailRow row = findDetailRow(connection, idBook);
            if (row == null) {
                return null;
            }
            ReadingProgress progress = null;
            if (userId != null) {
                progress = Jdbc.queryOne(connection,
                        "SELECT rp.progress_id, rp.user_id, rp.book_id, rp.last_page, rp.updated_at, "
                                + "b.page_count, c.status "
                                + "FROM reading_progress rp JOIN books b ON b.book_id = rp.book_id "
                                + "LEFT JOIN collections c ON c.user_id = rp.user_id AND c.book_id = rp.book_id "
                                + "WHERE rp.user_id = ? AND rp.book_id = ?",
                        Jdbc.params(userId, idBook), EbookRepository::mapProgress);
            }
            return new BookDetail(row.ebook(), row.kategori(), row.author(), row.publisherName(), progress);
        });
    }

    private record DetailRow(Ebook ebook, Kategori kategori, Author author, String publisherName) {
    }

    public boolean isbnExists(String isbn, String excludeId) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM books WHERE LOWER(isbn) = LOWER(?)",
                        Jdbc.params(isbn)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM books WHERE LOWER(isbn) = LOWER(?) AND book_id <> ?",
                    Jdbc.params(isbn, excludeId)) > 0;
        });
    }

    public boolean slugExists(Connection connection, String slug, String excludeId) {
        if (excludeId == null) {
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM books WHERE slug = ?", Jdbc.params(slug)) > 0;
        }
        return Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM books WHERE slug = ? AND book_id <> ?",
                Jdbc.params(slug, excludeId)) > 0;
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM books WHERE is_active = 1", List.of()));
    }

    public String insert(Connection connection, Ebook ebook) {
        long id = Jdbc.insertAndGetId(connection,
                "INSERT INTO books (author_id, publisher_id, genre_id, title, slug, description, "
                        + "published_year, isbn, storage_path, file_size, checksum_sha256, page_count, cover_path) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Jdbc.params(ebook.getIdAuthor(), ebook.getIdPublisher(), ebook.getIdGenre(),
                        ebook.getJudul(), ebook.getSlug(), ebook.getSinopsis(), ebook.getTahunTerbit(),
                        ebook.getIsbn(), ebook.getFilePath(), ebook.getFileSize(),
                        ebook.getChecksumSha256(), ebook.getJumlahHalaman(), ebook.getCoverPath()));
        ebook.setIdBook(String.valueOf(id));
        return ebook.getIdBook();
    }

    public void update(Connection connection, Ebook ebook) {
        Jdbc.update(connection,
                "UPDATE books SET author_id = ?, publisher_id = ?, genre_id = ?, title = ?, slug = ?, "
                        + "description = ?, published_year = ?, isbn = ?, storage_path = ?, file_size = ?, "
                        + "checksum_sha256 = ?, page_count = ?, cover_path = ? WHERE book_id = ?",
                Jdbc.params(ebook.getIdAuthor(), ebook.getIdPublisher(), ebook.getIdGenre(),
                        ebook.getJudul(), ebook.getSlug(), ebook.getSinopsis(), ebook.getTahunTerbit(),
                        ebook.getIsbn(), ebook.getFilePath(), ebook.getFileSize(),
                        ebook.getChecksumSha256(), ebook.getJumlahHalaman(), ebook.getCoverPath(),
                        ebook.getIdBook()));
    }

    /** Hapus logis: buku tetap ada tetapi tidak tampil di katalog. */
    public void softDelete(Connection connection, String idBook) {
        Jdbc.update(connection, "UPDATE books SET is_active = 0 WHERE book_id = ?", Jdbc.params(idBook));
    }

    public void setActive(Connection connection, String idBook, boolean active) {
        Jdbc.update(connection, "UPDATE books SET is_active = ? WHERE book_id = ?",
                Jdbc.params(active ? 1 : 0, idBook));
    }

    public static BookListItem mapListItem(ResultSet resultSet) throws SQLException {
        BigDecimal progress = Jdbc.getBigDecimal(resultSet, "PROGRESS_PERCENT");
        Integer lastPosition = Jdbc.getInteger(resultSet, "LAST_POSITION");
        ReadingStatus status = ReadingStatus.fromDatabaseNullable(resultSet.getString("STATUS"));
        LocalDate lastRead = Jdbc.getLocalDate(resultSet, "LAST_READ");
        boolean active = true;
        try {
            active = resultSet.getInt("IS_ACTIVE") != 0;
        } catch (SQLException ignored) {
            // Query lama tanpa kolom IS_ACTIVE dianggap aktif.
        }
        return new BookListItem(
                resultSet.getString("ID_BOOK"),
                resultSet.getString("JUDUL"),
                resultSet.getString("PENULIS"),
                resultSet.getString("NAMA_KATEGORI"),
                resultSet.getString("COVER"),
                Jdbc.getInteger(resultSet, "TAHUN_TERBIT"),
                Jdbc.getInteger(resultSet, "JUMLAH_HALAMAN"),
                progress,
                lastPosition,
                status,
                lastRead,
                active);
    }

    private Ebook mapEbook(ResultSet resultSet) throws SQLException {
        Ebook ebook = new Ebook();
        ebook.setIdBook(resultSet.getString("book_id"));
        ebook.setSlug(resultSet.getString("slug"));
        ebook.setJudul(resultSet.getString("title"));
        ebook.setSinopsis(resultSet.getString("description"));
        ebook.setTahunTerbit(Jdbc.getInteger(resultSet, "published_year"));
        ebook.setIsbn(resultSet.getString("isbn"));
        ebook.setFilePath(resultSet.getString("storage_path"));
        ebook.setFileSize(resultSet.getLong("file_size"));
        ebook.setChecksumSha256(resultSet.getString("checksum_sha256"));
        ebook.setJumlahHalaman(Jdbc.getInteger(resultSet, "page_count"));
        ebook.setCoverPath(resultSet.getString("cover_path"));
        ebook.setIdAuthor(resultSet.getString("author_id"));
        ebook.setIdPublisher(resultSet.getString("publisher_id"));
        ebook.setIdGenre(resultSet.getString("genre_id"));
        ebook.setActive(resultSet.getInt("is_active") != 0);
        return ebook;
    }

    private static ReadingProgress mapProgress(ResultSet resultSet) throws SQLException {
        ReadingProgress progress = new ReadingProgress();
        progress.setIdProgress(resultSet.getString("progress_id"));
        progress.setUserId(resultSet.getString("user_id"));
        progress.setBookId(resultSet.getString("book_id"));
        Integer pageCount = Jdbc.getInteger(resultSet, "page_count");
        progress.setLastPosition(Jdbc.getInteger(resultSet, "last_page"));
        progress.setPageCount(pageCount == null ? 0 : pageCount);
        progress.setLastRead(Jdbc.getLocalDate(resultSet, "updated_at"));
        progress.setStatus(ReadingStatus.fromDatabase(resultSet.getString("status")));
        return progress;
    }
}
