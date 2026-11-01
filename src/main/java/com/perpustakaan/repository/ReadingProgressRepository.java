package com.perpustakaan.repository;

import com.perpustakaan.model.BookFormat;
import com.perpustakaan.model.BookListItem;
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
 * Repository tabel reading_progress.
 */
public class ReadingProgressRepository extends RepositorySupport {

    private static final String PROGRESS_COLUMNS =
            "ID_PROGRESS, PROGRESS_PERCENT, LAST_READ, LAST_POSITION, START_DATE, END_DATE, "
                    + "STATUS_BACA, USER_ID_USER, BOOK_ID_BOOK";

    private static final String AUTHOR_AGGREGATE =
            "(SELECT GROUP_CONCAT(CONCAT_WS(' ', a.NAMA_DEPAN, a.NAMA_BELAKANG) "
                    + "ORDER BY a.NAMA_DEPAN SEPARATOR ', ') "
                    + "FROM ebook_author ea JOIN author a ON a.ID_AUTHOR = ea.AUTHOR_ID_AUTHOR "
                    + "WHERE ea.BOOK_ID_BOOK = e.ID_BOOK) AS PENULIS";

    private static final String SHELF_COLUMNS =
            "e.ID_BOOK, e.JUDUL, e.TAHUN_TERBIT, e.JUMLAH_HALAMAN, e.ISBN, e.PENERBIT, e.SINOPSIS, "
                    + "e.COVER, e.FILE_BOOK, e.FORMAT_FILE, e.KATEGORI_ID_KATEGORI, "
                    + "k.NAMA_KATEGORI, " + AUTHOR_AGGREGATE + ", "
                    + "r.PROGRESS_PERCENT, r.LAST_POSITION, r.STATUS_BACA, r.LAST_READ";

    public ReadingProgress findByUserAndBook(String userId, String bookId) {
        return read(connection -> findByUserAndBook(connection, userId, bookId));
    }

    public ReadingProgress findByUserAndBook(Connection connection, String userId, String bookId) {
        return Jdbc.queryOne(connection,
                "SELECT " + PROGRESS_COLUMNS + " FROM reading_progress "
                        + "WHERE USER_ID_USER = ? AND BOOK_ID_BOOK = ?",
                Jdbc.params(userId, bookId), this::mapProgress);
    }

