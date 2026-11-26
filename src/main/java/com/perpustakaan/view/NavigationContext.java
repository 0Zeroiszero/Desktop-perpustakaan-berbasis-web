package com.perpustakaan.view;

/**
 * Konteks tambahan saat berpindah halaman.
 */
public record NavigationContext(String bookId, Route origin, boolean restart, String message,
                                Route detailOrigin) {

    public static NavigationContext book(String bookId, Route origin, boolean restart) {
        return new NavigationContext(bookId, origin, restart, null, null);
    }

    /**
     * Membuka reader dan mengingat halaman asal detail agar tombol kembali
     * tetap mengarah ke halaman yang benar.
     */
    public static NavigationContext reader(String bookId, Route detailOrigin) {
        return new NavigationContext(bookId, Route.BOOK_DETAIL, false, null, detailOrigin);
    }

    public static NavigationContext message(String message) {
        return new NavigationContext(null, null, false, message, null);
    }

    public static NavigationContext empty() {
        return new NavigationContext(null, null, false, null, null);
    }
}
