package com.perpustakaan.config;

import com.perpustakaan.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Koneksi JDBC tanpa connection pool (aplikasi desktop single-user session).
 * Semua koneksi wajib ditutup dengan try-with-resources.
 */
public final class DatabaseConnection {

    static {
        DriverManager.setLoginTimeout(8);
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        AppConfig config = AppConfig.getInstance();
        try {
            return DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Tidak dapat terhubung ke database. Pastikan MySQL berjalan dan konfigurasi benar.", e);
        }
    }

    public static void testConnection() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            if (!resultSet.next()) {
                throw new DataAccessException("Database tidak merespons dengan benar.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Tidak dapat terhubung ke database. Pastikan MySQL berjalan dan konfigurasi benar.", e);
        }
    }
}
