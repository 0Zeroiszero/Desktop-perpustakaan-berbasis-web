package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.service.UserService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Form tambah/ubah pengguna oleh admin.
 */
public class UserFormController extends BaseController {

    @FXML private Label formTitle;
    @FXML private Label passwordHint;
    @FXML private Label errorLabel;
    @FXML private TextField namaDepanField;
    @FXML private TextField namaBelakangField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextArea bioArea;

    private User editing;

    @FXML
    private void initialize() {
        roleCombo.getItems().setAll(Role.ADMIN, Role.MEMBER);
        genderCombo.getItems().setAll("Tidak disebutkan", "Laki-laki", "Perempuan");
        hideError();
    }

    public void configure(User user) {
        this.editing = user;
        formTitle.setText(user == null ? "Tambah Pengguna" : "Ubah Pengguna");
        passwordHint.setText(user == null
                ? "Password minimal 8 karakter."
                : "Kosongkan password jika tidak ingin mengubahnya.");
        namaDepanField.setText(user == null ? "" : safe(user.getNamaDepan()));
        namaBelakangField.setText(user == null ? "" : safe(user.getNamaBelakang()));
        usernameField.setText(user == null ? "" : safe(user.getUsername()));
        emailField.setText(user == null ? "" : safe(user.getEmail()));
        phoneField.setText(user == null ? "" : safe(user.getNomorPonsel()));
        addressField.setText(user == null ? "" : safe(user.getAlamat()));
        bioArea.setText(user == null ? "" : safe(user.getBio()));
        passwordField.clear();
        confirmPasswordField.clear();
        roleCombo.getSelectionModel().select(user == null ? Role.MEMBER : user.getRole());
        genderCombo.getSelectionModel().select(genderLabel(user == null ? null : user.getJenisKelamin()));
        hideError();
        namaDepanField.requestFocus();
    }

    public boolean save() {
        hideError();
        String password = passwordField.getText();
        String confirmation = confirmPasswordField.getText();
        if (editing != null && (password == null || password.isBlank())) {
            password = null;
            confirmation = null;
        }
        try {
            UserService.UserFormData data = new UserService.UserFormData(
                    namaDepanField.getText(),
                    namaBelakangField.getText(),
                    usernameField.getText(),
                    emailField.getText(),
                    password,
                    confirmation,
                    roleCombo.getValue(),
                    FormatUtil.genderCode(genderCombo.getValue()),
                    phoneField.getText(),
                    addressField.getText(),
                    bioArea.getText());
            if (editing == null) {
                app.getUserService().create(currentUser(), data);
            } else {
                User updated = app.getUserService().update(currentUser(), editing.getIdUser(), data);
                if (updated.getIdUser().equals(currentUser().getIdUser())) {
                    session.updateCurrentUser(updated);
                    Ui.refreshUser();
                }
            }
            return true;
        } catch (ValidationException e) {
            showError(e.getMessage());
            focusField(e.getFieldErrors().keySet().stream().findFirst().orElse(null));
            return false;
        } catch (AppException e) {
            showError(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            AppLogger.error("Gagal menyimpan pengguna.", e);
            showError("Pengguna gagal disimpan. Silakan coba lagi.");
            return false;
        }
    }

    private void focusField(String field) {
        if (field == null) {
            return;
        }
        switch (field) {
            case "namaDepan" -> namaDepanField.requestFocus();
            case "username" -> usernameField.requestFocus();
            case "email" -> emailField.requestFocus();
            case "password" -> passwordField.requestFocus();
            case "confirmPassword" -> confirmPasswordField.requestFocus();
            case "role" -> roleCombo.requestFocus();
            case "nomorPonsel" -> phoneField.requestFocus();
            default -> {
            }
        }
    }

    private String genderLabel(String code) {
        if ("L".equalsIgnoreCase(code)) {
            return "Laki-laki";
        }
        if ("P".equalsIgnoreCase(code)) {
            return "Perempuan";
        }
        return "Tidak disebutkan";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
