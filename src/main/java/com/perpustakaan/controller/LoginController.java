package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.User;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * Halaman masuk.
 */
public class LoginController extends BaseController {

    @FXML private TextField identifierField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loginProgress;
    @FXML private Hyperlink registerLink;

    @FXML
    private void initialize() {
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        togglePasswordButton.setGraphic(Icons.icon(Icons.EYE, 16));
        togglePasswordButton.getStyleClass().addAll("btn-icon", "btn-ghost");
        togglePasswordButton.setOnAction(event -> togglePassword());
        hide(errorLabel);
        hide(successLabel);
        loginProgress.setVisible(false);
        loginProgress.setManaged(false);
    }

    @Override
    public void onShow() {
        super.onShow();
        hide(errorLabel);
        hide(successLabel);
        if (context().message() != null && !context().message().isBlank()) {
            successLabel.setText(context().message());
            show(successLabel);
        }
        if (identifierField.getText() == null || identifierField.getText().isBlank()) {
            identifierField.requestFocus();
        } else {
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleLogin() {
        hide(errorLabel);
        hide(successLabel);
        String identifier = identifierField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : visiblePasswordField.getText();
        if (identifier == null || identifier.isBlank()) {
            showError("Email atau username wajib diisi.");
            identifierField.requestFocus();
            return;
        }
        if (password == null || password.isEmpty()) {
            showError("Password wajib diisi.");
            focusPassword();
            return;
        }

        setLoading(true);
        runAsync(() -> app.getAuthService().login(identifier, password), user -> {
            setLoading(false);
            session.login(user);
            Route destination = user.isAdmin() ? Route.ADMIN_DASHBOARD : Route.MEMBER_DASHBOARD;
            Ui.navigate(destination, NavigationContext.empty());
            Ui.success("Selamat datang, " + user.getNamaLengkap() + ".");
        }, error -> {
            setLoading(false);
            showLoginError(error);
        });
    }

    private void showLoginError(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof ValidationException validation) {
            showError(validation.getMessage());
        } else if (cause instanceof AppException appException) {
            showError(appException.getMessage());
        } else {
            AppLogger.error("Login gagal.", cause);
            showError("Terjadi kesalahan saat masuk. Silakan coba lagi.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        show(errorLabel);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Memproses..." : "Masuk");
        loginProgress.setVisible(loading);
        loginProgress.setManaged(loading);
        identifierField.setDisable(loading);
        passwordField.setDisable(loading);
        visiblePasswordField.setDisable(loading);
        togglePasswordButton.setDisable(loading);
    }

    private void togglePassword() {
        if (passwordField.isVisible()) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
            togglePasswordButton.setGraphic(Icons.icon(Icons.EYE_OFF, 16));
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordButton.setGraphic(Icons.icon(Icons.EYE, 16));
        }
    }

    private void focusPassword() {
        if (passwordField.isVisible()) {
            passwordField.requestFocus();
        } else {
            visiblePasswordField.requestFocus();
        }
    }

    @FXML
    private void handleRegister() {
        Ui.navigate(Route.REGISTER, NavigationContext.empty());
    }

    private void show(Label label) {
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hide(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
