package com.perpustakaan.repository;

import java.sql.Connection;
import java.util.List;

/**
 * Repository tabel relasi ebook_author.
 */
public class EbookAuthorRepository extends RepositorySupport {

    public void replaceForBook(Connection connection, String bookId, List<String> authorIds) {
        Jdbc.update(connection, "DELETE FROM ebook_author WHERE BOOK_ID_BOOK = ?", Jdbc.params(bookId));
        if (authorIds == null || authorIds.isEmpty()) {
            return;
        }
        for (String authorId : authorIds) {
            Jdbc.update(connection,
                    "INSERT INTO ebook_author (BOOK_ID_BOOK, AUTHOR_ID_AUTHOR) VALUES (?, ?)",
                    Jdbc.params(bookId, authorId));
        }
    }

    public void deleteByBook(Connection connection, String bookId) {
        Jdbc.update(connection, "DELETE FROM ebook_author WHERE BOOK_ID_BOOK = ?", Jdbc.params(bookId));
    }

    public long countByAuthor(String authorId) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM ebook_author WHERE AUTHOR_ID_AUTHOR = ?", Jdbc.params(authorId)));
    }

    public List<String> findAuthorIdsByBook(String bookId) {
        return read(connection -> Jdbc.queryList(connection,
                "SELECT AUTHOR_ID_AUTHOR FROM ebook_author WHERE BOOK_ID_BOOK = ?",
                Jdbc.params(bookId), resultSet -> resultSet.getString("AUTHOR_ID_AUTHOR")));
    }
}
