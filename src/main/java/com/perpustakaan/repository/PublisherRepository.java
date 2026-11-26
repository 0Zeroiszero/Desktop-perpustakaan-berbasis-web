package com.perpustakaan.repository;

import com.perpustakaan.model.Publisher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository tabel publishers.
 */
public class PublisherRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "p.publisher_id, p.name, p.slug, p.city, "
                    + "(SELECT COUNT(*) FROM books b WHERE b.publisher_id = p.publisher_id) AS jumlah_buku";

    public List<Publisher> findAll() {
        return read(connection -> Jdbc.queryList(connection,
                "SELECT " + BASE_COLUMNS + " FROM publishers p ORDER BY p.name ASC, p.publisher_id ASC",
                List.of(), this::map));
    }

    public Publisher findById(String idPublisher) {
        return read(connection -> findById(connection, idPublisher));
    }

    public Publisher findById(Connection connection, String idPublisher) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM publishers p WHERE p.publisher_id = ?",
                Jdbc.params(idPublisher), this::map);
    }

    public Publisher findByName(Connection connection, String name) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM publishers p WHERE LOWER(p.name) = LOWER(?) LIMIT 1",
                Jdbc.params(name), this::map);
    }

    public boolean nameExists(String name, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM publishers WHERE LOWER(name) = LOWER(?)",
                        Jdbc.params(name)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM publishers WHERE LOWER(name) = LOWER(?) AND publisher_id <> ?",
                    Jdbc.params(name, excludeId)) > 0;
        });
    }

    public boolean slugExists(Connection connection, String slug, String excludeId) {
        if (excludeId == null) {
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM publishers WHERE slug = ?", Jdbc.params(slug)) > 0;
        }
        return Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM publishers WHERE slug = ? AND publisher_id <> ?",
                Jdbc.params(slug, excludeId)) > 0;
    }

    public long countBooks(String idPublisher) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM books WHERE publisher_id = ?", Jdbc.params(idPublisher)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM publishers", List.of()));
    }

    public String insert(Connection connection, Publisher publisher) {
        long id = Jdbc.insertAndGetId(connection,
                "INSERT INTO publishers (name, slug, city) VALUES (?, ?, ?)",
                Jdbc.params(publisher.getNama(), publisher.getSlug(), publisher.getKota()));
        publisher.setIdPublisher(String.valueOf(id));
        return publisher.getIdPublisher();
    }

    public void update(Connection connection, Publisher publisher) {
        Jdbc.update(connection,
                "UPDATE publishers SET name = ?, slug = ?, city = ? WHERE publisher_id = ?",
                Jdbc.params(publisher.getNama(), publisher.getSlug(), publisher.getKota(),
                        publisher.getIdPublisher()));
    }

    private Publisher map(ResultSet resultSet) throws SQLException {
        return new Publisher(
                resultSet.getString("publisher_id"),
                resultSet.getString("name"),
                resultSet.getString("slug"),
                resultSet.getString("city"),
                resultSet.getLong("jumlah_buku"));
    }
}
