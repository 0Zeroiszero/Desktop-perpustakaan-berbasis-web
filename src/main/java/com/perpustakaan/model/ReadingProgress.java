package com.perpustakaan.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entitas reading_progress.
 */
public class ReadingProgress {

    private String idProgress;
    private String userId;
    private String bookId;
    private BigDecimal progressPercent = BigDecimal.ZERO;
    private LocalDate lastRead;
    private Integer lastPosition;
    private LocalDate startDate;
    private LocalDate endDate;
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

    public BigDecimal getProgressPercent() {
        return progressPercent == null ? BigDecimal.ZERO : progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent == null ? BigDecimal.ZERO : progressPercent;
    }

    public LocalDate getLastRead() {
        return lastRead;
    }

    public void setLastRead(LocalDate lastRead) {
        this.lastRead = lastRead;
    }

    public Integer getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Integer lastPosition) {
        this.lastPosition = lastPosition;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
