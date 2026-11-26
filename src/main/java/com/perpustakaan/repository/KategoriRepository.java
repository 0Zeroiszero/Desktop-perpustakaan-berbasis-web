package com.perpustakaan.repository;

import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repository tabel genres (kategori buku).
 */
public class KategoriRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "g.genre_id, g.name, g.description, "
                    + "(SELECT COUNT(*) FROM books b WHERE b.genre_id = g.genre_id) AS jumlah_buku";

    public PageResult<Kategori> findAll(String search, int page, int pageSize) {
        return read(connection -> {
            List<Object> parameters = new ArrayList<>();
            String where = buildWhere(search, parameters);
            long total = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM genres g" + where, parameters);

            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((long) (page - 1) * pageSize);
            List<Kategori> items = Jdbc.queryList(connection,
                    "SELECT " + BASE_COLUMNS + " FROM genres g" + where
                            + " ORDER BY g.name ASC LIMIT ? OFFSET ?",
                    pageParameters, this::map);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    public List<Kategori> findAll() {
        return read(connection -> Jdbc.queryList(connection,
                "SELECT " + BASE_COLUMNS + " FROM genres g ORDER BY g.name ASC",
                List.of(), this::map));
    }

    public Kategori findById(String idKategori) {
        return read(connection -> findById(connection, idKategori));
    }

    public Kategori findById(Connection connection, String idKategori) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM genres g WHERE g.genre_id = ?",
                Jdbc.params(idKategori), this::map);
    }

    public Kategori findByName(Connection connection, String name) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM genres g WHERE LOWER(g.name) = LOWER(?) LIMIT 1",
                Jdbc.params(name), this::map);
    }

    public boolean nameExists(String name, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM genres WHERE LOWER(name) = LOWER(?)",
                        Jdbc.params(name)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM genres WHERE LOWER(name) = LOWER(?) AND genre_id <> ?",
                    Jdbc.params(name, excludeId)) > 0;
        });
    }

    public long countBooks(String idKategori) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM books WHERE genre_id = ?", Jdbc.params(idKategori)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM genres", List.of()));
    }

    public String insert(Connection connection, Kategori kategori) {
        long id = Jdbc.insertAndGetId(connection,
                "INSERT INTO genres (name, description) VALUES (?, ?)",
                Jdbc.params(kategori.getNamaKategori(), kategori.getDeskripsi()));
        kategori.setIdKategori(String.valueOf(id));
        return kategori.getIdKategori();
    }

    public void update(Connection connection, Kategori kategori) {
        Jdbc.update(connection,
                "UPDATE genres SET name = ?, description = ? WHERE genre_id = ?",
                Jdbc.params(kategori.getNamaKategori(), kategori.getDeskripsi(), kategori.getIdKategori()));
    }

    private String buildWhere(String search, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(g.name) LIKE ? OR LOWER(COALESCE(g.description, '')) LIKE ?)");
            parameters.add(pattern);
            parameters.add(pattern);
        }
        return where.toString();
    }

    private Kategori map(ResultSet resultSet) throws SQLException {
        return new Kategori(
                resultSet.getString("genre_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getLong("jumlah_buku"));
    }
}
