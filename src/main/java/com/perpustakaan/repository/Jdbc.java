package com.perpustakaan.repository;

import com.perpustakaan.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper JDBC kecil. Semua SQL tetap berada di repository.
 */
public final class Jdbc {

    private Jdbc() {
    }

    public static List<Object> params(Object... values) {
        return Arrays.asList(values);
    }

    public static <T> List<T> queryList(Connection connection, String sql, List<Object> parameters, RowMapper<T> mapper) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(mapper.map(resultSet));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query database gagal: " + firstLine(sql), e);
        }
    }

    public static <T> T queryOne(Connection connection, String sql, List<Object> parameters, RowMapper<T> mapper) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.map(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query database gagal: " + firstLine(sql), e);
        }
    }

    public static long queryLong(Connection connection, String sql, List<Object> parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query database gagal: " + firstLine(sql), e);
        }
    }

    public static int queryInt(Connection connection, String sql, List<Object> parameters) {
        return (int) queryLong(connection, sql, parameters);
    }

    public static BigDecimal queryBigDecimal(Connection connection, String sql, List<Object> parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getBigDecimal(1) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query database gagal: " + firstLine(sql), e);
        }
    }

    public static int update(Connection connection, String sql, List<Object> parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Operasi database gagal: " + firstLine(sql), e);
        }
    }

    private static void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        if (parameters == null) {
            return;
        }
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            int index = i + 1;
            if (value == null) {
                statement.setNull(index, Types.NULL);
            } else if (value instanceof LocalDate localDate) {
                statement.setDate(index, Date.valueOf(localDate));
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index, decimal);
            } else if (value instanceof Enum<?> enumValue) {
                statement.setString(index, enumValue.toString());
            } else {
                statement.setObject(index, value);
            }
        }
    }

    public static Integer getInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    public static BigDecimal getBigDecimal(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getBigDecimal(column);
    }

    public static LocalDate getLocalDate(ResultSet resultSet, String column) throws SQLException {
        Date date = resultSet.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    public static String getString(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getString(column);
    }

    private static String firstLine(String sql) {
        String compact = sql.replaceAll("\\s+", " ").trim();
        return compact.length() > 160 ? compact.substring(0, 160) + "..." : compact;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }
}
