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
 * Repository tabel kategori.
 */
public class KategoriRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "k.ID_KATEGORI, k.NAMA_KATEGORI, k.DESKRIPSI, "
                    + "(SELECT COUNT(*) FROM ebook e WHERE e.KATEGORI_ID_KATEGORI = k.ID_KATEGORI) AS JUMLAH_BUKU";

    public PageResult<Kategori> findAll(String search, int page, int pageSize) {
        return read(connection -> {
            String where = "";
            List<Object> parameters = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                where = " WHERE (LOWER(k.NAMA_KATEGORI) LIKE ? OR LOWER(COALESCE(k.DESKRIPSI, '')) LIKE ?)";
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                parameters.add(pattern);
                parameters.add(pattern);
            }
            long total = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM kategori k" + where, parameters);
            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((page - 1) * pageSize);
            List<Kategori> items = Jdbc.queryList(connection,
                    "SELECT " + BASE_COLUMNS + " FROM kategori k" + where
                            + " ORDER BY k.NAMA_KATEGORI ASC LIMIT ? OFFSET ?",
                    pageParameters, this::map);
            return new PageResult<>(items, page, pageSize, total);
        });
    }

    public List<Kategori> findAll() {
        return read(connection -> Jdbc.queryList(connection,
                "SELECT " + BASE_COLUMNS + " FROM kategori k ORDER BY k.NAMA_KATEGORI ASC",
                List.of(), this::map));
    }

    public Kategori findById(String idKategori) {
        return read(connection -> findById(connection, idKategori));
    }

    public Kategori findById(Connection connection, String idKategori) {
        return Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM kategori k WHERE k.ID_KATEGORI = ?",
                Jdbc.params(idKategori), this::map);
    }

    public boolean nameExists(String name, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM kategori WHERE LOWER(NAMA_KATEGORI) = LOWER(?)",
                        Jdbc.params(name)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM kategori WHERE LOWER(NAMA_KATEGORI) = LOWER(?) AND ID_KATEGORI <> ?",
                    Jdbc.params(name, excludeId)) > 0;
        });
    }

    public long countBooks(String idKategori) {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM ebook WHERE KATEGORI_ID_KATEGORI = ?",
                Jdbc.params(idKategori)));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM kategori", List.of()));
    }

    public void insert(Connection connection, Kategori kategori) {
        Jdbc.update(connection,
                "INSERT INTO kategori (ID_KATEGORI, NAMA_KATEGORI, DESKRIPSI) VALUES (?, ?, ?)",
                Jdbc.params(kategori.getIdKategori(), kategori.getNamaKategori(), kategori.getDeskripsi()));
    }

    public void update(Connection connection, Kategori kategori) {
        Jdbc.update(connection,
                "UPDATE kategori SET NAMA_KATEGORI = ?, DESKRIPSI = ? WHERE ID_KATEGORI = ?",
                Jdbc.params(kategori.getNamaKategori(), kategori.getDeskripsi(), kategori.getIdKategori()));
    }

    public void delete(Connection connection, String idKategori) {
        Jdbc.update(connection, "DELETE FROM kategori WHERE ID_KATEGORI = ?", Jdbc.params(idKategori));
    }

    private Kategori map(ResultSet resultSet) throws SQLException {
        return new Kategori(
                resultSet.getString("ID_KATEGORI"),
                resultSet.getString("NAMA_KATEGORI"),
                resultSet.getString("DESKRIPSI"),
                resultSet.getLong("JUMLAH_BUKU"));
    }
}
