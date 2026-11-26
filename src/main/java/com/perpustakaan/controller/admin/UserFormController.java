package com.perpustakaan.controller.admin;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.service.UserService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.view.Ui;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Form tambah/ubah pengguna oleh admin.
 */
public class UserFormController extends BaseController {

    @FXML private Label formTitle;
    @FXML private Label passwordHint;
    @FXML private Label errorLabel;
    @FXML private TextField emailField;
    @FXML private TextField displayNameField;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordRequiredMark;
    @FXML private ComboBox<Role> roleCombo;

    private final BooleanProperty saveDisabled = new SimpleBooleanProperty(true);
    private User editing;

    @FXML
    private void initialize() {
        roleCombo.getItems().setAll(Role.ADMIN, Role.MEMBER);
        hideError();
        emailField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        displayNameField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        roleCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        refreshSaveState();
    }

    public BooleanProperty saveDisabledProperty() {
        return saveDisabled;
    }

    public void configure(User user) {
        this.editing = user;
        formTitle.setText(user == null ? "Tambah Pengguna" : "Ubah Pengguna");
        passwordHint.setText(user == null
                ? "Password minimal 8 karakter."
                : "Kosongkan password jika tidak ingin mengubahnya.");
        passwordRequiredMark.setVisible(user == null);
        passwordRequiredMark.setManaged(user == null);
        emailField.setText(user == null ? "" : safe(user.getEmail()));
        displayNameField.setText(user == null ? "" : safe(user.getNamaLengkap()));
        passwordField.clear();
        roleCombo.getSelectionModel().select(user == null ? Role.MEMBER : user.getRole());
        hideError();
        refreshSaveState();
        emailField.requestFocus();
    }

    public boolean save() {
        hideError();
        if (!requiredFieldsFilled()) {
            showError("Lengkapi kolom bertanda * terlebih dahulu.");
            return false;
        }
        if (!emailField.getText().trim().contains("@")) {
            showError("Email tidak valid.");
            emailField.requestFocus();
            return false;
        }
        if (editing == null && passwordField.getText().length() < 8) {
            showError("Password minimal 8 karakter.");
            passwordField.requestFocus();
            return false;
        }
        try {
            UserService.UserFormData data = new UserService.UserFormData(
                    emailField.getText().trim(),
                    displayNameField.getText().trim(),
                    isBlank(passwordField.getText()) ? null : passwordField.getText(),
                    roleCombo.getValue());
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
        } catch (AppException e) {
            showError(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            AppLogger.error("Gagal menyimpan pengguna.", e);
            showError("Pengguna gagal disimpan. Silakan coba lagi.");
            return false;
        }
    }

    private boolean requiredFieldsFilled() {
        return !isBlank(emailField.getText()) && !isBlank(displayNameField.getText())
                && roleCombo.getValue() != null
                && (editing != null || !isBlank(passwordField.getText()));
    }

    private void refreshSaveState() {
        saveDisabled.set(!requiredFieldsFilled());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
