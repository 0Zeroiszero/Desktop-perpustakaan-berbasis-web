package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.IdSequenceRepository;
import com.perpustakaan.repository.ReadingProgressRepository;
import com.perpustakaan.repository.UserRepository;
import com.perpustakaan.security.AccessControl;
import com.perpustakaan.security.PasswordHasher;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.ValidationUtil;

import java.util.Locale;

/**
 * Operasi admin untuk mengelola pengguna.
 */
public class UserService {

    private final UserRepository userRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final TransactionManager transactionManager;
    private final PasswordHasher passwordHasher;
    private final IdSequenceRepository idSequenceRepository;

    public UserService(UserRepository userRepository, ReadingProgressRepository readingProgressRepository,
                       TransactionManager transactionManager, PasswordHasher passwordHasher,
                       IdSequenceRepository idSequenceRepository) {
        this.userRepository = userRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.transactionManager = transactionManager;
        this.passwordHasher = passwordHasher;
        this.idSequenceRepository = idSequenceRepository;
    }

    public PageResult<User> list(User actor, String search, Role roleFilter, int page, int pageSize) {
        AccessControl.requireAdmin(actor);
        return userRepository.findAll(search, roleFilter, page, pageSize);
    }

    public User get(User actor, String idUser) {
        AccessControl.requireAdmin(actor);
        User user = userRepository.findById(idUser);
        if (user == null) {
            throw new AppException("Pengguna tidak ditemukan.");
        }
        return user;
    }

    public User create(User actor, UserFormData data) {
        AccessControl.requireAdmin(actor);
        ValidatedData validated = validate(data, false);
        ensureUnique(validated, null);

        User user = new User();
        user.setNamaDepan(validated.namaDepan());
        user.setNamaBelakang(validated.namaBelakang());
        user.setJenisKelamin(validated.jenisKelamin());
        user.setEmail(validated.email());
        user.setUsername(validated.username());
        user.setNomorPonsel(validated.nomorPonsel());
        user.setAlamat(validated.alamat());
        user.setBio(validated.bio());
        user.setRole(validated.role());
        user.setPasswordHash(passwordHasher.hash(validated.password()));

        transactionManager.execute(connection -> {
            user.setIdUser(idSequenceRepository.nextId(connection, IdSequenceRepository.EntityType.USER));
            userRepository.insert(connection, user);
            return user;
        });
        user.setPasswordHash(null);
        AppLogger.info("Admin " + actor.getUsername() + " membuat pengguna " + user.getUsername());
        return user;
    }

    public User update(User actor, String idUser, UserFormData data) {
        AccessControl.requireAdmin(actor);
        User existing = userRepository.findById(idUser);
        if (existing == null) {
            throw new AppException("Pengguna tidak ditemukan.");
        }
        ValidatedData validated = validate(data, true);
        ensureUnique(validated, idUser);

        if (existing.isAdmin() && validated.role() == Role.MEMBER) {
            if (existing.getIdUser().equals(actor.getIdUser())) {
                throw new ValidationException("role", "Anda tidak dapat menurunkan role akun sendiri.");
            }
            if (userRepository.countAdmins() <= 1) {
                throw new ValidationException("role", "Minimal harus ada satu akun admin.");
            }
        }

        existing.setNamaDepan(validated.namaDepan());
        existing.setNamaBelakang(validated.namaBelakang());
        existing.setJenisKelamin(validated.jenisKelamin());
        existing.setEmail(validated.email());
        existing.setUsername(validated.username());
        existing.setNomorPonsel(validated.nomorPonsel());
        existing.setAlamat(validated.alamat());
        existing.setBio(validated.bio());
        existing.setRole(validated.role());

        transactionManager.execute(connection -> {
            userRepository.update(connection, existing);
            if (validated.password() != null) {
                userRepository.updatePassword(connection, idUser, passwordHasher.hash(validated.password()));
            }
            return null;
        });
        existing.setPasswordHash(null);
        AppLogger.info("Admin " + actor.getUsername() + " memperbarui pengguna " + existing.getUsername());
        return existing;
    }

    public void delete(User actor, String idUser) {
        AccessControl.requireAdmin(actor);
        if (actor.getIdUser().equals(idUser)) {
            throw new ValidationException("id", "Anda tidak dapat menghapus akun sendiri.");
        }
        User existing = userRepository.findById(idUser);
        if (existing == null) {
            throw new AppException("Pengguna tidak ditemukan.");
        }
        if (existing.isAdmin() && userRepository.countAdmins() <= 1) {
            throw new ValidationException("id", "Akun admin terakhir tidak dapat dihapus.");
        }
        transactionManager.execute(connection -> {
            readingProgressRepository.deleteByUser(connection, idUser);
            userRepository.delete(connection, idUser);
            return null;
        });
        AppLogger.info("Admin " + actor.getUsername() + " menghapus pengguna " + existing.getUsername());
    }

    private void ensureUnique(ValidatedData data, String excludeId) {
        if (userRepository.emailExists(data.email(), excludeId)) {
            throw new ValidationException("email", "Email sudah terdaftar.");
        }
        if (userRepository.usernameExists(data.username(), excludeId)) {
            throw new ValidationException("username", "Username sudah digunakan.");
        }
    }

    private ValidatedData validate(UserFormData data, boolean edit) {
        String namaDepan = ValidationUtil.required(data.namaDepan(), "namaDepan", "Nama depan", 50);
        String namaBelakang = ValidationUtil.optional(data.namaBelakang(), "namaBelakang", "Nama belakang", 50);
        ValidationUtil.username(data.username(), "username");
        ValidationUtil.email(data.email(), "email");
        String phone = ValidationUtil.phone(data.nomorPonsel(), "nomorPonsel");
        String alamat = ValidationUtil.optional(data.alamat(), "alamat", "Alamat", 255);
        String bio = ValidationUtil.optional(data.bio(), "bio", "Bio", 500);
        String gender = data.jenisKelamin();
        if (gender != null && !gender.isBlank() && !gender.equals("L") && !gender.equals("P")) {
            throw new ValidationException("jenisKelamin", "Jenis kelamin tidak valid.");
        }
        if (data.role() == null) {
            throw new ValidationException("role", "Role wajib dipilih.");
        }

        String password = data.password();
        if (!edit) {
            ValidationUtil.password(password, "password");
            ValidationUtil.passwordConfirmation(password, data.passwordConfirmation(), "confirmPassword");
        } else if (password != null && !password.isBlank()) {
            ValidationUtil.password(password, "password");
            ValidationUtil.passwordConfirmation(password, data.passwordConfirmation(), "confirmPassword");
        } else {
            password = null;
        }

        return new ValidatedData(
                namaDepan,
                namaBelakang,
                data.username().trim().toLowerCase(Locale.ROOT),
                data.email().trim().toLowerCase(Locale.ROOT),
                password,
                data.role(),
                gender == null || gender.isBlank() ? null : gender,
                phone,
                alamat,
                bio);
    }

    public record UserFormData(
            String namaDepan,
            String namaBelakang,
            String username,
            String email,
            String password,
            String passwordConfirmation,
            Role role,
            String jenisKelamin,
            String nomorPonsel,
            String alamat,
            String bio
    ) {
    }

    private record ValidatedData(
            String namaDepan,
            String namaBelakang,
            String username,
            String email,
            String password,
            Role role,
            String jenisKelamin,
            String nomorPonsel,
            String alamat,
            String bio
    ) {
    }
}
