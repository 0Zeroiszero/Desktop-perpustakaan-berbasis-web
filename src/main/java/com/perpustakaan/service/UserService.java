package com.perpustakaan.service;

import com.perpustakaan.config.TransactionManager;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
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
    private final TransactionManager transactionManager;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, TransactionManager transactionManager,
                       PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.transactionManager = transactionManager;
        this.passwordHasher = passwordHasher;
    }

    public PageResult<User> list(User actor, String search, int page, int pageSize) {
        AccessControl.requireAdmin(actor);
        return userRepository.findAll(search, page, pageSize);
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
        ensureUnique(validated.email(), null);

        User user = new User();
        user.setEmail(validated.email());
        user.setNamaLengkap(validated.displayName());
        user.setRole(validated.role());
        user.setPasswordHash(passwordHasher.hash(validated.password()));

        transactionManager.execute(connection -> {
            userRepository.insert(connection, user);
            return user;
        });
        user.setPasswordHash(null);
        AppLogger.info("Admin " + actor.getEmail() + " membuat pengguna " + user.getEmail());
        return user;
    }

    public User update(User actor, String idUser, UserFormData data) {
        AccessControl.requireAdmin(actor);
        User existing = userRepository.findById(idUser);
        if (existing == null) {
            throw new AppException("Pengguna tidak ditemukan.");
        }
        ValidatedData validated = validate(data, true);
        ensureUnique(validated.email(), idUser);

        if (existing.isAdmin() && validated.role() == Role.MEMBER) {
            if (existing.getIdUser().equals(actor.getIdUser())) {
                throw new ValidationException("role", "Anda tidak dapat menurunkan role akun sendiri.");
            }
            if (userRepository.countAdmins() <= 1) {
                throw new ValidationException("role", "Minimal harus ada satu akun admin.");
            }
        }

        existing.setEmail(validated.email());
        existing.setNamaLengkap(validated.displayName());
        existing.setRole(validated.role());

        transactionManager.execute(connection -> {
            userRepository.update(connection, existing);
            if (validated.password() != null) {
                userRepository.updatePassword(connection, idUser, passwordHasher.hash(validated.password()));
            }
            return null;
        });
        existing.setPasswordHash(null);
        AppLogger.info("Admin " + actor.getEmail() + " memperbarui pengguna " + existing.getEmail());
        return existing;
    }

    public void setActive(User actor, String idUser, boolean active) {
        AccessControl.requireAdmin(actor);
        if (actor.getIdUser().equals(idUser) && !active) {
            throw new ValidationException("id", "Anda tidak dapat menonaktifkan akun sendiri.");
        }
        User existing = userRepository.findById(idUser);
        if (existing == null) {
            throw new AppException("Pengguna tidak ditemukan.");
        }
        if (existing.isAdmin() && !active && userRepository.countAdmins() <= 1) {
            throw new ValidationException("id", "Akun admin terakhir tidak dapat dinonaktifkan.");
        }
        userRepository.setActive(idUser, active);
        existing.setActive(active);
        AppLogger.info("Admin " + actor.getEmail() + (active ? " mengaktifkan " : " menonaktifkan ")
                + "pengguna " + existing.getEmail());
    }

    private void ensureUnique(String email, String excludeId) {
        if (userRepository.emailExists(email, excludeId)) {
            throw new ValidationException("email", "Email sudah terdaftar.");
        }
    }

    private ValidatedData validate(UserFormData data, boolean edit) {
        String displayName = ValidationUtil.required(data.displayName(), "displayName", "Nama lengkap", 100);
        ValidationUtil.email(data.email(), "email");
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

        return new ValidatedData(displayName, data.email().trim().toLowerCase(Locale.ROOT),
                password, data.role());
    }

    public record UserFormData(String email, String displayName, String password,
                               String passwordConfirmation, Role role) {
        public UserFormData(String email, String displayName, String password, Role role) {
            this(email, displayName, password, password, role);
        }
    }

    private record ValidatedData(String displayName, String email, String password, Role role) {
    }
}
