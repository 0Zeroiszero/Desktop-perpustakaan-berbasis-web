package com.perpustakaan.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ringkasan data untuk dashboard admin.
 */
public record DashboardStats(
        long totalBooks,
        long totalMembers,
        long totalCategories,
        long totalAuthors,
        long totalProgress,
        long currentlyReading,
        long completed,
        BigDecimal averageProgress,
        List<RecentActivity> recentActivities,
        List<CategoryStat> categoryStats
) {

    public DashboardStats {
        averageProgress = averageProgress == null ? BigDecimal.ZERO : averageProgress;
        recentActivities = recentActivities == null ? List.of() : List.copyOf(recentActivities);
        categoryStats = categoryStats == null ? List.of() : List.copyOf(categoryStats);
    }

    public record RecentActivity(
            String userName,
            String bookTitle,
            ReadingStatus status,
            LocalDate lastRead,
            BigDecimal progressPercent
    ) {
    }

    public record CategoryStat(String categoryName, long bookCount) {
    }
}
