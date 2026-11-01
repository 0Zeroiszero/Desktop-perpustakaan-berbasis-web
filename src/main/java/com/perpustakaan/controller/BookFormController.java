package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookFormat;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.service.EbookService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.Async;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.AuthorPicker;
import com.perpustakaan.view.Icons;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Form tambah/ubah e-book, termasuk unggah cover dan PDF.
 */
public class BookFormController extends BaseController {

    @FXML private Label formTitle;
    @FXML private Label errorLabel;
    @FXML private TabPane formTabs;
    @FXML private TextField judulField;
    @FXML private TextField tahunField;
    @FXML private TextField halamanField;
    @FXML private TextField isbnField;
    @FXML private TextField penerbitField;
    @FXML private ComboBox<Kategori> kategoriCombo;
    @FXML private ComboBox<BookFormat> formatCombo;
    @FXML private TextArea sinopsisArea;
    @FXML private VBox authorPickerBox;
    @FXML private StackPane coverPreview;
    @FXML private Label coverPathLabel;
    @FXML private Label ebookPathLabel;
    @FXML private Label pageCountLabel;
    @FXML private Label existingFileHint;

    private BookDetail editingDetail;
    private Ebook editing;
    private List<Author> allAuthors = List.of();
    private AuthorPicker authorPicker;
    private Path coverSource;
    private Path ebookSource;
    private int pdfPages;

    @FXML
    private void initialize() {
        formatCombo.getItems().setAll(BookFormat.PDF, BookFormat.EPUB, BookFormat.MOBI);
        formatCombo.getSelectionModel().select(BookFormat.PDF);
        formatCombo.setDisable(true);
        hideError();
        setCoverPlaceholder();
        try {
            kategoriCombo.getItems().setAll(app.getEbookService().categories(currentUser()));
            allAuthors = app.getEbookService().authors(currentUser());
            rebuildAuthorPicker(List.of());
        } catch (RuntimeException e) {
            AppLogger.error("Gagal memuat data form buku.", e);
            showError("Data kategori atau penulis gagal dimuat. Tutup form lalu coba lagi.");
        }
    }

    public void configure(BookDetail detail) {
        this.editingDetail = detail;
        this.editing = detail == null ? null : detail.ebook();
        this.coverSource = null;
        this.ebookSource = null;
        this.pdfPages = 0;
        hideError();
        formTabs.getSelectionModel().selectFirst();

        if (editing == null) {
            formTitle.setText("Tambah Buku");
            judulField.clear();
            tahunField.clear();
            halamanField.clear();
            isbnField.clear();
            penerbitField.clear();
            sinopsisArea.clear();
            formatCombo.getSelectionModel().select(BookFormat.PDF);
            formatCombo.setDisable(true);
            kategoriCombo.getSelectionModel().clearSelection();
            coverPathLabel.setText("Belum ada cover dipilih.");
            ebookPathLabel.setText("Belum ada PDF dipilih.");
            pageCountLabel.setText("Pilih berkas PDF untuk melihat jumlah halaman.");
            existingFileHint.setText("Buku baru wajib menggunakan berkas PDF.");
            rebuildAuthorPicker(List.of());
            setCoverPlaceholder();
        } else {
            formTitle.setText("Ubah Buku");
            judulField.setText(safe(editing.getJudul()));
            tahunField.setText(editing.getTahunTerbit() == null ? "" : String.valueOf(editing.getTahunTerbit()));
            halamanField.setText(editing.getJumlahHalaman() == null ? "" : String.valueOf(editing.getJumlahHalaman()));
            isbnField.setText(safe(editing.getIsbn()));
            penerbitField.setText(safe(editing.getPenerbit()));
            sinopsisArea.setText(safe(editing.getSinopsis()));
            formatCombo.getSelectionModel().select(editing.getFormat());
            formatCombo.setDisable(true);
            selectKategori(editing.getKategoriId());
            rebuildAuthorPicker(detail.authors().stream().map(Author::getIdAuthor).toList());
            coverPathLabel.setText(editing.getCoverPath() == null || editing.getCoverPath().isBlank()
                    ? "Belum ada cover."
                    : "Cover saat ini: " + fileName(editing.getCoverPath()));
            ebookPathLabel.setText(editing.getFilePath() == null || editing.getFilePath().isBlank()
                    ? "Belum ada berkas."
                    : "Berkas saat ini: " + fileName(editing.getFilePath()));
            existingFileHint.setText("Kosongkan pilihan berkas jika tidak ingin mengganti cover atau PDF.");
            pageCountLabel.setText(editing.getFormat() == BookFormat.PDF
                    ? "Jumlah halaman tercatat: " + (editing.getJumlahHalaman() == null ? "-" : editing.getJumlahHalaman())
                    : "Format " + editing.getFormat().displayName() + " tidak dapat dibaca oleh pembaca PDF.");
            showCover(editing.getCoverPath());
        }
        judulField.requestFocus();
    }

