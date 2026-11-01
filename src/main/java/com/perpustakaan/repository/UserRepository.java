package com.perpustakaan.repository;

import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repository tabel users.
 */
public class UserRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "ID_USER, NAMA_DEPAN, NAMA_BELAKANG, JENIS_KELAMIN, EMAIL, USERNAME, "
                    + "NOMOR_PONSEL, ALAMAT, BIO, USER_ROLE";

    public User findById(String idUser) {
        return read(connection -> Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM users WHERE ID_USER = ?",
                Jdbc.params(idUser), resultSet -> map(resultSet, false)));
    }

    public User findForAuth(String identifier) {
        String value = identifier == null ? "" : identifier.trim();
        return read(connection -> {
            User user = Jdbc.queryOne(connection,
                    "SELECT " + BASE_COLUMNS + ", PASSWORD FROM users WHERE LOWER(EMAIL) = LOWER(?) LIMIT 1",
                    Jdbc.params(value), resultSet -> map(resultSet, true));
            if (user == null) {
                user = Jdbc.queryOne(connection,
                        "SELECT " + BASE_COLUMNS + ", PASSWORD FROM users WHERE LOWER(USERNAME) = LOWER(?) LIMIT 1",
                        Jdbc.params(value), resultSet -> map(resultSet, true));
            }
            return user;
        });
    }

    public PageResult<User> findAll(String search, Role roleFilter, int page, int pageSize) {
        return read(connection -> {
            String where = buildWhere(search, roleFilter);
            List<Object> parameters = buildWhereParameters(search);
            long total = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM users" + where, parameters);

            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((page - 1) * pageSize);
            List<User> users = Jdbc.queryList(connection,
                    "SELECT " + BASE_COLUMNS + " FROM users" + where
                            + " ORDER BY NAMA_DEPAN ASC, NAMA_BELAKANG ASC LIMIT ? OFFSET ?",
                    pageParameters, resultSet -> map(resultSet, false));
            return new PageResult<>(users, page, pageSize, total);
        });
    }

    private String buildWhere(String search, Role roleFilter) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank()) {
            where.append(" AND (LOWER(CONCAT_WS(' ', NAMA_DEPAN, NAMA_BELAKANG)) LIKE ?")
                    .append(" OR LOWER(USERNAME) LIKE ? OR LOWER(EMAIL) LIKE ?)");
        }
        if (roleFilter == Role.ADMIN) {
            where.append(" AND LOWER(USER_ROLE) = 'admin'");
        } else if (roleFilter == Role.MEMBER) {
            where.append(" AND LOWER(USER_ROLE) <> 'admin'");
        }
        return where.toString();
    }

    private List<Object> buildWhereParameters(String search) {
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        return parameters;
    }

    public boolean emailExists(String email, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM users WHERE LOWER(EMAIL) = LOWER(?)",
                        Jdbc.params(email)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM users WHERE LOWER(EMAIL) = LOWER(?) AND ID_USER <> ?",
                    Jdbc.params(email, excludeId)) > 0;
        });
    }

    public boolean usernameExists(String username, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM users WHERE LOWER(USERNAME) = LOWER(?)",
                        Jdbc.params(username)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM users WHERE LOWER(USERNAME) = LOWER(?) AND ID_USER <> ?",
                    Jdbc.params(username, excludeId)) > 0;
        });
    }

    public long countAdmins() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM users WHERE LOWER(USER_ROLE) = 'admin'", List.of()));
    }

    public long countMembers() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM users WHERE LOWER(USER_ROLE) <> 'admin'", List.of()));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM users", List.of()));
    }

    public void insert(Connection connection, User user) {
        Jdbc.update(connection,
                "INSERT INTO users (ID_USER, NAMA_DEPAN, NAMA_BELAKANG, JENIS_KELAMIN, EMAIL, USERNAME, "
                        + "PASSWORD, NOMOR_PONSEL, ALAMAT, BIO, USER_ROLE) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Jdbc.params(user.getIdUser(), user.getNamaDepan(), user.getNamaBelakang(), user.getJenisKelamin(),
                        user.getEmail(), user.getUsername(), user.getPasswordHash(), user.getNomorPonsel(),
                        user.getAlamat(), user.getBio(), user.getRole().databaseValue()));
    }

    public void update(Connection connection, User user) {
        Jdbc.update(connection,
                "UPDATE users SET NAMA_DEPAN = ?, NAMA_BELAKANG = ?, JENIS_KELAMIN = ?, EMAIL = ?, "
                        + "USERNAME = ?, NOMOR_PONSEL = ?, ALAMAT = ?, BIO = ?, USER_ROLE = ? WHERE ID_USER = ?",
                Jdbc.params(user.getNamaDepan(), user.getNamaBelakang(), user.getJenisKelamin(), user.getEmail(),
                        user.getUsername(), user.getNomorPonsel(), user.getAlamat(), user.getBio(),
                        user.getRole().databaseValue(), user.getIdUser()));
    }

    public void updatePassword(Connection connection, String idUser, String passwordHash) {
        Jdbc.update(connection, "UPDATE users SET PASSWORD = ? WHERE ID_USER = ?",
                Jdbc.params(passwordHash, idUser));
    }

    public void delete(Connection connection, String idUser) {
        Jdbc.update(connection, "DELETE FROM users WHERE ID_USER = ?", Jdbc.params(idUser));
    }

    private User map(ResultSet resultSet, boolean withPassword) throws SQLException {
        User user = new User();
        user.setIdUser(resultSet.getString("ID_USER"));
        user.setNamaDepan(resultSet.getString("NAMA_DEPAN"));
        user.setNamaBelakang(resultSet.getString("NAMA_BELAKANG"));
        user.setJenisKelamin(resultSet.getString("JENIS_KELAMIN"));
        user.setEmail(resultSet.getString("EMAIL"));
        user.setUsername(resultSet.getString("USERNAME"));
        user.setNomorPonsel(resultSet.getString("NOMOR_PONSEL"));
        user.setAlamat(resultSet.getString("ALAMAT"));
        user.setBio(resultSet.getString("BIO"));
        user.setRole(Role.fromDatabase(resultSet.getString("USER_ROLE")));
        if (withPassword) {
            user.setPasswordHash(resultSet.getString("PASSWORD"));
        }
        return user;
    }
}
