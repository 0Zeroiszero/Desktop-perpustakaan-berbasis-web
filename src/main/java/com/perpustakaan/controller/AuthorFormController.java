package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Author;
import com.perpustakaan.service.AuthorService;
import com.perpustakaan.util.AppLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Form tambah/ubah penulis.
 */
public class AuthorFormController extends BaseController {

    @FXML private Label formTitle;
    @FXML private Label errorLabel;
    @FXML private TextField namaDepanField;
    @FXML private TextField namaBelakangField;
    @FXML private TextField emailField;
    @FXML private TextField negaraField;
    @FXML private TextArea bioArea;

    private Author editing;

    public void configure(Author author) {
        this.editing = author;
        formTitle.setText(author == null ? "Tambah Penulis" : "Ubah Penulis");
        namaDepanField.setText(author == null ? "" : safe(author.getNamaDepan()));
        namaBelakangField.setText(author == null ? "" : safe(author.getNamaBelakang()));
        emailField.setText(author == null ? "" : safe(author.getEmail()));
        negaraField.setText(author == null ? "" : safe(author.getNegaraAsal()));
        bioArea.setText(author == null ? "" : safe(author.getBio()));
        hideError();
        namaDepanField.requestFocus();
    }

    public boolean save() {
        hideError();
        try {
            AuthorService.AuthorFormData data = new AuthorService.AuthorFormData(
                    namaDepanField.getText(),
                    namaBelakangField.getText(),
                    emailField.getText(),
                    negaraField.getText(),
                    bioArea.getText());
            if (editing == null) {
                app.getAuthorService().create(currentUser(), data);
            } else {
                app.getAuthorService().update(currentUser(), editing.getIdAuthor(), data);
            }
            return true;
        } catch (ValidationException e) {
            showError(e.getMessage());
            if (e.getFieldErrors().containsKey("namaDepan")) {
                namaDepanField.requestFocus();
            } else if (e.getFieldErrors().containsKey("email")) {
                emailField.requestFocus();
            }
            return false;
        } catch (AppException e) {
            showError(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            AppLogger.error("Gagal menyimpan penulis.", e);
            showError("Penulis gagal disimpan. Silakan coba lagi.");
            return false;
        }
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
