package com.perpustakaan.repository;

import com.perpustakaan.config.DatabaseConnection;
import com.perpustakaan.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base repository yang mengelola pembukaan/penutupan koneksi.
 */
public abstract class RepositorySupport {

    protected <T> T read(SqlWork<T> work) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return work.run(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengakses database.", e);
        }
    }

    protected int write(SqlWork<Integer> work) {
        return read(work);
    }

    @FunctionalInterface
    protected interface SqlWork<T> {
        T run(Connection connection);
    }
}
