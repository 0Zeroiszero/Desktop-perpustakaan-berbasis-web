package com.pbo.data;

import com.pbo.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Rak memakai akun demo USR002 (Andi Saputra) karena layar login belum dikonversi.
 * Semua penulisan tetap mengikuti skema reading_progress yang ada.
 */
public final class ShelfRepository {

    public static final String CURRENT_USER_ID = "USR002";

    public enum Result {
        ADDED,
        UPDATED
    }

    private ShelfRepository() {
    }

    public static String currentUserName() throws SQLException {
        String sql = "SELECT CONCAT(NAMA_DEPAN, IFNULL(CONCAT(' ', NAMA_BELAKANG), '')) "
                + "FROM users WHERE ID_USER = ?";
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, CURRENT_USER_ID);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString(1) : "Anggota";
            }
        }
    }

    /** Status rak buku milik pengguna aktif, dipetakan dari ID buku. */
    public static Map<String, String> findStatuses() throws SQLException {
        String sql = "SELECT BOOK_ID_BOOK, STATUS_BACA FROM reading_progress WHERE USER_ID_USER = ?";
        Map<String, String> statuses = new HashMap<>();
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, CURRENT_USER_ID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    statuses.put(rs.getString("BOOK_ID_BOOK"), rs.getString("STATUS_BACA"));
                }
            }
        }
        return statuses;
    }

    public static Result save(Book book, String status) throws SQLException {
        try (Connection connection = Database.open()) {
            String update = "UPDATE reading_progress SET STATUS_BACA = ? "
                    + "WHERE USER_ID_USER = ? AND BOOK_ID_BOOK = ?";
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setString(1, status);
                statement.setString(2, CURRENT_USER_ID);
                statement.setString(3, book.id());
                if (statement.executeUpdate() > 0) {
                    return Result.UPDATED;
                }
            }

            String nextId;
            // shortcut: id dihitung dari MAX + 1; aman untuk pemakaian lokal satu pengguna.
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT COALESCE(MAX(CAST(SUBSTRING(ID_PROGRESS, 4) AS UNSIGNED)), 0) + 1 "
                                 + "FROM reading_progress")) {
                rs.next();
                nextId = String.format("PRG%03d", rs.getInt(1));
            }

            String sql = "INSERT INTO reading_progress "
                    + "(ID_PROGRESS, USER_ID_USER, BOOK_ID_BOOK, PROGRESS_PERCENT, START_DATE, STATUS_BACA) "
                    + "SELECT ?, ?, ?, 0, CURDATE(), ? FROM DUAL "
                    + "WHERE NOT EXISTS (SELECT 1 FROM reading_progress "
                    + "WHERE USER_ID_USER = ? AND BOOK_ID_BOOK = ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, nextId);
                statement.setString(2, CURRENT_USER_ID);
                statement.setString(3, book.id());
                statement.setString(4, status);
                statement.setString(5, CURRENT_USER_ID);
                statement.setString(6, book.id());
                return statement.executeUpdate() == 0 ? Result.UPDATED : Result.ADDED;
            }
        }
    }
}
