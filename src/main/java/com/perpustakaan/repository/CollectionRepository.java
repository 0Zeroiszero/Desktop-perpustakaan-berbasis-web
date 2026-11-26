package com.perpustakaan.repository;

import java.sql.Connection;
import java.util.List;

/**
 * Repository tabel collections (rak baca pengguna).
 * Status yang dipilih pengguna tidak pernah ditimpa oleh sistem.
 */
public class CollectionRepository extends RepositorySupport {

    public CollectionState find(String userId, String bookId) {
        return read(connection -> Jdbc.queryOne(connection,
                "SELECT status, status_source FROM collections WHERE user_id = ? AND book_id = ?",
                Jdbc.params(userId, bookId), resultSet -> new CollectionState(
                        resultSet.getString("status"), resultSet.getString("status_source"))));
    }

    /** Menambah buku ke rak baca bila belum ada. */
    public void ensure(Connection connection, String userId, String bookId, String status, String source) {
        Jdbc.update(connection,
                "INSERT INTO collections (user_id, book_id, status, status_source) VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE status = status",
                Jdbc.params(userId, bookId, status, source));
    }

    /** Status yang dipilih pengguna selalu mengubah status_source menjadi 'user'. */
    public void setUserStatus(Connection connection, String userId, String bookId, String status) {
        Jdbc.update(connection,
                "INSERT INTO collections (user_id, book_id, status, status_source) VALUES (?, ?, ?, 'user') "
                        + "ON DUPLICATE KEY UPDATE status = VALUES(status), status_source = 'user'",
                Jdbc.params(userId, bookId, status));
    }

    /** Status sistem hanya diterapkan bila belum ada pilihan pengguna. */
    public void setSystemStatus(Connection connection, String userId, String bookId, String status) {
        Jdbc.update(connection,
                "INSERT INTO collections (user_id, book_id, status, status_source) VALUES (?, ?, ?, 'system') "
                        + "ON DUPLICATE KEY UPDATE status = IF(status_source = 'system', VALUES(status), status)",
                Jdbc.params(userId, bookId, status));
    }

    public void delete(Connection connection, String userId, String bookId) {
        Jdbc.update(connection, "DELETE FROM collections WHERE user_id = ? AND book_id = ?",
                Jdbc.params(userId, bookId));
    }

    public void deleteByUser(Connection connection, String userId) {
        Jdbc.update(connection, "DELETE FROM collections WHERE user_id = ?", Jdbc.params(userId));
    }

    public void deleteByBook(Connection connection, String bookId) {
        Jdbc.update(connection, "DELETE FROM collections WHERE book_id = ?", Jdbc.params(bookId));
    }

    public long countByUser(String userId) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections WHERE user_id = ?", Jdbc.params(userId)));
    }

    public long countByUserAndStatus(String userId, String status) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections WHERE user_id = ? AND status = ?",
                Jdbc.params(userId, status)));
    }

    public long countByStatus(String status) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections WHERE status = ?", Jdbc.params(status)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM collections", List.of()));
    }

    /** Status dan sumber status dari tabel collections. */
    public record CollectionState(String status, String statusSource) {
        public boolean isUserChoice() {
            return "user".equalsIgnoreCase(statusSource);
        }
    }
}
