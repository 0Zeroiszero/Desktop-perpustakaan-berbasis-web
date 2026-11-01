package com.pbo.data;

import com.pbo.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Membaca katalog langsung dari tabel ebook, kategori, dan author. */
public final class BookRepository {

    private static final String BOOKS_SQL = """
            SELECT e.ID_BOOK, e.JUDUL, e.TAHUN_TERBIT, e.JUMLAH_HALAMAN, e.ISBN, e.SINOPSIS,
                   e.COVER, e.FILE_BOOK, e.FORMAT_FILE, k.NAMA_KATEGORI,
                   (SELECT GROUP_CONCAT(CONCAT(a.NAMA_DEPAN,
                            IFNULL(CONCAT(' ', a.NAMA_BELAKANG), ''))
                            ORDER BY a.NAMA_DEPAN SEPARATOR ', ')
                      FROM ebook_author ea
                      JOIN author a ON a.ID_AUTHOR = ea.AUTHOR_ID_AUTHOR
                     WHERE ea.BOOK_ID_BOOK = e.ID_BOOK) AS PENULIS
              FROM ebook e
              JOIN kategori k ON k.ID_KATEGORI = e.KATEGORI_ID_KATEGORI
             ORDER BY e.JUDUL
            """;

    private static final String CATEGORIES_SQL =
            "SELECT NAMA_KATEGORI FROM kategori ORDER BY NAMA_KATEGORI";

    private BookRepository() {
    }

    public static List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(BOOKS_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getString("ID_BOOK"),
                        rs.getString("JUDUL"),
                        text(rs, "PENULIS"),
                        rs.getString("NAMA_KATEGORI"),
                        rs.getString("COVER"),
                        rs.getString("FILE_BOOK"),
                        text(rs, "FORMAT_FILE"),
                        rs.getObject("TAHUN_TERBIT", Integer.class),
                        rs.getObject("JUMLAH_HALAMAN", Integer.class),
                        text(rs, "ISBN"),
                        synopsis(rs)
                ));
            }
        }
        return books;
    }

    public static List<String> findCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(CATEGORIES_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("NAMA_KATEGORI"));
            }
        }
        return categories;
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String synopsis(ResultSet rs) throws SQLException {
        String value = rs.getString("SINOPSIS");
        return value == null ? "" : value;
    }
}
