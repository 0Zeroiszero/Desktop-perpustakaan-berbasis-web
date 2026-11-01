package com.perpustakaan.exception;

/**
 * Exception dasar untuk seluruh error aplikasi yang dapat ditampilkan ke pengguna.
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
