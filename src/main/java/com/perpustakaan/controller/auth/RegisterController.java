package com.perpustakaan.controller.auth;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.service.AuthService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * Halaman registrasi anggota baru.
 */
public class RegisterController extends BaseController {

    @FXML private TextField emailField;
    @FXML private TextField displayNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private ProgressIndicator registerProgress;

    private boolean loading;

    @FXML
    private void initialize() {
        hideError();
        registerProgress.setVisible(false);
        registerProgress.setManaged(false);
        emailField.textProperty().addListener((observable, oldValue, newValue) -> refreshRegisterButton());
        displayNameField.textProperty().addListener((observable, oldValue, newValue) -> refreshRegisterButton());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> refreshRegisterButton());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> refreshRegisterButton());
        refreshRegisterButton();
    }

    @Override
    public void onShow() {
        super.onShow();
        hideError();
        emailField.requestFocus();
    }

    @FXML
    private void handleRegister() {
        hideError();
        if (!validateLocalInputs()) {
            return;
        }
        AuthService.RegisterRequest request = new AuthService.RegisterRequest(
                emailField.getText().trim(),
                displayNameField.getText().trim(),
                passwordField.getText(),
                confirmPasswordField.getText());
        setLoading(true);
        runAsync(() -> app.getAuthService().register(request), user -> {
            setLoading(false);
            Ui.navigate(Route.LOGIN,
                    NavigationContext.message("Registrasi berhasil. Silakan masuk dengan akun baru Anda."));
        }, error -> {
            setLoading(false);
            showRegisterError(error);
        });
    }

    private boolean validateLocalInputs() {
        if (isBlank(emailField.getText())) {
            showError("Email wajib diisi.");
            emailField.requestFocus();
            return false;
        }
        if (!emailField.getText().trim().contains("@") || !emailField.getText().trim().contains(".")) {
            showError("Email tidak valid.");
            emailField.requestFocus();
            return false;
        }
        if (isBlank(displayNameField.getText())) {
            showError("Nama lengkap wajib diisi.");
            displayNameField.requestFocus();
            return false;
        }
        if (isBlank(passwordField.getText())) {
            showError("Password wajib diisi.");
            passwordField.requestFocus();
            return false;
        }
        if (passwordField.getText().length() < 8) {
            showError("Password minimal 8 karakter.");
            passwordField.requestFocus();
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Konfirmasi password tidak sesuai.");
            confirmPasswordField.requestFocus();
            return false;
        }
        return true;
    }

    private void refreshRegisterButton() {
        registerButton.setDisable(loading || isBlank(emailField.getText())
                || isBlank(displayNameField.getText()) || isBlank(passwordField.getText())
                || !passwordField.getText().equals(confirmPasswordField.getText()));
    }

    private void showRegisterError(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof AppException appException) {
            showError(appException.getMessage());
        } else {
            AppLogger.error("Registrasi gagal.", cause);
            showError("Terjadi kesalahan saat mendaftar. Silakan coba lagi.");
        }
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        registerButton.setText(loading ? "Mendaftar..." : "Daftar Sekarang");
        registerProgress.setVisible(loading);
        registerProgress.setManaged(loading);
        emailField.setDisable(loading);
        displayNameField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        refreshRegisterButton();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    @FXML
    private void handleBack() {
        Ui.navigate(Route.LOGIN, NavigationContext.empty());
    }
}
