package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.IdSequenceRepository;
import com.perpustakaan.repository.UserRepository;
import com.perpustakaan.security.PasswordHasher;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.ValidationUtil;

import java.util.Locale;

/**
 * Autentikasi, registrasi, dan upgrade hash password warisan.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TransactionManager transactionManager;
    private final IdSequenceRepository idSequenceRepository;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher,
                       TransactionManager transactionManager, IdSequenceRepository idSequenceRepository) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.transactionManager = transactionManager;
        this.idSequenceRepository = idSequenceRepository;
    }

    public User login(String identifier, String password) {
        String loginValue = ValidationUtil.required(identifier, "identifier", "Email atau username", 100);
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "Password wajib diisi.");
        }
        User user = userRepository.findForAuth(loginValue);
        if (user == null || !passwordHasher.verify(password, user.getPasswordHash())) {
            throw new AppException("Email/username atau password salah.");
        }
        upgradeHashIfNeeded(user, password);
        user.setPasswordHash(null);
        AppLogger.info("Login berhasil: " + user.getUsername());
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
            AppLogger.info("Hash password pengguna " + user.getUsername() + " berhasil di-upgrade.");
        } catch (RuntimeException e) {
            AppLogger.warn("Gagal meng-upgrade hash password pengguna " + user.getUsername() + ".", e);
        }
    }

    public User register(RegisterRequest request) {
        String namaDepan = ValidationUtil.required(request.namaDepan(), "namaDepan", "Nama depan", 50);
        String namaBelakang = ValidationUtil.optional(request.namaBelakang(), "namaBelakang", "Nama belakang", 50);
        ValidationUtil.username(request.username(), "username");
        ValidationUtil.email(request.email(), "email");
        ValidationUtil.password(request.password(), "password");
        ValidationUtil.passwordConfirmation(request.password(), request.passwordConfirmation(), "confirmPassword");
        String phone = ValidationUtil.phone(request.nomorPonsel(), "nomorPonsel");
        String alamat = ValidationUtil.optional(request.alamat(), "alamat", "Alamat", 255);
        String bio = ValidationUtil.optional(request.bio(), "bio", "Bio", 500);
        String gender = validateGender(request.jenisKelamin());

        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.emailExists(email, null)) {
            throw new ValidationException("email", "Email sudah terdaftar.");
        }
        if (userRepository.usernameExists(username, null)) {
            throw new ValidationException("username", "Username sudah digunakan.");
        }

        User user = new User();
        user.setNamaDepan(namaDepan);
        user.setNamaBelakang(namaBelakang);
        user.setJenisKelamin(gender);
        user.setEmail(email);
        user.setUsername(username);
        user.setNomorPonsel(phone);
        user.setAlamat(alamat);
        user.setBio(bio);
        user.setRole(Role.MEMBER);
        user.setPasswordHash(passwordHasher.hash(request.password()));

        transactionManager.execute(connection -> {
            user.setIdUser(idSequenceRepository.nextId(connection, IdSequenceRepository.EntityType.USER));
            userRepository.insert(connection, user);
            return user;
        });
        user.setPasswordHash(null);
        AppLogger.info("Registrasi anggota baru: " + user.getUsername());
        return user;
    }

    private String validateGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        if (!gender.equals("L") && !gender.equals("P")) {
            throw new ValidationException("jenisKelamin", "Jenis kelamin tidak valid.");
        }
        return gender;
    }

    public record RegisterRequest(
            String namaDepan,
            String namaBelakang,
            String username,
            String email,
            String password,
            String passwordConfirmation,
            String jenisKelamin,
            String nomorPonsel,
            String alamat,
            String bio
    ) {
    }
}