    private void rebuildAuthorPicker(List<String> selectedIds) {
        authorPicker = new AuthorPicker(allAuthors, selectedIds);
        authorPickerBox.getChildren().setAll(authorPicker);
    }

    private void selectKategori(String kategoriId) {
        for (Kategori kategori : kategoriCombo.getItems()) {
            if (kategori.getIdKategori().equals(kategoriId)) {
                kategoriCombo.getSelectionModel().select(kategori);
                return;
            }
        }
    }

    @FXML
    private void handleChooseCover() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Cover Buku");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gambar JPG/PNG", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(window());
        if (file == null) {
            return;
        }
        coverSource = file.toPath();
        coverPathLabel.setText("Cover baru: " + file.getName());
        Image image = ImageUtil.loadImage(coverSource, 86, 116);
        if (image == null) {
            setCoverPlaceholder();
            showError("Cover tidak dapat dibaca. Pilih gambar JPG atau PNG lain.");
            coverSource = null;
        } else {
            ImageView view = new ImageView(image);
            view.setFitWidth(86);
            view.setFitHeight(116);
            view.setPreserveRatio(true);
            coverPreview.getChildren().setAll(view);
        }
    }

    @FXML
    private void handleChooseEbook() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Berkas PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF", "*.pdf"));
        File file = chooser.showOpenDialog(window());
        if (file == null) {
            return;
        }
        ebookSource = file.toPath();
        ebookPathLabel.setText("PDF baru: " + file.getName());
        formatCombo.getSelectionModel().select(BookFormat.PDF);
        pageCountLabel.setText("Menghitung jumlah halaman PDF...");
        Path path = ebookSource;
        Async.run(() -> app.getEbookService().getPdfPageCount(path), count -> {
            if (!path.equals(ebookSource)) {
                return;
            }
            pdfPages = count;
            pageCountLabel.setText("PDF terdeteksi: " + count + " halaman.");
            halamanField.setText(String.valueOf(count));
        }, error -> {
            if (!path.equals(ebookSource)) {
                return;
            }
            ebookSource = null;
            pdfPages = 0;
            ebookPathLabel.setText("Belum ada PDF dipilih.");
            pageCountLabel.setText("PDF gagal dibaca. Pilih berkas lain.");
            showError(unwrap(error).getMessage() == null
                    ? "Berkas PDF tidak dapat dibaca." : unwrap(error).getMessage());
        });
    }

    public boolean save() {
        hideError();
        if (editing == null && ebookSource == null) {
            showError("Berkas PDF wajib dipilih untuk buku baru.");
            formTabs.getSelectionModel().select(2);
            return false;
        }
        try {
            Kategori kategori = kategoriCombo.getValue();
            EbookService.BookFormData data = new EbookService.BookFormData(
                    judulField.getText(),
                    tahunField.getText(),
                    halamanField.getText(),
                    isbnField.getText(),
                    penerbitField.getText(),
                    sinopsisArea.getText(),
                    formatCombo.getValue(),
                    kategori == null ? null : kategori.getIdKategori(),
                    authorPicker == null ? List.of() : authorPicker.getSelectedAuthorIds(),
                    coverSource,
                    ebookSource);
            if (editing == null) {
                app.getEbookService().create(currentUser(), data);
            } else {
                app.getEbookService().update(currentUser(), editing.getIdBook(), data);
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
            AppLogger.error("Gagal menyimpan buku.", e);
            showError("Buku gagal disimpan. Silakan coba lagi.");
            return false;
        }
    }

    private void focusField(String field) {
        if (field == null) {
            return;
        }
        switch (field) {
            case "judul" -> judulField.requestFocus();
            case "tahunTerbit" -> tahunField.requestFocus();
            case "jumlahHalaman" -> halamanField.requestFocus();
            case "isbn" -> isbnField.requestFocus();
            case "penerbit" -> penerbitField.requestFocus();
            case "kategoriId" -> kategoriCombo.requestFocus();
            case "authorIds" -> formTabs.getSelectionModel().select(1);
            case "file" -> formTabs.getSelectionModel().select(2);
            default -> {
            }
        }
    }

    private void showCover(String storedPath) {
        Image image = ImageUtil.loadCover(storedPath, 86, 116);
        if (image == null) {
            setCoverPlaceholder();
            return;
        }
        ImageView view = new ImageView(image);
        view.setFitWidth(86);
        view.setFitHeight(116);
        view.setPreserveRatio(true);
        coverPreview.getChildren().setAll(view);
    }

    private void setCoverPlaceholder() {
        coverPreview.getChildren().setAll(Icons.icon(Icons.BOOK, 30, "cover-placeholder-icon"));
    }

    private Window window() {
        return coverPreview.getScene() == null ? null : coverPreview.getScene().getWindow();
    }

    private String fileName(String path) {
        int slash = path == null ? -1 : Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showError(String message) {
        errorLabel.setText(message == null ? "Terjadi kesalahan." : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
