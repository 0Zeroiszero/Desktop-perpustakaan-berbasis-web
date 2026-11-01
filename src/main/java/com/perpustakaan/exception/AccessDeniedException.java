package com.perpustakaan.exception;

/**
 * Dilempar ketika pengguna tidak memiliki hak akses terhadap operasi tertentu.
 */
public class AccessDeniedException extends AppException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
