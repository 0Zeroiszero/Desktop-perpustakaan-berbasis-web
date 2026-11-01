package com.perpustakaan.repository;

import com.perpustakaan.model.DashboardStats;
import com.perpustakaan.model.ReadingStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Query agregat untuk dashboard admin.
 */
public class DashboardRepository extends RepositorySupport {

    public DashboardStats loadAdminStats(int recentLimit, int categoryLimit) {
        return read(connection -> {
            long totalBooks = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM ebook", List.of());
            long totalMembers = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM users WHERE LOWER(USER_ROLE) <> 'admin'", List.of());
            long totalCategories = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM kategori", List.of());
            long totalAuthors = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM author", List.of());
            long totalProgress = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM reading_progress", List.of());
            long currentlyReading = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM reading_progress "
                            + "WHERE STATUS_BACA IN ('Sedang dibaca', 'Tidak selesai')",
                    List.of());
            long completed = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM reading_progress WHERE STATUS_BACA = 'Telah dibaca'",
                    List.of());
            BigDecimal averageProgress = Jdbc.queryBigDecimal(connection,
                    "SELECT COALESCE(AVG(PROGRESS_PERCENT), 0) FROM reading_progress", List.of());

            List<DashboardStats.RecentActivity> recentActivities = Jdbc.queryList(connection,
                    "SELECT CONCAT_WS(' ', u.NAMA_DEPAN, u.NAMA_BELAKANG) AS NAMA, "
                            + "e.JUDUL, r.STATUS_BACA, r.LAST_READ, r.PROGRESS_PERCENT "
                            + "FROM reading_progress r "
                            + "JOIN users u ON u.ID_USER = r.USER_ID_USER "
                            + "JOIN ebook e ON e.ID_BOOK = r.BOOK_ID_BOOK "
                            + "ORDER BY r.LAST_READ DESC, r.ID_PROGRESS DESC LIMIT ?",
                    Jdbc.params(recentLimit),
                    resultSet -> new DashboardStats.RecentActivity(
                            resultSet.getString("NAMA"),
                            resultSet.getString("JUDUL"),
                            ReadingStatus.fromDatabase(resultSet.getString("STATUS_BACA")),
                            Jdbc.getLocalDate(resultSet, "LAST_READ"),
                            Jdbc.getBigDecimal(resultSet, "PROGRESS_PERCENT")));

            List<DashboardStats.CategoryStat> categoryStats = Jdbc.queryList(connection,
                    "SELECT k.NAMA_KATEGORI, COUNT(e.ID_BOOK) AS JUMLAH "
                            + "FROM kategori k LEFT JOIN ebook e ON e.KATEGORI_ID_KATEGORI = k.ID_KATEGORI "
                            + "GROUP BY k.ID_KATEGORI, k.NAMA_KATEGORI "
                            + "ORDER BY JUMLAH DESC, k.NAMA_KATEGORI ASC LIMIT ?",
                    Jdbc.params(categoryLimit),
                    resultSet -> new DashboardStats.CategoryStat(
                            resultSet.getString("NAMA_KATEGORI"),
                            resultSet.getLong("JUMLAH")));

            return new DashboardStats(totalBooks, totalMembers, totalCategories, totalAuthors,
                    totalProgress, currentlyReading, completed, averageProgress,
                    recentActivities, categoryStats);
        });
    }
}
