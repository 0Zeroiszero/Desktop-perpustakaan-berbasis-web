package com.perpustakaan.controller.admin;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.Ebook;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.Publisher;
import com.perpustakaan.service.EbookService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.Async;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.AutoCompleteTextField;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
    @FXML private AutoCompleteTextField penulisField;
    @FXML private AutoCompleteTextField genreField;
    @FXML private AutoCompleteTextField penerbitField;
    @FXML private TextArea sinopsisArea;
    @FXML private StackPane coverPreview;
    @FXML private Label coverPathLabel;
    @FXML private Label ebookPathLabel;
    @FXML private Label pageCountLabel;
    @FXML private Label existingFileHint;

    private final BooleanProperty saveDisabled = new SimpleBooleanProperty(true);
    private BookDetail editingDetail;
    private Ebook editing;
    private Path coverSource;
    private Path ebookSource;

    @FXML
    private void initialize() {
        hideError();
        setCoverPlaceholder();
        try {
            penulisField.setProfileSuggestions(app.getEbookService().authors(currentUser()).stream()
                    .map(a -> new AutoCompleteTextField.Suggestion(
                            a.getNamaLengkap(), "author", a.getIdAuthor())).toList());
            genreField.setProfileSuggestions(app.getEbookService().categories(currentUser()).stream()
                    .map(g -> new AutoCompleteTextField.Suggestion(
                            g.getNamaKategori(), "genre", g.getIdKategori())).toList());
            List<Publisher> publishers = app.getEbookService().publishers(currentUser());
            penerbitField.setProfileSuggestions(publishers.stream()
                    .map(p -> new AutoCompleteTextField.Suggestion(
                            p.getNama(), "publisher", p.getIdPublisher())).toList());
            penulisField.setProfileOpener(this::openProfile);
            genreField.setProfileOpener(this::openProfile);
            penerbitField.setProfileOpener(this::openProfile);
        } catch (RuntimeException e) {
            AppLogger.error("Gagal memuat data form buku.", e);
            showError("Data penulis, genre, atau penerbit gagal dimuat. Tutup form lalu coba lagi.");
        }
        judulField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        penulisField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        genreField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        penerbitField.textProperty().addListener((observable, oldValue, newValue) -> refreshSaveState());
        refreshSaveState();
    }

    public BooleanProperty saveDisabledProperty() {
        return saveDisabled;
    }

    private void openProfile(AutoCompleteTextField.Suggestion suggestion) {
        try {
            com.perpustakaan.service.ProfileService.ProfileInfo info =
                    app.getProfileService().load(currentUser(), suggestion.type(), suggestion.id());
            com.perpustakaan.view.ProfileDialog.show(window(penulisField), info);
        } catch (RuntimeException e) {
            AppLogger.error("Gagal membuka profil.", e);
            showError("Profil tidak dapat dibuka: " + unwrap(e).getMessage());
        }
    }

    public void configure(BookDetail detail) {
        this.editingDetail = detail;
        this.editing = detail == null ? null : detail.ebook();
        this.coverSource = null;
        this.ebookSource = null;
        hideError();
        formTabs.getSelectionModel().selectFirst();

        if (editing == null) {
            formTitle.setText("Tambah Buku");
            judulField.clear();
            tahunField.clear();
            halamanField.clear();
            isbnField.clear();
            penulisField.clear();
            genreField.clear();
            penerbitField.clear();
            sinopsisArea.clear();
            coverPathLabel.setText("Belum ada cover dipilih.");
            ebookPathLabel.setText("Belum ada PDF dipilih.");
            pageCountLabel.setText("Pilih berkas PDF untuk melihat jumlah halaman.");
            existingFileHint.setText("Buku baru wajib menggunakan berkas PDF.");
            setCoverPlaceholder();
        } else {
            formTitle.setText("Ubah Buku");
            judulField.setText(safe(editing.getJudul()));
            tahunField.setText(editing.getTahunTerbit() == null ? "" : String.valueOf(editing.getTahunTerbit()));
            halamanField.setText(editing.getJumlahHalaman() == null ? "" : String.valueOf(editing.getJumlahHalaman()));
            isbnField.setText(safe(editing.getIsbn()));
            penulisField.setText(detail.penulis());
            genreField.setText(detail.kategori() == null ? "" : safe(detail.kategori().getNamaKategori()));
            penerbitField.setText(safe(detail.publisherName()));
            sinopsisArea.setText(safe(editing.getSinopsis()));
            coverPathLabel.setText(editing.getCoverPath() == null || editing.getCoverPath().isBlank()
                    ? "Belum ada cover." : "Cover saat ini: " + fileName(editing.getCoverPath()));
            ebookPathLabel.setText(editing.getFilePath() == null || editing.getFilePath().isBlank()
                    ? "Belum ada berkas." : "Berkas saat ini: " + fileName(editing.getFilePath()));
            existingFileHint.setText("Kosongkan pilihan berkas jika tidak ingin mengganti cover atau PDF.");
            pageCountLabel.setText("Jumlah halaman tercatat: "
                    + (editing.getJumlahHalaman() == null ? "-" : editing.getJumlahHalaman()));
            showCover(editing.getCoverPath());
        }
        refreshSaveState();
        judulField.requestFocus();
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
        refreshSaveState();
        pageCountLabel.setText("Menghitung jumlah halaman PDF...");
        Path path = ebookSource;
        Async.run(() -> app.getEbookService().getPdfPageCount(path), count -> {
            if (!path.equals(ebookSource)) {
                return;
            }
            pageCountLabel.setText("PDF terdeteksi: " + count + " halaman.");
            halamanField.setText(String.valueOf(count));
        }, error -> {
            if (!path.equals(ebookSource)) {
                return;
            }
            ebookSource = null;
            refreshSaveState();
            ebookPathLabel.setText("Belum ada PDF dipilih.");
            pageCountLabel.setText("PDF gagal dibaca. Pilih berkas lain.");
            showError(unwrap(error).getMessage() == null
                    ? "Berkas PDF tidak dapat dibaca." : unwrap(error).getMessage());
        });
    }

    public boolean save() {
        hideError();
        if (isBlank(judulField.getText())) {
            showError("Judul buku wajib diisi.");
            judulField.requestFocus();
            return false;
        }
        if (isBlank(penulisField.getText())) {
            showError("Penulis wajib diisi.");
            penulisField.requestFocus();
            return false;
        }
        if (isBlank(genreField.getText())) {
            showError("Genre wajib diisi.");
            genreField.requestFocus();
            return false;
        }
        if (isBlank(penerbitField.getText())) {
            showError("Penerbit wajib diisi.");
            penerbitField.requestFocus();
            return false;
        }
        if (editing == null && ebookSource == null) {
            showError("Berkas PDF wajib dipilih untuk buku baru.");
            formTabs.getSelectionModel().select(1);
            return false;
        }
        try {
            EbookService.BookFormData data = new EbookService.BookFormData(
                    judulField.getText(),
                    tahunField.getText(),
                    halamanField.getText(),
                    isbnField.getText(),
                    penerbitField.getText(),
                    genreField.getText(),
                    penulisField.getText(),
                    sinopsisArea.getText(),
                    coverSource,
                    ebookSource);
            if (editing == null) {
                app.getEbookService().create(currentUser(), data);
            } else {
                app.getEbookService().update(currentUser(), editing.getIdBook(), data);
            }
            return true;
        } catch (AppException e) {
            showError(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            AppLogger.error("Gagal menyimpan buku.", e);
            showError("Buku gagal disimpan. Silakan coba lagi.");
            return false;
        }
    }

    private void refreshSaveState() {
        saveDisabled.set(isBlank(judulField.getText()) || isBlank(penulisField.getText())
                || isBlank(genreField.getText()) || isBlank(penerbitField.getText())
                || (editing == null && ebookSource == null));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
