package com.perpustakaan.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO ringkas untuk kartu dan tabel buku.
 */
public record BookListItem(
        String idBook,
        String judul,
        String penulis,
        String kategori,
        String coverPath,
        Integer tahunTerbit,
        Integer jumlahHalaman,
        BigDecimal progressPercent,
        Integer lastPosition,
        ReadingStatus status,
        LocalDate lastRead,
        boolean active
) {

    public BookListItem {
        penulis = penulis == null || penulis.isBlank() ? "Tanpa penulis" : penulis;
        kategori = kategori == null ? "-" : kategori;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasProgress() {
        return status != null || progressPercent != null;
    }

    public int progressInt() {
        if (progressPercent == null) {
            return 0;
        }
        return progressPercent.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).intValue();
    }

    public String statusLabel() {
        return status == null ? "-" : status.label();
    }

    public String halamanLabel() {
        if (jumlahHalaman == null || jumlahHalaman <= 0) {
            return "-";
        }
        int posisi = lastPosition == null ? 0 : Math.max(0, Math.min(lastPosition, jumlahHalaman));
        return posisi + " / " + jumlahHalaman;
    }
}