    public PageResult<BookListItem> findShelf(String userId, String search, String sort, int page, int pageSize) {
        return read(connection -> {
            List<Object> parameters = new ArrayList<>();
            parameters.add(userId);
            String where = buildShelfWhere(search, parameters);
            long total = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM reading_progress r "
                            + "JOIN ebook e ON e.ID_BOOK = r.BOOK_ID_BOOK "
                            + "JOIN kategori k ON k.ID_KATEGORI = e.KATEGORI_ID_KATEGORI "
                            + where,
                    parameters);

            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((page - 1) * pageSize);
            List<BookListItem> items = Jdbc.queryList(connection,
                    "SELECT " + SHELF_COLUMNS + " FROM reading_progress r "
                            + "JOIN ebook e ON e.ID_BOOK = r.BOOK_ID_BOOK "
                            + "JOIN kategori k ON k.ID_KATEGORI = e.KATEGORI_ID_KATEGORI "
                            + where
                            + " ORDER BY " + shelfOrderBy(sort) + " LIMIT ? OFFSET ?",
                    pageParameters, this::mapListItem);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    private String buildShelfWhere(String search, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE r.USER_ID_USER = ?");
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(e.JUDUL) LIKE ?")
                    .append(" OR LOWER(COALESCE(e.ISBN, '')) LIKE ?")
                    .append(" OR LOWER(k.NAMA_KATEGORI) LIKE ?")
                    .append(" OR EXISTS (SELECT 1 FROM ebook_author ea2")
                    .append(" JOIN author a2 ON a2.ID_AUTHOR = ea2.AUTHOR_ID_AUTHOR")
                    .append(" WHERE ea2.BOOK_ID_BOOK = e.ID_BOOK")
                    .append(" AND LOWER(CONCAT_WS(' ', a2.NAMA_DEPAN, a2.NAMA_BELAKANG)) LIKE ?))");
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        return where.toString();
    }

    private String shelfOrderBy(String sort) {
        if (sort == null) {
            return "r.LAST_READ DESC, e.JUDUL ASC";
        }
        return switch (sort) {
            case "judul_az" -> "e.JUDUL ASC";
            case "progres_desc" -> "r.PROGRESS_PERCENT DESC, e.JUDUL ASC";
            default -> "r.LAST_READ DESC, e.JUDUL ASC";
        };
    }

    public BookListItem findLastReadBook(String userId) {
        return read(connection -> Jdbc.queryOne(connection,
                "SELECT " + SHELF_COLUMNS + " FROM reading_progress r "
                        + "JOIN ebook e ON e.ID_BOOK = r.BOOK_ID_BOOK "
                        + "JOIN kategori k ON k.ID_KATEGORI = e.KATEGORI_ID_KATEGORI "
                        + "WHERE r.USER_ID_USER = ? AND r.LAST_READ IS NOT NULL "
                        + "ORDER BY r.LAST_READ DESC LIMIT 1",
                Jdbc.params(userId), this::mapListItem));
    }

    public void insert(Connection connection, ReadingProgress progress) {
        Jdbc.update(connection,
                "INSERT INTO reading_progress (ID_PROGRESS, PROGRESS_PERCENT, LAST_READ, LAST_POSITION, "
                        + "START_DATE, END_DATE, STATUS_BACA, USER_ID_USER, BOOK_ID_BOOK) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Jdbc.params(progress.getIdProgress(), progress.getProgressPercent(), progress.getLastRead(),
                        progress.getLastPosition(), progress.getStartDate(), progress.getEndDate(),
                        progress.getStatus().databaseValue(), progress.getUserId(), progress.getBookId()));
    }

    public void update(Connection connection, ReadingProgress progress) {
        Jdbc.update(connection,
                "UPDATE reading_progress SET PROGRESS_PERCENT = ?, LAST_READ = ?, LAST_POSITION = ?, "
                        + "START_DATE = ?, END_DATE = ?, STATUS_BACA = ? WHERE ID_PROGRESS = ?",
                Jdbc.params(progress.getProgressPercent(), progress.getLastRead(), progress.getLastPosition(),
                        progress.getStartDate(), progress.getEndDate(), progress.getStatus().databaseValue(),
                        progress.getIdProgress()));
    }

    public void deleteByUserAndBook(Connection connection, String userId, String bookId) {
        Jdbc.update(connection,
                "DELETE FROM reading_progress WHERE USER_ID_USER = ? AND BOOK_ID_BOOK = ?",
                Jdbc.params(userId, bookId));
    }

    public void deleteByUser(Connection connection, String userId) {
        Jdbc.update(connection, "DELETE FROM reading_progress WHERE USER_ID_USER = ?", Jdbc.params(userId));
    }

    public void deleteByBook(Connection connection, String bookId) {
        Jdbc.update(connection, "DELETE FROM reading_progress WHERE BOOK_ID_BOOK = ?", Jdbc.params(bookId));
    }

    public long countByUser(String userId) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM reading_progress WHERE USER_ID_USER = ?", Jdbc.params(userId)));
    }

    public long countByUserAndStatus(String userId, ReadingStatus status) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM reading_progress WHERE USER_ID_USER = ? AND STATUS_BACA = ?",
                Jdbc.params(userId, status.databaseValue())));
    }

    public long countActiveByUser(String userId) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM reading_progress WHERE USER_ID_USER = ? "
                        + "AND STATUS_BACA IN ('Sedang dibaca', 'Tidak selesai')",
                Jdbc.params(userId)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM reading_progress", List.of()));
    }

    public long countByStatus(ReadingStatus status) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM reading_progress WHERE STATUS_BACA = ?",
                Jdbc.params(status.databaseValue())));
    }

    public BigDecimal averageProgress() {
        return read(connection -> Jdbc.queryBigDecimal(connection,
                "SELECT COALESCE(AVG(PROGRESS_PERCENT), 0) FROM reading_progress", List.of()));
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
}
