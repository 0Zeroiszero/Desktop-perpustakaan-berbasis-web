package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.service.AuthService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Halaman registrasi anggota baru.
 */
public class RegisterController extends BaseController {

    @FXML private TextField namaDepanField;
    @FXML private TextField namaBelakangField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private TextField addressArea;
    @FXML private TextArea bioArea;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private ProgressIndicator registerProgress;

    @FXML
    private void initialize() {
        genderCombo.getItems().setAll("Tidak disebutkan", "Laki-laki", "Perempuan");
        genderCombo.getSelectionModel().selectFirst();
        hideError();
        registerProgress.setVisible(false);
        registerProgress.setManaged(false);
    }

    @Override
    public void onShow() {
        super.onShow();
        hideError();
        namaDepanField.requestFocus();
    }

    @FXML
    private void handleRegister() {
        hideError();
        AuthService.RegisterRequest request = new AuthService.RegisterRequest(
                namaDepanField.getText(),
                namaBelakangField.getText(),
                usernameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                FormatUtil.genderCode(genderCombo.getValue()),
                phoneField.getText(),
                addressArea.getText(),
                bioArea.getText());
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

    private void showRegisterError(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof ValidationException validation) {
            showError(validation.getMessage());
            focusField(validation.getFieldErrors().keySet().stream().findFirst().orElse(null));
        } else if (cause instanceof AppException appException) {
            showError(appException.getMessage());
        } else {
            AppLogger.error("Registrasi gagal.", cause);
            showError("Terjadi kesalahan saat mendaftar. Silakan coba lagi.");
        }
    }

    private void focusField(String field) {
        if (field == null) {
            return;
        }
        switch (field) {
            case "namaDepan" -> namaDepanField.requestFocus();
            case "namaBelakang" -> namaBelakangField.requestFocus();
            case "username" -> usernameField.requestFocus();
            case "email" -> emailField.requestFocus();
            case "password" -> passwordField.requestFocus();
            case "confirmPassword" -> confirmPasswordField.requestFocus();
            case "jenisKelamin" -> genderCombo.requestFocus();
            case "nomorPonsel" -> phoneField.requestFocus();
            case "alamat" -> addressArea.requestFocus();
            case "bio" -> bioArea.requestFocus();
            default -> {
            }
        }
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        registerButton.setText(loading ? "Mendaftar..." : "Daftar Sekarang");
        registerProgress.setVisible(loading);
        registerProgress.setManaged(loading);
        namaDepanField.setDisable(loading);
        namaBelakangField.setDisable(loading);
        usernameField.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        genderCombo.setDisable(loading);
        phoneField.setDisable(loading);
        addressArea.setDisable(loading);
        bioArea.setDisable(loading);
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
