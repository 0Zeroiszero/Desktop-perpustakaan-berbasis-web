package com.perpustakaan.repository;

import com.perpustakaan.model.DashboardStats;
import com.perpustakaan.model.ReadingStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Query agregat untuk dashboard admin pada skema optl_pustaka.
 */
public class DashboardRepository extends RepositorySupport {

    public DashboardStats loadAdminStats(int recentLimit, int categoryLimit) {
        return read(connection -> {
            long totalBooks = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM books WHERE is_active = 1", List.of());
            long totalMembers = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM users WHERE role = 'member'", List.of());
            long totalCategories = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM genres", List.of());
            long totalAuthors = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM authors", List.of());
            long totalProgress = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM reading_progress", List.of());
            long currentlyReading = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM collections WHERE status = 'reading'", List.of());
            long completed = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM collections WHERE status = 'finished'", List.of());
            BigDecimal averageProgress = Jdbc.queryBigDecimal(connection,
                    "SELECT COALESCE(AVG(rp.last_page * 100.0 / NULLIF(b.page_count, 0)), 0) "
                            + "FROM reading_progress rp JOIN books b ON b.book_id = rp.book_id",
                    List.of());

            List<DashboardStats.RecentActivity> recentActivities = Jdbc.queryList(connection,
                    "SELECT u.display_name AS NAMA, b.title AS JUDUL, c.status AS STATUS, "
                            + "DATE(rp.updated_at) AS LAST_READ, "
                            + "ROUND(rp.last_page * 100.0 / NULLIF(b.page_count, 0), 2) AS PROGRESS_PERCENT "
                            + "FROM collections c "
                            + "JOIN users u ON u.user_id = c.user_id "
                            + "JOIN books b ON b.book_id = c.book_id "
                            + "LEFT JOIN reading_progress rp ON rp.user_id = c.user_id AND rp.book_id = c.book_id "
                            + "ORDER BY rp.updated_at DESC, c.collection_id DESC LIMIT ?",
                    Jdbc.params(recentLimit),
                    resultSet -> new DashboardStats.RecentActivity(
                            resultSet.getString("NAMA"),
                            resultSet.getString("JUDUL"),
                            ReadingStatus.fromDatabase(resultSet.getString("STATUS")),
                            Jdbc.getLocalDate(resultSet, "LAST_READ"),
                            Jdbc.getBigDecimal(resultSet, "PROGRESS_PERCENT")));

            List<DashboardStats.CategoryStat> categoryStats = Jdbc.queryList(connection,
                    "SELECT g.name AS NAMA_KATEGORI, COUNT(b.book_id) AS JUMLAH "
                            + "FROM genres g LEFT JOIN books b ON b.genre_id = g.genre_id AND b.is_active = 1 "
                            + "GROUP BY g.genre_id, g.name "
                            + "ORDER BY JUMLAH DESC, g.name ASC LIMIT ?",
                    Jdbc.params(categoryLimit),
                    resultSet -> new DashboardStats.CategoryStat(
                            resultSet.getString("NAMA_KATEGORI"), resultSet.getLong("JUMLAH")));

            return new DashboardStats(totalBooks, totalMembers, totalCategories, totalAuthors,
                    totalProgress, currentlyReading, completed, averageProgress,
                    recentActivities, categoryStats);
        });
    }
}
