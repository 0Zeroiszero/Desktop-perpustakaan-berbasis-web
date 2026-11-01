package com.pbo.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Koneksi ke MySQL Laragon. Bisa ditimpa lewat -Ddb.url / -Ddb.user / -Ddb.password. */
public final class Database {

    private static final String URL = System.getProperty(
            "db.url", "jdbc:mysql://localhost:3306/db_pustaka?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&characterEncoding=UTF-8&serverTimezone=Asia/Jakarta&connectTimeout=3000");
    private static final String USER = System.getProperty("db.user", "root");
    private static final String PASSWORD = System.getProperty("db.password", "");

    private Database() {
    }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
