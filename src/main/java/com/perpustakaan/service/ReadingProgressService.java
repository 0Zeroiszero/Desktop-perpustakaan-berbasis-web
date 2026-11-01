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
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.IdSequenceRepository;
import com.perpustakaan.repository.ReadingProgressRepository;
import com.perpustakaan.security.AccessControl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Rak baca dan progres membaca per pengguna.
 */
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final EbookRepository ebookRepository;
    private final IdSequenceRepository idSequenceRepository;
    private final TransactionManager transactionManager;

    public ReadingProgressService(ReadingProgressRepository readingProgressRepository,
                                  EbookRepository ebookRepository, IdSequenceRepository idSequenceRepository,
                                  TransactionManager transactionManager) {
        this.readingProgressRepository = readingProgressRepository;
        this.ebookRepository = ebookRepository;
        this.idSequenceRepository = idSequenceRepository;
        this.transactionManager = transactionManager;
    }

    public PageResult<BookListItem> shelf(User actor, String search, String sort, int page, int pageSize) {
        AccessControl.requireAuthenticated(actor);
        return readingProgressRepository.findShelf(actor.getIdUser(), search, sort, page, pageSize);
    }

    public ReadingProgress find(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        return readingProgressRepository.findByUserAndBook(actor.getIdUser(), bookId);
    }

    public ReadingProgress addToShelf(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        ensureBook(bookId);
        ReadingProgress existing = readingProgressRepository.findByUserAndBook(actor.getIdUser(), bookId);
        if (existing != null) {
            return existing;
        }
        ReadingProgress progress = new ReadingProgress();
        progress.setUserId(actor.getIdUser());
        progress.setBookId(bookId);
        progress.setProgressPercent(BigDecimal.ZERO);
        progress.setLastPosition(0);
        progress.setStatus(ReadingStatus.AKAN_DIBACA);
        transactionManager.execute(connection -> {
            progress.setIdProgress(idSequenceRepository.nextId(
                    connection, IdSequenceRepository.EntityType.PROGRESS));
            readingProgressRepository.insert(connection, progress);
            return progress;
        });
        return progress;
    }

    public void removeFromShelf(User actor, String bookId) {
        AccessControl.requireAuthenticated(actor);
        transactionManager.execute(connection -> {
            readingProgressRepository.deleteByUserAndBook(connection, actor.getIdUser(), bookId);
            return null;
        });
    }

    public ReadingProgress startReading(User actor, String bookId, int totalPages) {
        AccessControl.requireAuthenticated(actor);
        validateTotalPages(totalPages);
        ensureBook(bookId);

        ReadingProgress existing = readingProgressRepository.findByUserAndBook(actor.getIdUser(), bookId);
        LocalDate today = LocalDate.now();
        if (existing == null) {
            ReadingProgress progress = new ReadingProgress();
            progress.setUserId(actor.getIdUser());
            progress.setBookId(bookId);
            progress.setLastPosition(1);
            progress.setProgressPercent(calculatePercent(1, totalPages));
            progress.setStartDate(today);
            progress.setLastRead(today);
            progress.setStatus(ReadingStatus.SEDANG_DIBACA);
            transactionManager.execute(connection -> {
                progress.setIdProgress(idSequenceRepository.nextId(
                        connection, IdSequenceRepository.EntityType.PROGRESS));
                readingProgressRepository.insert(connection, progress);
                return progress;
            });
            return progress;
        }

        int originalPosition = existing.getLastPosition() == null ? 0 : existing.getLastPosition();
        int lastPosition = clampPage(originalPosition <= 0 ? 1 : originalPosition, totalPages);
        existing.setLastPosition(lastPosition);
        existing.setLastRead(today);
        if (existing.getStatus() == ReadingStatus.TELAH_DIBACA) {
            transactionManager.execute(connection -> {
                readingProgressRepository.update(connection, existing);
                return null;
            });
            return existing;
        }
        if (existing.getStatus() == ReadingStatus.AKAN_DIBACA || originalPosition <= 0) {
            existing.setLastPosition(1);
            existing.setProgressPercent(calculatePercent(1, totalPages));
        }
        existing.setStatus(ReadingStatus.SEDANG_DIBACA);
        existing.setEndDate(null);
        if (existing.getStartDate() == null) {
            existing.setStartDate(today);
        }
        transactionManager.execute(connection -> {
            readingProgressRepository.update(connection, existing);
            return null;
        });
        return existing;
    }

    public ReadingProgress saveProgress(User actor, String bookId, int page, int totalPages) {
        AccessControl.requireAuthenticated(actor);
        validateTotalPages(totalPages);
        if (page < 1 || page > totalPages) {
            throw new ValidationException("page", "Halaman tidak valid.");
        }
        ensureBook(bookId);

        ReadingProgress progress = readingProgressRepository.findByUserAndBook(actor.getIdUser(), bookId);
        boolean insert = progress == null;
        LocalDate today = LocalDate.now();
        if (insert) {
            progress = new ReadingProgress();
            progress.setUserId(actor.getIdUser());
            progress.setBookId(bookId);
            progress.setStartDate(today);
        }
        progress.setLastPosition(page);
        progress.setProgressPercent(calculatePercent(page, totalPages));
        progress.setLastRead(today);
        if (progress.getStartDate() == null) {
            progress.setStartDate(today);
        }
        if (page >= totalPages) {
            progress.setStatus(ReadingStatus.TELAH_DIBACA);
            progress.setEndDate(today);
        } else {
            progress.setStatus(ReadingStatus.SEDANG_DIBACA);
            progress.setEndDate(null);
        }

        ReadingProgress saved = progress;
        transactionManager.execute(connection -> {
            if (insert) {
                saved.setIdProgress(idSequenceRepository.nextId(
                        connection, IdSequenceRepository.EntityType.PROGRESS));
                readingProgressRepository.insert(connection, saved);
            } else {
                readingProgressRepository.update(connection, saved);
            }
            return null;
        });
        return saved;
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

    private void ensureBook(String bookId) {
        Ebook ebook = ebookRepository.findById(bookId);
        if (ebook == null) {
            throw new AppException("Buku tidak ditemukan.");
        }
    }

    private void validateTotalPages(int totalPages) {
        if (totalPages <= 0) {
            throw new ValidationException("totalPages", "Jumlah halaman PDF tidak valid.");
        }
    }
}
