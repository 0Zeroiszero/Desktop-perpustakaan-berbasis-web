package com.perpustakaan.config;

import com.perpustakaan.exception.DataAccessException;
import com.perpustakaan.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Mengelola transaksi database dengan commit/rollback eksplisit.
 */
public final class TransactionManager {

    public <T> T execute(TransactionWork<T> work) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                    AppLogger.error("Rollback transaksi gagal.", rollbackError);
                }
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new DataAccessException("Transaksi database gagal.", e);
            } finally {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException cleanupError) {
                    AppLogger.warn("Gagal mengembalikan auto-commit (koneksi akan ditutup).", cleanupError);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transaksi database gagal.", e);
        }
    }

    @FunctionalInterface
    public interface TransactionWork<T> {
        T run(Connection connection) throws Exception;
    }
}
