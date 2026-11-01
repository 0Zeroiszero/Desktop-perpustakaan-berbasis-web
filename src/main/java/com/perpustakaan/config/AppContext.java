package com.perpustakaan.config;

import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.repository.DashboardRepository;
import com.perpustakaan.repository.EbookAuthorRepository;
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.IdSequenceRepository;
import com.perpustakaan.repository.KategoriRepository;
import com.perpustakaan.repository.ReadingProgressRepository;
import com.perpustakaan.repository.UserRepository;
import com.perpustakaan.security.PasswordHasher;
import com.perpustakaan.security.SessionManager;
import com.perpustakaan.service.AuthService;
import com.perpustakaan.service.AuthorService;
import com.perpustakaan.service.DashboardService;
import com.perpustakaan.service.EbookService;
import com.perpustakaan.service.FileStorageService;
import com.perpustakaan.service.KategoriService;
import com.perpustakaan.service.PdfService;
import com.perpustakaan.service.ReadingProgressService;
import com.perpustakaan.service.UserService;

/**
 * Composition root aplikasi: membuat repository dan service sekali saja.
 */
public final class AppContext {

    private static AppContext instance;

    private final AppConfig config;
    private final SessionManager sessionManager;
    private final TransactionManager transactionManager;

    private final AuthService authService;
    private final UserService userService;
    private final KategoriService kategoriService;
    private final AuthorService authorService;
    private final EbookService ebookService;
    private final ReadingProgressService readingProgressService;
    private final DashboardService dashboardService;

    private AppContext() {
        config = AppConfig.getInstance();
        sessionManager = new SessionManager();
        transactionManager = new TransactionManager();
        PasswordHasher passwordHasher = new PasswordHasher(config.getBcryptCost());
        FileStorageService fileStorageService = new FileStorageService(config);
        PdfService pdfService = new PdfService();

        UserRepository userRepository = new UserRepository();
        KategoriRepository kategoriRepository = new KategoriRepository();
        AuthorRepository authorRepository = new AuthorRepository();
        EbookRepository ebookRepository = new EbookRepository();
        EbookAuthorRepository ebookAuthorRepository = new EbookAuthorRepository();
        ReadingProgressRepository readingProgressRepository = new ReadingProgressRepository();
        DashboardRepository dashboardRepository = new DashboardRepository();
        IdSequenceRepository idSequenceRepository = new IdSequenceRepository();

        authService = new AuthService(userRepository, passwordHasher, transactionManager, idSequenceRepository);
        userService = new UserService(userRepository, readingProgressRepository, transactionManager,
                passwordHasher, idSequenceRepository);
        kategoriService = new KategoriService(kategoriRepository, idSequenceRepository, transactionManager);
        authorService = new AuthorService(authorRepository, idSequenceRepository, transactionManager);
        ebookService = new EbookService(ebookRepository, ebookAuthorRepository, kategoriRepository,
                authorRepository, readingProgressRepository, idSequenceRepository, transactionManager,
                fileStorageService, pdfService);
        readingProgressService = new ReadingProgressService(readingProgressRepository, ebookRepository,
                idSequenceRepository, transactionManager);
        dashboardService = new DashboardService(dashboardRepository, ebookRepository,
                readingProgressRepository, config);
    }

    public static synchronized AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public AppConfig getConfig() {
        return config;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }

    public KategoriService getKategoriService() {
        return kategoriService;
    }

    public AuthorService getAuthorService() {
        return authorService;
    }

    public EbookService getEbookService() {
        return ebookService;
    }

    public ReadingProgressService getReadingProgressService() {
        return readingProgressService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }
}
