package com.perpustakaan.repository;

import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookFormat;
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
 * Repository tabel ebook beserta relasi kategori, penulis, dan progres pengguna.
 */
public class EbookRepository extends RepositorySupport {

    private static final String BOOK_COLUMNS =
            "e.ID_BOOK, e.JUDUL, e.TAHUN_TERBIT, e.JUMLAH_HALAMAN, e.ISBN, e.PENERBIT, e.SINOPSIS, "
                    + "e.COVER, e.FILE_BOOK, e.FORMAT_FILE, e.KATEGORI_ID_KATEGORI";

    private static final String AUTHOR_AGGREGATE =
            "(SELECT GROUP_CONCAT(CONCAT_WS(' ', a.NAMA_DEPAN, a.NAMA_BELAKANG) "
                    + "ORDER BY a.NAMA_DEPAN SEPARATOR ', ') "
                    + "FROM ebook_author ea JOIN author a ON a.ID_AUTHOR = ea.AUTHOR_ID_AUTHOR "
                    + "WHERE ea.BOOK_ID_BOOK = e.ID_BOOK) AS PENULIS";

    public PageResult<BookListItem> findBooks(String search, String kategoriId, BookFormat format,
                                              String sort, int page, int pageSize, String userId) {
        return read(connection -> {
            List<Object> whereParameters = new ArrayList<>();
            String where = buildWhere(search, kategoriId, format, whereParameters);
            long total = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM ebook e JOIN kategori k ON k.ID_KATEGORI = e.KATEGORI_ID_KATEGORI"
                            + where,
                    whereParameters);

            List<Object> dataParameters = new ArrayList<>();
            dataParameters.add(userId);
            dataParameters.addAll(whereParameters);
            dataParameters.add(pageSize);
            dataParameters.add((page - 1) * pageSize);

            String sql = "SELECT " + BOOK_COLUMNS + ", k.NAMA_KATEGORI, " + AUTHOR_AGGREGATE + ", "
                    + "r.PROGRESS_PERCENT, r.LAST_POSITION, r.STATUS_BACA, r.LAST_READ "
                    + "FROM ebook e "
                    + "JOIN kategori k ON k.ID_KATEGORI = e.KATEGORI_ID_KATEGORI "
                    + "LEFT JOIN reading_progress r ON r.BOOK_ID_BOOK = e.ID_BOOK AND r.USER_ID_USER = ?"
                    + where
                    + " ORDER BY " + orderBy(sort) + " LIMIT ? OFFSET ?";

            List<BookListItem> items = Jdbc.queryList(connection, sql, dataParameters, this::mapListItem);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    private String buildWhere(String search, String kategoriId, BookFormat format, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(e.JUDUL) LIKE ?")
                    .append(" OR LOWER(COALESCE(e.ISBN, '')) LIKE ?")
                    .append(" OR LOWER(COALESCE(e.PENERBIT, '')) LIKE ?")
                    .append(" OR LOWER(k.NAMA_KATEGORI) LIKE ?")
                    .append(" OR EXISTS (SELECT 1 FROM ebook_author ea2")
                    .append(" JOIN author a2 ON a2.ID_AUTHOR = ea2.AUTHOR_ID_AUTHOR")
                    .append(" WHERE ea2.BOOK_ID_BOOK = e.ID_BOOK")
                    .append(" AND LOWER(CONCAT_WS(' ', a2.NAMA_DEPAN, a2.NAMA_BELAKANG)) LIKE ?))");
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (kategoriId != null && !kategoriId.isBlank()) {
            where.append(" AND e.KATEGORI_ID_KATEGORI = ?");
            parameters.add(kategoriId);
        }
        if (format != null) {
            where.append(" AND UPPER(COALESCE(e.FORMAT_FILE, '')) = ?");
            parameters.add(format.name());
        }
        return where.toString();
    }

    private String orderBy(String sort) {
        if (sort == null) {
            return "CAST(SUBSTRING(e.ID_BOOK, 3) AS UNSIGNED) DESC";
        }
        return switch (sort) {
            case "judul_az" -> "e.JUDUL ASC";
            case "tahun_desc" -> "e.TAHUN_TERBIT DESC, e.JUDUL ASC";
            case "penulis_az" -> "PENULIS ASC, e.JUDUL ASC";
            default -> "CAST(SUBSTRING(e.ID_BOOK, 3) AS UNSIGNED) DESC";
        };
    }

    public Ebook findById(String idBook) {
        return read(connection -> findById(connection, idBook));
    }

    public Ebook findById(Connection connection, String idBook) {
        return Jdbc.queryOne(connection,
                "SELECT " + BOOK_COLUMNS + " FROM ebook e WHERE e.ID_BOOK = ?",
                Jdbc.params(idBook), this::mapEbook);
    }

    public BookDetail findDetail(String idBook, String userId) {
        return read(connection -> {
            Ebook ebook = findById(connection, idBook);
            if (ebook == null) {
                return null;
            }
            Kategori kategori = Jdbc.queryOne(connection,
                    "SELECT ID_KATEGORI, NAMA_KATEGORI, DESKRIPSI FROM kategori WHERE ID_KATEGORI = ?",
                    Jdbc.params(ebook.getKategoriId()),
                    resultSet -> new Kategori(
                            resultSet.getString("ID_KATEGORI"),
                            resultSet.getString("NAMA_KATEGORI"),
                            resultSet.getString("DESKRIPSI"),
                            0));

            List<Author> authors = Jdbc.queryList(connection,
                    "SELECT a.ID_AUTHOR, a.NAMA_DEPAN, a.NAMA_BELAKANG, a.EMAIL, a.NEGARA_ASAL, a.BIO "
                            + "FROM ebook_author ea JOIN author a ON a.ID_AUTHOR = ea.AUTHOR_ID_AUTHOR "
                            + "WHERE ea.BOOK_ID_BOOK = ? ORDER BY a.NAMA_DEPAN ASC, a.NAMA_BELAKANG ASC",
                    Jdbc.params(idBook),
                    resultSet -> new Author(
                            resultSet.getString("ID_AUTHOR"),
                            resultSet.getString("NAMA_DEPAN"),
                            resultSet.getString("NAMA_BELAKANG"),
                            resultSet.getString("EMAIL"),
                            resultSet.getString("NEGARA_ASAL"),
                            resultSet.getString("BIO"),
                            0));

            ReadingProgress progress = null;
            if (userId != null) {
                progress = Jdbc.queryOne(connection,
                        "SELECT ID_PROGRESS, PROGRESS_PERCENT, LAST_READ, LAST_POSITION, START_DATE, END_DATE, "
                                + "STATUS_BACA, USER_ID_USER, BOOK_ID_BOOK FROM reading_progress "
                                + "WHERE USER_ID_USER = ? AND BOOK_ID_BOOK = ?",
                        Jdbc.params(userId, idBook), this::mapProgress);
            }
            return new BookDetail(ebook, kategori, authors, progress);
        });
    }

    public boolean isbnExists(String isbn, String excludeId) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM ebook WHERE LOWER(ISBN) = LOWER(?)",
                        Jdbc.params(isbn)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM ebook WHERE LOWER(ISBN) = LOWER(?) AND ID_BOOK <> ?",
                    Jdbc.params(isbn, excludeId)) > 0;
        });
    }

    public boolean isPathReferenced(String storedPath, String excludeBookId) {
        if (storedPath == null || storedPath.isBlank()) {
            return false;
        }
        return read(connection -> {
            if (excludeBookId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM ebook WHERE COVER = ? OR FILE_BOOK = ?",
                        Jdbc.params(storedPath, storedPath)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM ebook WHERE (COVER = ? OR FILE_BOOK = ?) AND ID_BOOK <> ?",
                    Jdbc.params(storedPath, storedPath, excludeBookId)) > 0;
        });
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM ebook", List.of()));
    }

    public void insert(Connection connection, Ebook ebook) {
        Jdbc.update(connection,
                "INSERT INTO ebook (ID_BOOK, JUDUL, TAHUN_TERBIT, JUMLAH_HALAMAN, ISBN, PENERBIT, SINOPSIS, "
                        + "COVER, FILE_BOOK, FORMAT_FILE, KATEGORI_ID_KATEGORI) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Jdbc.params(ebook.getIdBook(), ebook.getJudul(), ebook.getTahunTerbit(), ebook.getJumlahHalaman(),
                        ebook.getIsbn(), ebook.getPenerbit(), ebook.getSinopsis(), ebook.getCoverPath(),
                        ebook.getFilePath(), ebook.getFormat().name(), ebook.getKategoriId()));
    }

    public void update(Connection connection, Ebook ebook) {
        Jdbc.update(connection,
                "UPDATE ebook SET JUDUL = ?, TAHUN_TERBIT = ?, JUMLAH_HALAMAN = ?, ISBN = ?, PENERBIT = ?, "
                        + "SINOPSIS = ?, COVER = ?, FILE_BOOK = ?, FORMAT_FILE = ?, KATEGORI_ID_KATEGORI = ? "
                        + "WHERE ID_BOOK = ?",
                Jdbc.params(ebook.getJudul(), ebook.getTahunTerbit(), ebook.getJumlahHalaman(), ebook.getIsbn(),
                        ebook.getPenerbit(), ebook.getSinopsis(), ebook.getCoverPath(), ebook.getFilePath(),
                        ebook.getFormat().name(), ebook.getKategoriId(), ebook.getIdBook()));
    }

    public void delete(Connection connection, String idBook) {
        Jdbc.update(connection, "DELETE FROM ebook WHERE ID_BOOK = ?", Jdbc.params(idBook));
    }

    private BookListItem mapListItem(ResultSet resultSet) throws SQLException {
        BigDecimal progress = Jdbc.getBigDecimal(resultSet, "PROGRESS_PERCENT");
        Integer lastPosition = Jdbc.getInteger(resultSet, "LAST_POSITION");
        ReadingStatus status = ReadingStatus.fromDatabaseNullable(resultSet.getString("STATUS_BACA"));
        LocalDate lastRead = Jdbc.getLocalDate(resultSet, "LAST_READ");
        return new BookListItem(
                resultSet.getString("ID_BOOK"),
                resultSet.getString("JUDUL"),
                resultSet.getString("PENULIS"),
                resultSet.getString("NAMA_KATEGORI"),
                resultSet.getString("COVER"),
                BookFormat.fromDatabase(resultSet.getString("FORMAT_FILE")),
                Jdbc.getInteger(resultSet, "TAHUN_TERBIT"),
                Jdbc.getInteger(resultSet, "JUMLAH_HALAMAN"),
                progress,
                lastPosition,
                status,
                lastRead);
    }

    private Ebook mapEbook(ResultSet resultSet) throws SQLException {
        Ebook ebook = new Ebook();
        ebook.setIdBook(resultSet.getString("ID_BOOK"));
        ebook.setJudul(resultSet.getString("JUDUL"));
        ebook.setTahunTerbit(Jdbc.getInteger(resultSet, "TAHUN_TERBIT"));
        ebook.setJumlahHalaman(Jdbc.getInteger(resultSet, "JUMLAH_HALAMAN"));
        ebook.setIsbn(resultSet.getString("ISBN"));
        ebook.setPenerbit(resultSet.getString("PENERBIT"));
        ebook.setSinopsis(resultSet.getString("SINOPSIS"));
        ebook.setCoverPath(resultSet.getString("COVER"));
        ebook.setFilePath(resultSet.getString("FILE_BOOK"));
        ebook.setFormat(BookFormat.fromDatabase(resultSet.getString("FORMAT_FILE")));
        ebook.setKategoriId(resultSet.getString("KATEGORI_ID_KATEGORI"));
        return ebook;
    }

    private ReadingProgress mapProgress(ResultSet resultSet) throws SQLException {
        ReadingProgress progress = new ReadingProgress();
        progress.setIdProgress(resultSet.getString("ID_PROGRESS"));
        progress.setProgressPercent(Jdbc.getBigDecimal(resultSet, "PROGRESS_PERCENT"));
        progress.setLastRead(Jdbc.getLocalDate(resultSet, "LAST_READ"));
        progress.setLastPosition(Jdbc.getInteger(resultSet, "LAST_POSITION"));
        progress.setStartDate(Jdbc.getLocalDate(resultSet, "START_DATE"));
        progress.setEndDate(Jdbc.getLocalDate(resultSet, "END_DATE"));
        progress.setStatus(ReadingStatus.fromDatabase(resultSet.getString("STATUS_BACA")));
        progress.setUserId(resultSet.getString("USER_ID_USER"));
        progress.setBookId(resultSet.getString("BOOK_ID_BOOK"));
        return progress;
    }
}
