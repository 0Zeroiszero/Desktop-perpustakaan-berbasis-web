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
 * Repository tabel users (identitas login = email).
 */
public class UserRepository extends RepositorySupport {

    private static final String BASE_COLUMNS =
            "user_id, email, password_hash, display_name, role, is_active";

    public User findById(String idUser) {
        return read(connection -> Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM users WHERE user_id = ?",
                Jdbc.params(idUser), resultSet -> map(resultSet, false)));
    }

    public User findForAuth(String email) {
        String value = email == null ? "" : email.trim();
        return read(connection -> Jdbc.queryOne(connection,
                "SELECT " + BASE_COLUMNS + " FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1",
                Jdbc.params(value), resultSet -> map(resultSet, true)));
    }

    public PageResult<User> findAll(String search, int page, int pageSize) {
        return read(connection -> {
            List<Object> parameters = new ArrayList<>();
            String where = buildWhere(search, parameters);
            long total = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM users" + where, parameters);

            List<Object> pageParameters = new ArrayList<>(parameters);
            pageParameters.add(pageSize);
            pageParameters.add((long) (page - 1) * pageSize);
            List<User> users = Jdbc.queryList(connection,
                    "SELECT " + BASE_COLUMNS + " FROM users" + where
                            + " ORDER BY display_name ASC, user_id ASC LIMIT ? OFFSET ?",
                    pageParameters, resultSet -> map(resultSet, false));
            return new PageResult<>(users, page, pageSize, total);
        });
    }

    private String buildWhere(String search, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(email) LIKE ? OR LOWER(display_name) LIKE ?)");
            parameters.add(pattern);
            parameters.add(pattern);
        }
        return where.toString();
    }

    public boolean emailExists(String email, String excludeId) {
        return read(connection -> {
            if (excludeId == null) {
                return Jdbc.queryLong(connection,
                        "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)",
                        Jdbc.params(email)) > 0;
            }
            return Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND user_id <> ?",
                    Jdbc.params(email, excludeId)) > 0;
        });
    }

    public long countAdmins() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM users WHERE role = 'admin'", List.of()));
    }

    public long countMembers() {
        return read(connection -> Jdbc.queryLong(connection,
                "SELECT COUNT(*) FROM users WHERE role = 'member'", List.of()));
    }

    public long countAll() {
        return read(connection -> Jdbc.queryLong(connection, "SELECT COUNT(*) FROM users", List.of()));
    }

    /** Menyimpan user baru dan mengisi id auto-increment ke objek. */
    public String insert(Connection connection, User user) {
        long id = Jdbc.insertAndGetId(connection,
                "INSERT INTO users (email, password_hash, display_name, role, is_active) "
                        + "VALUES (?, ?, ?, ?, ?)",
                Jdbc.params(user.getEmail(), user.getPasswordHash(), user.getNamaLengkap(),
                        user.getRole().databaseValue(), user.isActive()));
        user.setIdUser(String.valueOf(id));
        return user.getIdUser();
    }

    public void update(Connection connection, User user) {
        Jdbc.update(connection,
                "UPDATE users SET email = ?, display_name = ?, role = ?, is_active = ? WHERE user_id = ?",
                Jdbc.params(user.getEmail(), user.getNamaLengkap(), user.getRole().databaseValue(),
                        user.isActive(), user.getIdUser()));
    }

    public void updatePassword(Connection connection, String idUser, String passwordHash) {
        Jdbc.update(connection, "UPDATE users SET password_hash = ? WHERE user_id = ?",
                Jdbc.params(passwordHash, idUser));
    }

    public void setActive(String idUser, boolean active) {
        write(connection -> Jdbc.update(connection,
                "UPDATE users SET is_active = ? WHERE user_id = ?", Jdbc.params(active, idUser)));
    }

    private User map(ResultSet resultSet, boolean withPassword) throws SQLException {
        User user = new User();
        user.setIdUser(resultSet.getString("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setNamaLengkap(resultSet.getString("display_name"));
        user.setRole(Role.fromDatabase(resultSet.getString("role")));
        user.setActive(resultSet.getBoolean("is_active"));
        if (withPassword) {
            user.setPasswordHash(resultSet.getString("password_hash"));
        }
        return user;
    }
}
