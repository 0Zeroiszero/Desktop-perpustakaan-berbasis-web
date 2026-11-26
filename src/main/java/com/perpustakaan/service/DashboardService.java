package com.perpustakaan.service;

import com.perpustakaan.config.AppConfig;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.DashboardStats;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.Publisher;
import com.perpustakaan.model.ReadingStats;
import com.perpustakaan.model.ReadingStatus;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.repository.CollectionRepository;
import com.perpustakaan.repository.DashboardRepository;
import com.perpustakaan.repository.KategoriRepository;
import com.perpustakaan.repository.PublisherRepository;
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.ReadingProgressRepository;
import com.perpustakaan.security.AccessControl;

import java.util.Comparator;
import java.util.List;

/**
 * Menyusun data dashboard admin dan anggota.
 */
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final EbookRepository ebookRepository;
    private final CollectionRepository collectionRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final KategoriRepository kategoriRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final AppConfig config;

    public DashboardService(DashboardRepository dashboardRepository, EbookRepository ebookRepository,
                            CollectionRepository collectionRepository,
                            ReadingProgressRepository readingProgressRepository,
                            KategoriRepository kategoriRepository, AuthorRepository authorRepository,
                            PublisherRepository publisherRepository, AppConfig config) {
        this.dashboardRepository = dashboardRepository;
        this.ebookRepository = ebookRepository;
        this.collectionRepository = collectionRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.kategoriRepository = kategoriRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.config = config;
    }

    public DashboardStats admin(User actor) {
        AccessControl.requireAdmin(actor);
        return dashboardRepository.loadAdminStats(config.getDashboardRecentLimit(), 5);
    }

    /** Ringkasan lima item teratas untuk kartu dashboard admin. */
    public Summary summary(User actor) {
        AccessControl.requireAdmin(actor);
        List<BookListItem> books = ebookRepository.findRecent(actor.getIdUser(), 5);
        List<Kategori> genres = kategoriRepository.findAll().stream()
                .sorted(Comparator.comparingLong(Kategori::getJumlahBuku).reversed()).limit(5).toList();
        List<Author> authors = authorRepository.findAll().stream()
                .sorted(Comparator.comparingLong(Author::getJumlahBuku).reversed()).limit(5).toList();
        List<Publisher> publishers = publisherRepository.findAll().stream()
                .sorted(Comparator.comparingLong(Publisher::getJumlahBuku).reversed()).limit(5).toList();
        return new Summary(books, genres, authors, publishers);
    }

    public record Summary(List<BookListItem> books, List<Kategori> genres,
                          List<Author> authors, List<Publisher> publishers) {
    }

    public ReadingStats member(User actor) {
        AccessControl.requireAuthenticated(actor);
        String userId = actor.getIdUser();
        long totalBooks = ebookRepository.countAll();
        long shelfCount = collectionRepository.countByUser(userId);
        long currentlyReading = collectionRepository.countByUserAndStatus(
                userId, ReadingStatus.SEDANG_DIBACA.dbValue());
        long completed = collectionRepository.countByUserAndStatus(
                userId, ReadingStatus.TELAH_DIBACA.dbValue());
        BookListItem lastRead = readingProgressRepository.findLastReadBook(userId);
        List<BookListItem> latest = ebookRepository.findRecent(userId, config.getRecommendationLimit());
        return new ReadingStats(totalBooks, shelfCount, currentlyReading, completed, lastRead, latest);
    }
}
