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
 * Repository tabel authors.
 */
public class AuthorRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "a.author_id, a.name, a.slug, a.country, a.bio, "
                    + "(SELECT COUNT(*) FROM books b WHERE b.author_id = a.author_id) AS jumlah_buku";

    public PageResult<Author> findAll(String search, int page, int pageSize) {
        return read(connection -> {
            List<Object> parameters = new ArrayList<>();
            String where = buildWhere(search, parameters);
            long total = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM authors a" + where, parameters);

            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((long) (page - 1) * pageSize);
            List<Author> items = Jdbc.queryList(connection,
                    "SELECT " + BASE_COLUMNS + " FROM authors a" + where
                            + " ORDER BY a.name ASC, a.author_id ASC LIMIT ? OFFSET ?",
                    pageParameters, this::map);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    public List<Author> findAll() {
        return read(connection -> Jdbc.queryList(connection,
                "SELECT " + BASE_COLUMNS + " FROM authors a ORDER BY a.name ASC, a.author_id ASC",
                List.of(), this::map));
    }

    public Author findById(String idAuthor) {
        return read(connection -> findById(connection, idAuthor));
    }

    public Author findById(Connection connection, String idAuthor) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM authors a WHERE a.author_id = ?",
                Jdbc.params(idAuthor), this::map);
    }

    /** Pencarian nama tanpa memperhatikan huruf besar/kecil untuk ensureAuthor. */
    public Author findByName(Connection connection, String name) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM authors a WHERE LOWER(a.name) = LOWER(?) LIMIT 1",
                Jdbc.params(name), this::map);
    }

    public boolean nameExists(String name, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM authors WHERE LOWER(name) = LOWER(?)",
                        Jdbc.params(name)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM authors WHERE LOWER(name) = LOWER(?) AND author_id <> ?",
                    Jdbc.params(name, excludeId)) > 0;
        });
    }

    public boolean slugExists(Connection connection, String slug, String excludeId) {
        if (excludeId == null) {
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM authors WHERE slug = ?", Jdbc.params(slug)) > 0;
        }
        return Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM authors WHERE slug = ? AND author_id <> ?",
                Jdbc.params(slug, excludeId)) > 0;
    }

    public long countBooks(String idAuthor) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM books WHERE author_id = ?", Jdbc.params(idAuthor)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM authors", List.of()));
    }

    public String insert(Connection connection, Author author) {
        long id = Jdbc.insertAndGetId(connection,
                "INSERT INTO authors (name, slug, country, bio) VALUES (?, ?, ?, ?)",
                Jdbc.params(author.getNamaLengkap(), author.getSlug(), author.getNegaraAsal(), author.getBio()));
        author.setIdAuthor(String.valueOf(id));
        return author.getIdAuthor();
    }

    public void update(Connection connection, Author author) {
        Jdbc.update(connection,
                "UPDATE authors SET name = ?, slug = ?, country = ?, bio = ? WHERE author_id = ?",
                Jdbc.params(author.getNamaLengkap(), author.getSlug(), author.getNegaraAsal(),
                        author.getBio(), author.getIdAuthor()));
    }

    private String buildWhere(String search, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(a.name) LIKE ? OR LOWER(COALESCE(a.country, '')) LIKE ?)");
            parameters.add(pattern);
            parameters.add(pattern);
        }
        return where.toString();
    }

    private Author map(ResultSet resultSet) throws SQLException {
        return new Author(
                resultSet.getString("author_id"),
                resultSet.getString("name"),
                resultSet.getString("slug"),
                resultSet.getString("country"),
                resultSet.getString("bio"),
                resultSet.getLong("jumlah_buku"));
    }
}
