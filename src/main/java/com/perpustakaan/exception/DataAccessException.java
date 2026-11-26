package com.perpustakaan.exception;

/**
 * Membungkus SQLException agar lapisan atas tidak bergantung pada detail JDBC.
 */
public class DataAccessException extends AppException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
