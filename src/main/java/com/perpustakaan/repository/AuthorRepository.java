package com.perpustakaan.repository;

import com.perpustakaan.model.Author;
import com.perpustakaan.model.PageResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repository tabel author.
 */
public class AuthorRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "a.ID_AUTHOR, a.NAMA_DEPAN, a.NAMA_BELAKANG, a.EMAIL, a.NEGARA_ASAL, a.BIO, "
                    + "(SELECT COUNT(*) FROM ebook_author ea WHERE ea.AUTHOR_ID_AUTHOR = a.ID_AUTHOR) AS JUMLAH_BUKU";

    public PageResult<Author> findAll(String search, int page, int pageSize) {
        return read(connection -> {
            String where = "";
            List<Object> parameters = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                where = " WHERE (LOWER(CONCAT_WS(' ', a.NAMA_DEPAN, a.NAMA_BELAKANG)) LIKE ?"
                        + " OR LOWER(COALESCE(a.EMAIL, '')) LIKE ? OR LOWER(COALESCE(a.NEGARA_ASAL, '')) LIKE ?)";
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                parameters.add(pattern);
                parameters.add(pattern);
                parameters.add(pattern);
            }
            long total = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM author a" + where, parameters);
            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((page - 1) * pageSize);
            List<Author> items = Jdbc.queryList(connection,
                    "SELECT " + BASE_COLUMNS + " FROM author a" + where
                            + " ORDER BY a.NAMA_DEPAN ASC, a.NAMA_BELAKANG ASC LIMIT ? OFFSET ?",
                    pageParameters, this::map);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    public List<Author> findAll() {
        return read(connection -> Jdbc.queryList(connection,
                "SELECT " + BASE_COLUMNS + " FROM author a ORDER BY a.NAMA_DEPAN ASC, a.NAMA_BELAKANG ASC",
                List.of(), this::map));
    }

    public Author findById(String idAuthor) {
        return read(connection -> findById(connection, idAuthor));
    }

    public Author findById(Connection connection, String idAuthor) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM author a WHERE a.ID_AUTHOR = ?",
                Jdbc.params(idAuthor), this::map);
    }

    public boolean emailExists(String email, String excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM author WHERE LOWER(EMAIL) = LOWER(?)",
                        Jdbc.params(email)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM author WHERE LOWER(EMAIL) = LOWER(?) AND ID_AUTHOR <> ?",
                    Jdbc.params(email, excludeId)) > 0;
        });
    }

    public long countBooks(String idAuthor) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM ebook_author WHERE AUTHOR_ID_AUTHOR = ?",
                Jdbc.params(idAuthor)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM author", List.of()));
    }

    public void insert(Connection connection, Author author) {
        Jdbc.update(connection,
                "INSERT INTO author (ID_AUTHOR, NAMA_DEPAN, NAMA_BELAKANG, EMAIL, NEGARA_ASAL, BIO) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                Jdbc.params(author.getIdAuthor(), author.getNamaDepan(), author.getNamaBelakang(),
                        author.getEmail(), author.getNegaraAsal(), author.getBio()));
    }

    public void update(Connection connection, Author author) {
        Jdbc.update(connection,
                "UPDATE author SET NAMA_DEPAN = ?, NAMA_BELAKANG = ?, EMAIL = ?, NEGARA_ASAL = ?, BIO = ? "
                        + "WHERE ID_AUTHOR = ?",
                Jdbc.params(author.getNamaDepan(), author.getNamaBelakang(), author.getEmail(),
                        author.getNegaraAsal(), author.getBio(), author.getIdAuthor()));
    }

    public void delete(Connection connection, String idAuthor) {
        Jdbc.update(connection, "DELETE FROM author WHERE ID_AUTHOR = ?", Jdbc.params(idAuthor));
    }

    private Author map(ResultSet resultSet) throws SQLException {
        return new Author(
                resultSet.getString("ID_AUTHOR"),
                resultSet.getString("NAMA_DEPAN"),
                resultSet.getString("NAMA_BELAKANG"),
                resultSet.getString("EMAIL"),
                resultSet.getString("NEGARA_ASAL"),
                resultSet.getString("BIO"),
                resultSet.getLong("JUMLAH_BUKU"));
    }
}
