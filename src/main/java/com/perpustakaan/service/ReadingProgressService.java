package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.ReadingProgress;
import com.perpustakaan.model.ReadingStatus;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.CollectionRepository;
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.ReadingProgressRepository;
import com.perpustakaan.security.AccessControl;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Rak baca dan progres membaca. Persentase selalu mengikuti halaman terakhir.
 * Status hanya label: tidak mengubah progres, kecuali label "Telah dibaca"
 * yang memaksa halaman terakhir menjadi jumlah halaman buku (100%).
 * Status sistem tetap diisi otomatis saat mulai baca dan saat halaman terakhir.
 */
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final EbookRepository ebookRepository;
    private final CollectionRepository collectionRepository;
    private final TransactionManager transactionManager;

    public ReadingProgressService(ReadingProgressRepository readingProgressRepository,
                                  EbookRepository ebookRepository, CollectionRepository collectionRepository,
                                  TransactionManager transactionManager) {
        this.readingProgressRepository = readingProgressRepository;
        this.ebookRepository = ebookRepository;
        this.collectionRepository = collectionRepository;
        this.transactionManager = transactionManager;
    }

    public PageResult<BookListItem> shelf(User actor, String search, String status,
                                          String sort, int page, int size) {
        AccessControl.requireAuthenticated(actor);
        return readingProgressRepository.findShelf(actor.getIdUser(), search, status, sort, page, size);
    }

    public ReadingProgress find(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        return readingProgressRepository.findByUserAndBook(actor.getIdUser(), bookId);
    }

    public void addToShelf(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        ensureBook(bookId);
        transactionManager.execute(connection -> {
            collectionRepository.ensure(connection, actor.getIdUser(), bookId,
                    ReadingStatus.AKAN_DIBACA.dbValue(), "user");
            return null;
        });
    }

    public void removeFromShelf(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        transactionManager.execute(connection -> {
            collectionRepository.delete(connection, actor.getIdUser(), bookId);
            readingProgressRepository.deleteByUserAndBook(connection, actor.getIdUser(), bookId);
            return null;
        });
    }

    public void setStatus(User actor, String bookId, ReadingStatus status) {
        AccessControl.requireAuthenticated(actor);
        if (status == null) {
            throw new ValidationException("status", "Status baca wajib dipilih.");
        }
        Ebook ebook = ensureBook(bookId);
        String userId = actor.getIdUser();
        boolean finished = status == ReadingStatus.TELAH_DIBACA;
        int pageCount = ebook.getJumlahHalaman() == null ? 0 : ebook.getJumlahHalaman();
        transactionManager.execute(connection -> {
            collectionRepository.setUserStatus(connection, userId, bookId, status.dbValue());
            // Label selesai dibaca adalah satu-satunya status yang menyentuh progres.
            if (finished && pageCount > 0) {
                readingProgressRepository.upsert(connection, userId, bookId, pageCount);
            }
            return null;
        });
    }

    public ReadingProgress startReading(User actor, String bookId, int pageCount) {
        AccessControl.requireAuthenticated(actor);
        validateTotalPages(pageCount);
        ensureBook(bookId);
        String userId = actor.getIdUser();
        boolean firstRead = readingProgressRepository.findByUserAndBook(userId, bookId) == null;

        transactionManager.execute(connection -> {
            collectionRepository.setSystemStatus(connection, userId, bookId, ReadingStatus.SEDANG_DIBACA.dbValue());
            if (firstRead) {
                readingProgressRepository.upsert(connection, userId, bookId, 1);
            }
            return null;
        });
        return loadProgress(userId, bookId);
    }

    public ReadingProgress saveProgress(User actor, String bookId, int page, int totalPages) {
        AccessControl.requireAuthenticated(actor);
        validateTotalPages(totalPages);
        ensureBook(bookId);
        int clamped = clampPage(page, totalPages);
        String userId = actor.getIdUser();

        transactionManager.execute(connection -> {
            readingProgressRepository.upsert(connection, userId, bookId, clamped);
            String status = clamped >= totalPages
                    ? ReadingStatus.TELAH_DIBACA.dbValue()
                    : ReadingStatus.SEDANG_DIBACA.dbValue();
            collectionRepository.setSystemStatus(connection, userId, bookId, status);
            return null;
        });
        return loadProgress(userId, bookId);
    }

    public ReadingProgress restart(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        ensureBook(bookId);
        String userId = actor.getIdUser();
        transactionManager.execute(connection -> {
            readingProgressRepository.upsert(connection, userId, bookId, 1);
            collectionRepository.setSystemStatus(connection, userId, bookId, ReadingStatus.SEDANG_DIBACA.dbValue());
            return null;
        });
        return loadProgress(userId, bookId);
    }

    private ReadingProgress loadProgress(String userId, String bookId) {
        ReadingProgress progress = readingProgressRepository.findByUserAndBook(userId, bookId);
        if (progress == null) {
            throw new AppException("Progres membaca tidak ditemukan.");
        }
        return progress;
    }

    public static BigDecimal calculatePercent(int page, int totalPages) {
        if (totalPages <= 0) {
            return BigDecimal.ZERO;
        }
        int safePage = Math.max(0, Math.min(page, totalPages));
        return BigDecimal.valueOf(safePage)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalPages), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
    }

    public static int clampPage(int page, int totalPages) {
        if (totalPages <= 0) {
            return 1;
        }
        return Math.max(1, Math.min(page, totalPages));
    }

    private Ebook ensureBook(String bookId) {
        Ebook ebook = ebookRepository.findById(bookId);
        if (ebook == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
        return ebook;
    }

    private void validateTotalPages(int totalPages) {
        if (totalPages <= 0) {
            throw new ValidationException("totalPages", "Jumlah halaman PDF tidak valid.");
        }
    }
}
