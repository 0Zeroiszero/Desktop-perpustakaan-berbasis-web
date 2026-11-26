export type CodeEntry = {
  path: string;
  symbol: string;
  summary: string;
  snippet: string;
};

export type DocStep = {
  title: string;
  text: string;
};

export type DocSection = {
  id: string;
  label: string;
  title: string;
  paragraphs: string[];
  steps?: DocStep[];
  bullets?: string[];
  entries?: CodeEntry[];
  table?: { head: string[]; rows: string[][] };
};

const REPO = 'https://github.com/0Zeroiszero/Desktop-perpustakaan-berbasis-web/blob/main/';

export const repositoryBase = REPO;

export const sections: DocSection[] = [
  {
    id: 'tentang',
    label: 'Tentang',
    title: 'Tentang aplikasi',
    paragraphs: [
      'PustakaDigital Desktop adalah aplikasi perpustakaan digital yang berjalan di komputer. Aplikasi ini memakai database yang sama dengan proyek web PHP, yaitu optl_pustaka. Data buku, akun, dan progres baca langsung bersama.',
      'Berkas PDF dan cover disimpan di folder storage milik proyek desktop. Aplikasi tidak membaca folder web. Setiap tim boleh menyimpan proyek web di tempat dan dengan nama apa pun.',
      'Ada dua peran. Admin mengelola koleksi dan akun. Anggota mencari buku, menyimpan buku ke rak, lalu membaca PDF.',
    ],
    bullets: [
      'Java 21 dan JavaFX 21 memakai FXML serta CSS.',
      'Database MariaDB atau MySQL lewat JDBC, tanpa connection pool.',
      'Satu sesi desktop, jendela dibuka dalam keadaan maximized.',
      'Login memakai email. Hash password BCrypt cocok dengan hash dari PHP.',
    ],
  },
  {
    id: 'alur',
    label: 'Alur aplikasi',
    title: 'Alur aplikasi',
    paragraphs: [
      'Aplikasi dibuka langsung ke halaman login. Tidak ada halaman beranda publik. Setelah login, jendela utama memuat sidebar dan area konten.',
    ],
    steps: [
      {
        title: 'Menjalankan aplikasi',
        text: 'Main.start menyiapkan folder storage dan log, menguji koneksi database, lalu memanggil RootNavigator. Jendela ditampilkan dalam keadaan maximized.',
      },
      {
        title: 'Masuk',
        text: 'LoginController memanggil AuthService.login dengan email dan password. Sesi menyimpan pengguna beserta perannya.',
      },
      {
        title: 'Shell utama',
        text: 'Sidebar berisi Dashboard, Katalog Buku, Kelola Koleksi untuk admin, Kelola Pengguna untuk admin, Rak Saya, dan Keluar.',
      },
      {
        title: 'Katalog dan detail',
        text: 'Klik kartu buku membuka halaman detail. Tombol Lanjutkan pada kartu membuka reader. Nama berkas tidak ditampilkan.',
      },
      {
        title: 'Rak baca',
        text: 'Rak memakai empat status. Tiap baris menampilkan persentase dan halaman, misalnya 68% • 194 / 285.',
      },
      {
        title: 'Reader',
        text: 'Reader memakai tombol Sebelumnya dan Berikutnya. Status baca tampil di samping judul buku. Halaman yang sudah dibuka disimpan di cache supaya tidak berkedip.',
      },
    ],
    entries: [
      {
        path: 'src/main/java/com/perpustakaan/Main.java',
        symbol: 'Main.start(Stage)',
        summary: 'Titik masuk JavaFX. Menyiapkan folder, log, koneksi database, lalu menampilkan login.',
        snippet: `@Override
public void start(Stage stage) {
    Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
        AppLogger.error("Uncaught exception pada thread " + thread.getName(), error);
        Platform.runLater(() -> Ui.error("Terjadi kesalahan tak terduga."));
    });
    try {
        AppPaths.ensureDirectories();
        AppLogger.init();
        DatabaseConnection.testConnection();
        RootNavigator navigator = new RootNavigator(stage);
        Ui.setHost(navigator);
        navigator.start();
    } catch (RuntimeException error) {
        showStartupError(error);
    }
}`,
      },
      {
        path: 'src/main/java/com/perpustakaan/view/RootNavigator.java',
        symbol: 'RootNavigator.navigate(Route, NavigationContext)',
        summary: 'Memilih halaman login atau shell, lalu memeriksa status login dan peran admin.',
        snippet: `@Override
public void navigate(Route route, NavigationContext context) {
    Route target = route == null ? Route.LOGIN : route;
    if (target.isAuthPage()) {
        showAuth(target, context);
        return;
    }
    if (!session.isAuthenticated()) {
        showAuth(Route.LOGIN, NavigationContext.message("Silakan masuk terlebih dahulu."));
        return;
    }
    if (target.adminOnly() && !session.isAdmin()) {
        toast("Akses ditolak.", ToastType.ERROR);
        return;
    }
    shell.navigateTo(target, context);
}`,
      },
    ],
  },
  {
    id: 'panduan',
    label: 'Panduan pengguna',
    title: 'Panduan pengguna',
    paragraphs: [
      'Form menandai kolom wajib dengan bintang merah. Tombol simpan menyala setelah kolom tersebut terisi. Berlaku untuk form daftar, form buku, dan form pengguna.',
      'Di form buku, ketik nama penulis, genre, atau penerbit. Bila nama belum ada, aplikasi membuat datanya saat buku disimpan. Tombol ! di samping saran membuka popup profil lengkap, termasuk daftar buku dan slug penulis.',
      'Buku yang dinonaktifkan admin hilang dari katalog. Buku yang sudah ada di rak tetap terlihat dengan pesan Buku tidak tersedia. Tombol baca tidak aktif dan progres tidak hilang. Admin dapat mengaktifkan kembali buku dari Kelola Koleksi.',
      'Cover dibuat otomatis saat admin tidak mengunggah gambar. Cover mengikuti bentuk cover dari proyek web, dengan judul, penulis, genre, dan tulisan PDF.',
    ],
    bullets: [
      'Dashboard admin menampilkan lima buku terbaru serta lima genre, penulis, dan penerbit terbanyak.',
      'Rak Saya tersedia untuk admin dan anggota.',
      'Status Telah dibaca menyetel progres ke 100%. Status lain hanya mengubah label.',
      'Buku nonaktif tidak bisa dibaca, tetapi detailnya masih bisa dibuka.',
    ],
  },
  {
    id: 'kode',
    label: 'Peta kode',
    title: 'Peta kode dan fungsi utama',
    paragraphs: [
      'Kode memakai lapisan yang jelas. Controller menghubungkan FXML dengan service. Service menyimpan aturan bisnis dan transaksi. Repository berisi SQL. Model berisi data dan enum.',
      'Semua path di bawah memakai slash dan terhubung ke branch main di GitHub. Buka tautan path untuk melihat file aslinya.',
    ],
    entries: [
      {
        path: 'src/main/java/com/perpustakaan/service/EbookService.java',
        symbol: 'EbookService.create(User, BookFormData)',
        summary: 'Menyimpan buku baru. Penulis, genre, dan penerbit dibuat otomatis bila belum ada. Cover otomatis dibuat bila tidak diunggah.',
        snippet: `Ebook created = transactionManager.execute(connection -> {
    Author author = ensureAuthor(connection, validated.penulisNama());
    Kategori genre = ensureGenre(connection, validated.genreNama());
    Publisher publisher = ensurePublisher(connection, validated.penerbitNama());

    Ebook ebook = new Ebook();
    ebook.setJudul(validated.judul());
    ebook.setSlug(uniqueBookSlug(connection, validated.judul(), null));
    ebook.setFilePath(fileStorageService.saveEbookPdf(
            data.ebookSource(), ebook.getIdBook()));
    if (data.coverSource() != null) {
        ebook.setCoverPath(fileStorageService.saveCover(
                data.coverSource(), ebook.getIdBook()));
    } else {
        ebook.setCoverPath(coverService.generate(ebook.getIdBook(),
                validated.judul(), validated.penulisNama(), validated.genreNama()));
    }
    ebookRepository.insert(connection, ebook);
    return ebook;
});`,
      },
      {
        path: 'src/main/java/com/perpustakaan/service/ReadingProgressService.java',
        symbol: 'ReadingProgressService.setStatus(User, String, ReadingStatus)',
        summary: 'Mengubah label status. Hanya label Telah dibaca yang menyetel halaman terakhir ke jumlah halaman buku.',
        snippet: `public void setStatus(User actor, String bookId, ReadingStatus status) {
    AccessControl.requireAuthenticated(actor);
    Ebook ebook = ensureBook(bookId);
    boolean finished = status == ReadingStatus.TELAH_DIBACA;
    int pageCount = ebook.getJumlahHalaman() == null ? 0 : ebook.getJumlahHalaman();
    transactionManager.execute(connection -> {
        collectionRepository.setUserStatus(
                connection, actor.getIdUser(), bookId, status.dbValue());
        if (finished && pageCount > 0) {
            readingProgressRepository.upsert(
                    connection, actor.getIdUser(), bookId, pageCount);
        }
        return null;
    });
}`,
      },
      {
        path: 'src/main/java/com/perpustakaan/service/CoverService.java',
        symbol: 'CoverService.generate(String, String, String, String)',
        summary: 'Membuat cover 600x800 memakai font Inter yang dibundel di proyek.',
        snippet: `public String generate(String bookId, String title, String author, String genre) {
    BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setColor(BLUE);
    g.fillRect(0, 0, WIDTH, HEIGHT);

    Font bold = loadFont("/fonts/Inter-Bold.ttf", 44f);
    Font regular = loadFont("/fonts/Inter-Regular.ttf", 24f);
    g.setFont(bold);
    List<String> lines = wrap(g, safe(title), 3);
    int y = 210;
    for (String line : lines) {
        g.drawString(line, 48, y);
        y += 58;
    }
    Path destination = AppPaths.getCoverDir().resolve(bookId + ".jpg");
    ImageIO.write(image, "jpg", destination.toFile());
    return AppPaths.coverStoredPath(bookId);
}`,
      },
      {
        path: 'src/main/java/com/perpustakaan/config/AppPaths.java',
        symbol: 'AppPaths.resolveStoredPath(String)',
        summary: 'Menerjemahkan path relatif di database menjadi file lokal di folder storage desktop.',
        snippet: `public static Path resolveStoredPath(String storedPath) {
    String value = storedPath.trim().replace('\\\\', '/');
    String name;
    if (value.startsWith("storage/ebooks/")) {
        name = value.substring("storage/ebooks/".length());
    } else if (value.startsWith("uploads/covers/")) {
        name = value.substring("uploads/covers/".length());
    } else {
        name = value;
    }
    Path directory = name.toLowerCase().endsWith(".pdf") ? EBOOK_DIR : COVER_DIR;
    Path candidate = directory.resolve(name).normalize();
    return candidate.startsWith(STORAGE_ROOT) ? candidate : null;
}`,
      },
      {
        path: 'src/main/java/com/perpustakaan/service/ProfileService.java',
        symbol: 'ProfileService.load(User, String, String)',
        summary: 'Menyiapkan isi popup profil penulis, penerbit, atau genre beserta daftar bukunya.',
        snippet: `public ProfileInfo load(User actor, String type, String id) {
    AccessControl.requireAuthenticated(actor);
    return switch (type) {
        case "author" -> new ProfileInfo("Penulis", author.getNamaLengkap(),
                "ID: " + author.getIdAuthor() + "  •  Slug: " + author.getSlug(),
                author.getNegaraAsal(), author.getBio(), author.getJumlahBuku(),
                ebookRepository.findByMaster("author", id, userId, false));
        case "publisher" -> new ProfileInfo("Penerbit", publisher.getNama(),
                "ID: " + publisher.getIdPublisher(), publisher.getKota(), "",
                publisher.getJumlahBuku(),
                ebookRepository.findByMaster("publisher", id, userId, false));
        default -> throw new AppException("Jenis profil tidak dikenal.");
    };
}`,
      },
    ],
  },
  {
    id: 'data',
    label: 'Database',
    title: 'Database dan aturan data',
    paragraphs: [
      'Aplikasi memakai database optl_pustaka yang sama dengan proyek web. Database lama db_pustaka tidak dipakai lagi. Skema memakai tujuh tabel dan satu view.',
      'Tabel books menyimpan buku. Tabel authors, genres, dan publishers menyimpan master data. Tabel collections menyimpan rak baca dengan empat status. Tabel reading_progress menyimpan halaman terakhir per pengguna dan buku.',
      'Persentase baca dihitung dari halaman terakhir dibanding jumlah halaman. Status hanya label. Pengecualian ada pada label Telah dibaca, yang menyetel halaman terakhir ke jumlah halaman buku.',
    ],
    table: {
      head: ['Tabel', 'Isi'],
      rows: [
        ['users', 'Akun, email, hash password, peran, dan status aktif.'],
        ['books', 'Buku, slug, ISBN, path berkas, cover, jumlah halaman, dan status aktif.'],
        ['authors, genres, publishers', 'Master data dengan slug unik.'],
        ['collections', 'Rak baca: to_read, reading, finished, not_interested.'],
        ['reading_progress', 'Halaman terakhir per pengguna dan buku.'],
        ['v_books_catalog', 'View gabungan untuk katalog dan detail.'],
      ],
    },
    entries: [
      {
        path: 'src/main/java/com/perpustakaan/repository/EbookRepository.java',
        symbol: 'EbookRepository.findByMaster(String, String, String, boolean)',
        summary: 'Mengambil buku untuk popup profil. Filter memakai tabel books karena view tidak membawa author_id dan publisher_id.',
        snippet: `String column = switch (master) {
    case "author" -> "author_id";
    case "publisher" -> "publisher_id";
    case "genre" -> "genre_id";
    default -> throw new IllegalArgumentException("Jenis profil tidak dikenal");
};
String sql = "SELECT v.book_id AS ID_BOOK, v.title AS JUDUL, "
        + "v.author_name AS PENULIS, v.cover_path AS COVER, "
        + "v.published_year AS TAHUN_TERBIT "
        + "FROM v_books_catalog v "
        + "JOIN books b ON b.book_id = v.book_id "
        + "WHERE b." + column + " = ? ORDER BY v.title ASC";`,
      },
    ],
  },
  {
    id: 'uji',
    label: 'Pengujian',
    title: 'Pengujian',
    paragraphs: [
      'Jalankan seluruh test dengan perintah ./mvnw.cmd test dari root project. Hook pre-commit menjalankan perintah yang sama sebelum setiap commit.',
      'IntegrationSmokeTest menguji koneksi database, login, katalog, rak, dan file lokal. UiSmokeTest memuat semua FXML, menjalankan shell, dan mengambil screenshot tiap halaman. UserScenarioTest mengklik delapan operasi CRUD dan menghitung jumlah klik.',
      'ReadingRuleTest menguji aturan status baca. ProfileLookupTest memastikan tombol ! membaca data profil tanpa error query.',
    ],
    table: {
      head: ['Fitur', 'Operasi', 'Klik'],
      rows: [
        ['Anggota', 'Tambah ke rak', '3'],
        ['Anggota', 'Baca buku', '2'],
        ['Anggota', 'Ganti status', '1'],
        ['Anggota', 'Hapus dari rak', '2'],
        ['Admin', 'Tambah penulis', '3'],
        ['Admin', 'Lihat buku', '2'],
        ['Admin', 'Ubah buku', '2'],
        ['Admin', 'Nonaktifkan buku', '2'],
      ],
    },
    bullets: [
      'Batas aturan tiga klik dipakai untuk setiap operasi.',
      'Semua operasi di atas lulus batas tersebut.',
      'Screenshot tersimpan di folder %TEMP%\\opencode\\pustaka-gui-smoke dan pustaka-user-scenarios.',
    ],
  },
];
