package com.perpustakaan.view;

/**
 * Daftar halaman aplikasi beserta metadata navigasi.
 */
public enum Route {
    LOGIN("/fxml/auth/login.fxml", "Masuk", "", false, false, false),
    REGISTER("/fxml/auth/register.fxml", "Daftar Akun", "", false, false, false),

    ADMIN_DASHBOARD("/fxml/admin/dashboard.fxml", "Dashboard", "Ringkasan aktivitas perpustakaan.", true, true, false),
    BOOKS("/fxml/admin/books.fxml", "Kelola Buku", "Tambah, ubah, dan hapus koleksi e-book.", true, true, false),
    CATEGORIES("/fxml/admin/categories.fxml", "Kelola Kategori", "Atur kategori koleksi perpustakaan.", true, true, false),
    AUTHORS("/fxml/admin/authors.fxml", "Kelola Penulis", "Kelola data penulis dan relasinya.", true, true, false),
    USERS("/fxml/admin/users.fxml", "Kelola Pengguna", "Kelola akun admin dan anggota.", true, true, false),

    MEMBER_DASHBOARD("/fxml/member/dashboard.fxml", "Dashboard", "Lanjutkan perjalanan membaca Anda.", false, true, false),
    CATALOG("/fxml/catalog/catalog.fxml", "Katalog Buku", "Jelajahi koleksi digital perpustakaan.", false, true, false),
    BOOK_DETAIL("/fxml/catalog/book-detail.fxml", "Detail Buku", "Informasi lengkap koleksi.", false, true, false),
    MY_SHELF("/fxml/member/my-shelf.fxml", "Rak Saya", "Lanjutkan bacaan dan kelola koleksi pribadi.", false, true, false),
    READER("/fxml/reader/reader.fxml", "Membaca", "", false, true, true);

    private final String fxmlPath;
    private final String title;
    private final String subtitle;
    private final boolean adminOnly;
    private final boolean requiresAuth;
    private final boolean immersive;

    Route(String fxmlPath, String title, String subtitle, boolean adminOnly,
          boolean requiresAuth, boolean immersive) {
        this.fxmlPath = fxmlPath;
        this.title = title;
        this.subtitle = subtitle;
        this.adminOnly = adminOnly;
        this.requiresAuth = requiresAuth;
        this.immersive = immersive;
    }

    public String fxmlPath() {
        return fxmlPath;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public boolean adminOnly() {
        return adminOnly;
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public boolean immersive() {
        return immersive;
    }

    public boolean isAuthPage() {
        return this == LOGIN || this == REGISTER;
    }
}
