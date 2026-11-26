package com.perpustakaan.repository;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.ReadingProgress;
import com.perpustakaan.model.ReadingStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repository tabel reading_progress (last_page) dan hubungannya dengan
 * collections untuk status baca.
 */
public class ReadingProgressRepository extends RepositorySupport {

    private static final String SHELF_SELECT =
            "SELECT v.book_id AS ID_BOOK, v.title AS JUDUL, v.author_name AS PENULIS, "
                    + "v.genre_name AS NAMA_KATEGORI, v.cover_path AS COVER, "
                    + "v.published_year AS TAHUN_TERBIT, v.page_count AS JUMLAH_HALAMAN, "
                    + "ROUND(rp.last_page * 100.0 / NULLIF(v.page_count, 0), 2) AS PROGRESS_PERCENT, "
                    + "rp.last_page AS LAST_POSITION, c.status AS STATUS, "
                    + "DATE(rp.updated_at) AS LAST_READ, v.is_active AS IS_ACTIVE "
                    + "FROM collections c "
                    + "JOIN v_books_catalog v ON v.book_id = c.book_id "
                    + "LEFT JOIN reading_progress rp ON rp.user_id = c.user_id AND rp.book_id = c.book_id ";

    public ReadingProgress findByUserAndBook(String userId, String bookId) {
        return read(connection -> findByUserAndBook(connection, userId, bookId));
    }

    public ReadingProgress findByUserAndBook(Connection connection, String userId, String bookId) {
        return Jdbc.queryOne(connection,
                "SELECT rp.progress_id, rp.user_id, rp.book_id, rp.last_page, rp.updated_at, "
                        + "b.page_count, c.status "
                        + "FROM reading_progress rp JOIN books b ON b.book_id = rp.book_id "
                        + "LEFT JOIN collections c ON c.user_id = rp.user_id AND c.book_id = rp.book_id "
                        + "WHERE rp.user_id = ? AND rp.book_id = ?",
                Jdbc.params(userId, bookId), ReadingProgressRepository::mapProgress);
    }

    public PageResult<BookListItem> findShelf(String userId, String search, String status,
                                              String sort, int page, int pageSize) {
        return read(connection -> {
            List<Object> parameters = new ArrayList<>();
            parameters.add(userId);
            String where = buildShelfWhere(search, status, parameters);
            long total = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM collections c "
                            + "JOIN v_books_catalog v ON v.book_id = c.book_id "
                            + "LEFT JOIN reading_progress rp ON rp.user_id = c.user_id AND rp.book_id = c.book_id "
                            + where,
                    parameters);

            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((long) (page - 1) * pageSize);
            List<BookListItem> items = Jdbc.queryList(connection,
                    SHELF_SELECT + where + " ORDER BY " + shelfOrderBy(sort) + " LIMIT ? OFFSET ?",
                    pageParameters, EbookRepository::mapListItem);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    private String buildShelfWhere(String search, String status, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE c.user_id = ?");
        if (status != null && !status.isBlank()) {
            ReadingStatus parsed = ReadingStatus.fromDatabaseNullable(status);
            String value = parsed == null ? status.trim().toLowerCase(Locale.ROOT) : parsed.dbValue();
            where.append(" AND c.status = ?");
            parameters.add(value);
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(v.title) LIKE ? OR LOWER(COALESCE(v.isbn, '')) LIKE ?")
                    .append(" OR LOWER(v.author_name) LIKE ? OR LOWER(v.genre_name) LIKE ?)");
            for (int i = 0; i < 4; i++) {
                parameters.add(pattern);
            }
        }
        return where.toString();
    }

    private String shelfOrderBy(String sort) {
        if (sort == null) {
            return "rp.updated_at DESC, v.title ASC";
        }
        return switch (sort) {
            case "judul_az" -> "v.title ASC";
            case "progres_desc" -> "PROGRESS_PERCENT DESC, v.title ASC";
            default -> "rp.updated_at DESC, v.title ASC";
        };
    }

    public BookListItem findLastReadBook(String userId) {
        return read(connection -> Jdbc.queryOne(connection,
                SHELF_SELECT + "WHERE c.user_id = ? AND rp.updated_at IS NOT NULL "
                        + "ORDER BY rp.updated_at DESC LIMIT 1",
                Jdbc.params(userId), EbookRepository::mapListItem));
    }

    public void insert(Connection connection, ReadingProgress progress) {
        Jdbc.update(connection,
                "INSERT INTO reading_progress (user_id, book_id, last_page) VALUES (?, ?, ?)",
                Jdbc.params(progress.getUserId(), progress.getBookId(), progress.getLastPosition()));
    }

    public void update(Connection connection, ReadingProgress progress) {
        Jdbc.update(connection,
                "UPDATE reading_progress SET last_page = ? WHERE progress_id = ?",
                Jdbc.params(progress.getLastPosition(), progress.getIdProgress()));
    }

    /** Menyimpan posisi baca, membuat baris baru bila belum ada. */
    public void upsert(Connection connection, String userId, String bookId, int lastPage) {
        Jdbc.update(connection,
                "INSERT INTO reading_progress (user_id, book_id, last_page) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE last_page = VALUES(last_page)",
                Jdbc.params(userId, bookId, lastPage));
    }

    public void deleteByUserAndBook(Connection connection, String userId, String bookId) {
        Jdbc.update(connection,
                "DELETE FROM reading_progress WHERE user_id = ? AND book_id = ?",
                Jdbc.params(userId, bookId));
    }

    public void deleteByUser(Connection connection, String userId) {
        Jdbc.update(connection, "DELETE FROM reading_progress WHERE user_id = ?", Jdbc.params(userId));
    }

    public void deleteByBook(Connection connection, String bookId) {
        Jdbc.update(connection, "DELETE FROM reading_progress WHERE book_id = ?", Jdbc.params(bookId));
    }

    public long countByUser(String userId) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM reading_progress WHERE user_id = ?", Jdbc.params(userId)));
    }

    public long countByUserAndStatus(String userId, ReadingStatus status) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections WHERE user_id = ? AND status = ?",
                Jdbc.params(userId, status.dbValue())));
    }

    public long countActiveByUser(String userId) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections WHERE user_id = ? AND status = 'reading'",
                Jdbc.params(userId)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM reading_progress", List.of()));
    }

    public long countByStatus(ReadingStatus status) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections WHERE status = ?", Jdbc.params(status.dbValue())));
    }

    public BigDecimal averageProgress() {
        return read(connection -> Jdbc.queryBigDecimal(connection,
                "SELECT COALESCE(AVG(rp.last_page * 100.0 / NULLIF(b.page_count, 0)), 0) "
                        + "FROM reading_progress rp JOIN books b ON b.book_id = rp.book_id",
                List.of()));
    }

    public static ReadingProgress mapProgress(ResultSet resultSet) throws SQLException {
        ReadingProgress progress = new ReadingProgress();
        progress.setIdProgress(resultSet.getString("progress_id"));
        progress.setUserId(resultSet.getString("user_id"));
        progress.setBookId(resultSet.getString("book_id"));
        Integer pageCount = Jdbc.getInteger(resultSet, "page_count");
        progress.setPageCount(pageCount == null ? 0 : pageCount);
        progress.setLastPosition(Jdbc.getInteger(resultSet, "last_page"));
        progress.setLastRead(Jdbc.getLocalDate(resultSet, "updated_at"));
        progress.setStatus(ReadingStatus.fromDatabase(resultSet.getString("status")));
        return progress;
    }
}
