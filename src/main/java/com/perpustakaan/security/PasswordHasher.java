package com.perpustakaan.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.perpustakaan.util.AppLogger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Hash dan verifikasi password BCrypt. Mendukung hash lama $2a/$2y dan
 * password plaintext warisan website untuk proses upgrade otomatis.
 */
public final class PasswordHasher {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final int cost;

    public PasswordHasher(int cost) {
        this.cost = cost;
    }

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong.");
        }
        return BCrypt.withDefaults().hashToString(cost, rawPassword.toCharArray());
    }

    public boolean verify(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            try {
                return BCrypt.verifyer().verify(rawPassword.toCharArray(), storedPassword).verified;
            } catch (RuntimeException e) {
                AppLogger.warn("Hash password tidak dapat diverifikasi.", e);
                return false;
            }
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isBcrypt(String storedPassword) {
        return storedPassword != null && BCRYPT_PATTERN.matcher(storedPassword).matches();
    }

    public boolean needsRehash(String storedPassword) {
        if (!isBcrypt(storedPassword)) {
            return true;
        }
        try {
            String[] parts = storedPassword.split("\\$");
            int storedCost = Integer.parseInt(parts[2]);
            return storedCost != cost;
        } catch (RuntimeException e) {
            return true;
        }
    }
}
