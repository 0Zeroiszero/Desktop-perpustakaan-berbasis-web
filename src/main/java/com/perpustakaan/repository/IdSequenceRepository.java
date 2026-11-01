package com.perpustakaan.repository;

import com.perpustakaan.exception.AppException;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;

/**
 * Membuat ID berurutan berdasarkan prefix yang sudah dipakai database.
 * Nama tabel/kolom berasal dari enum internal, bukan input pengguna.
 */
public class IdSequenceRepository extends RepositorySupport {

    public enum EntityType {
        USER("users", "ID_USER", "USR", 3),
        BOOK("ebook", "ID_BOOK", "BK", 3),
        CATEGORY("kategori", "ID_KATEGORI", "KAT", 2),
        AUTHOR("author", "ID_AUTHOR", "AUT", 2),
        PROGRESS("reading_progress", "ID_PROGRESS", "PRG", 3);

        private final String table;
        private final String column;
        private final String prefix;
        private final int width;

        EntityType(String table, String column, String prefix, int width) {
            this.table = table;
            this.column = column;
            this.prefix = prefix;
            this.width = width;
        }
    }

    public String nextId(Connection connection, EntityType type) {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(" + type.column + ", "
                + (type.prefix.length() + 1) + ") AS UNSIGNED)), 0) + 1 FROM " + type.table;
        long next = Jdbc.queryLong(connection, sql, List.of());
        String id = type.prefix + String.format(Locale.ROOT, "%0" + type.width + "d", next);
        if (id.length() > 10) {
            throw new AppException("ID " + type.name() + " sudah mencapai batas maksimum kolom (10 karakter).");
        }
        return id;
    }
}
