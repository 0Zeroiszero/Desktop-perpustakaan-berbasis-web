package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.UserRepository;
import com.perpustakaan.security.PasswordHasher;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.ValidationUtil;

import java.util.Locale;

/**
 * Autentikasi berbasis email dan registrasi anggota baru.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TransactionManager transactionManager;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher,
                       TransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.transactionManager = transactionManager;
    }

    public User login(String identifier, String password) {
        String email = ValidationUtil.required(identifier, "email", "Email", 100);
        ValidationUtil.email(email, "email");
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "Password wajib diisi.");
        }
        User user = userRepository.findForAuth(email.trim().toLowerCase(Locale.ROOT));
        if (user == null || !passwordHasher.verify(password, user.getPasswordHash())) {
            throw new AppException("Email atau password salah.");
        }
        if (!user.isActive()) {
            throw new AppException("Akun Anda sedang dinonaktifkan. Silakan hubungi admin perpustakaan.");
        }
        upgradeHashIfNeeded(user, password);
        user.setPasswordHash(null);
        AppLogger.info("Login berhasil: " + user.getEmail());
        return user;
    }

    private void upgradeHashIfNeeded(User user, String rawPassword) {
        String stored = user.getPasswordHash();
        if (!passwordHasher.needsRehash(stored)) {
            return;
        }
        try {
            String newHash = passwordHasher.hash(rawPassword);
            transactionManager.execute(connection -> {
                userRepository.updatePassword(connection, user.getIdUser(), newHash);
                return null;
            });
            AppLogger.info("Hash password pengguna " + user.getEmail() + " berhasil di-upgrade.");
        } catch (RuntimeException e) {
            AppLogger.warn("Gagal meng-upgrade hash password pengguna " + user.getEmail() + ".", e);
        }
    }

    public User register(RegisterRequest request) {
        ValidationUtil.email(request.email(), "email");
        String displayName = ValidationUtil.required(request.displayName(), "displayName", "Nama lengkap", 100);
        ValidationUtil.password(request.password(), "password");
        ValidationUtil.passwordConfirmation(request.password(), request.passwordConfirmation(), "confirmPassword");

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.emailExists(email, null)) {
            throw new ValidationException("email", "Email sudah terdaftar.");
        }

        User user = new User();
        user.setEmail(email);
        user.setNamaLengkap(displayName);
        user.setRole(Role.MEMBER);
        user.setPasswordHash(passwordHasher.hash(request.password()));

        transactionManager.execute(connection -> {
            userRepository.insert(connection, user);
            return user;
        });
        user.setPasswordHash(null);
        AppLogger.info("Registrasi anggota baru: " + user.getEmail());
        return user;
    }

    public record RegisterRequest(String email, String displayName, String password,
                                  String passwordConfirmation) {
        public RegisterRequest(String email, String displayName, String password) {
            this(email, displayName, password, password);
        }
    }
}
