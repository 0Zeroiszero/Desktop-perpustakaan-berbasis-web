package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.service.KategoriService;
import com.perpustakaan.util.AppLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Form tambah/ubah kategori.
 */
public class KategoriFormController extends BaseController {

    @FXML private Label formTitle;
    @FXML private Label errorLabel;
    @FXML private TextField namaField;
    @FXML private TextArea deskripsiArea;

    private Kategori editing;

    public void configure(Kategori kategori) {
        this.editing = kategori;
        formTitle.setText(kategori == null ? "Tambah Kategori" : "Ubah Kategori");
        namaField.setText(kategori == null ? "" : kategori.getNamaKategori());
        deskripsiArea.setText(kategori == null || kategori.getDeskripsi() == null ? "" : kategori.getDeskripsi());
        hideError();
        namaField.requestFocus();
    }

    public boolean save() {
        hideError();
        try {
            KategoriService.KategoriFormData data = new KategoriService.KategoriFormData(
                    namaField.getText(), deskripsiArea.getText());
            if (editing == null) {
                app.getKategoriService().create(currentUser(), data);
            } else {
                app.getKategoriService().update(currentUser(), editing.getIdKategori(), data);
            }
            return true;
        } catch (ValidationException e) {
            showError(e.getMessage());
            if (e.getFieldErrors().containsKey("namaKategori")) {
                namaField.requestFocus();
            }
            return false;
        } catch (AppException e) {
            showError(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            AppLogger.error("Gagal menyimpan kategori.", e);
            showError("Kategori gagal disimpan. Silakan coba lagi.");
            return false;
        }
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
