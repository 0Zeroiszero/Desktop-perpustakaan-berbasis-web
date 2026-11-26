package com.perpustakaan.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Entitas reading_progress. Persentase dihitung dari last_page dan
 * books.page_count (jumlah halaman diberikan saat memuat data).
 */
public class ReadingProgress {

    private String idProgress;
    private String userId;
    private String bookId;
    private Integer lastPosition = 1;
    private int pageCount;
    private BigDecimal progressPercent = BigDecimal.ZERO;
    private LocalDate lastRead;
    private ReadingStatus status = ReadingStatus.AKAN_DIBACA;

    public ReadingProgress() {
    }

    public String getIdProgress() {
        return idProgress;
    }

    public void setIdProgress(String idProgress) {
        this.idProgress = idProgress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Integer lastPosition) {
        this.lastPosition = lastPosition == null ? 1 : lastPosition;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = Math.max(0, pageCount);
    }

    public int getPageCount() {
        return pageCount;
    }

    /** Persentase progres; memakai jumlah halaman bila tersedia. */
    public BigDecimal getProgressPercent() {
        if (pageCount > 0) {
            int page = lastPosition == null ? 0 : Math.max(0, Math.min(lastPosition, pageCount));
            return BigDecimal.valueOf(page)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(pageCount), 2, RoundingMode.HALF_UP);
        }
        return progressPercent == null ? BigDecimal.ZERO : progressPercent;
    }

    /** Fallback bila jumlah halaman tidak dikirimkan repository. */
    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent == null ? BigDecimal.ZERO : progressPercent;
    }

    public LocalDate getLastRead() {
        return lastRead;
    }

    public void setLastRead(LocalDate lastRead) {
        this.lastRead = lastRead;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status == null ? ReadingStatus.AKAN_DIBACA : status;
    }

    public boolean isCompleted() {
        return status == ReadingStatus.TELAH_DIBACA;
    }

    public int getProgressInt() {
        return getProgressPercent().max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).intValue();
    }
}
