package com.perpustakaan.controller;

import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.ReadingProgress;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.util.TextPaginator;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * Detail buku dengan sinopsis terpaginasi dan aksi rak baca.
 */
public class BookDetailController extends BaseController {

    @FXML private Button backButton;
    @FXML private StackPane coverPane;
    @FXML private Label fileStatusLabel;
    @FXML private Label categoryLabel;
    @FXML private Label formatLabel;
    @FXML private Label statusBadge;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label yearLabel;
    @FXML private Label pagesLabel;
    @FXML private Label publisherLabel;
    @FXML private Button readButton;
    @FXML private Button shelfButton;
    @FXML private TabPane detailTabs;
    @FXML private Label synopsisLabel;
    @FXML private Button synopsisPrevButton;
    @FXML private Button synopsisNextButton;
    @FXML private Label synopsisPageLabel;
    @FXML private Label isbnValue;
    @FXML private Label publisherValue;
    @FXML private Label yearValue;
    @FXML private Label pagesValue;
    @FXML private Label formatValue;
    @FXML private Label categoryValue;
    @FXML private Label fileValue;
    @FXML private ProgressBar detailProgressBar;
    @FXML private Label progressStatusLabel;
    @FXML private Label progressDatesLabel;

    private BookDetail detail;
    private String bookId;
    private List<String> synopsisPages = List.of("");
    private int synopsisPage;

    @FXML
    private void initialize() {
        backButton.setGraphic(Icons.icon(Icons.BACK, 16));
        readButton.setGraphic(Icons.icon(Icons.BOOK, 16));
        shelfButton.setGraphic(Icons.icon(Icons.BOOKMARK, 15));
        synopsisPrevButton.setGraphic(Icons.icon(Icons.CHEVRON_LEFT, 15));
        synopsisNextButton.setGraphic(Icons.icon(Icons.CHEVRON_RIGHT, 15));
        synopsisPrevButton.setOnAction(event -> changeSynopsisPage(-1));
        synopsisNextButton.setOnAction(event -> changeSynopsisPage(1));
    }

    @Override
    public void onShow() {
        super.onShow();
        bookId = context().bookId();
        if (bookId == null || bookId.isBlank()) {
            Ui.error("Buku tidak dipilih.");
            Ui.navigate(Route.CATALOG, NavigationContext.empty());
            return;
        }
        load();
    }

    private void load() {
        runAsync(() -> app.getEbookService().detail(currentUser(), bookId), this::applyDetail);
    }

    private void applyDetail(BookDetail bookDetail) {
        detail = bookDetail;
        titleLabel.setText(bookDetail.ebook().getJudul());
        authorLabel.setText(bookDetail.penulis());
        categoryLabel.setText(bookDetail.kategori() == null ? "Tanpa kategori" : bookDetail.kategori().getNamaKategori());
        formatLabel.setText(bookDetail.ebook().getFormat().displayName());
        yearLabel.setText(FormatUtil.year(bookDetail.ebook().getTahunTerbit()));
        pagesLabel.setText(FormatUtil.pages(bookDetail.ebook().getJumlahHalaman()));
        publisherLabel.setText(bookDetail.ebook().getPenerbit() == null
                || bookDetail.ebook().getPenerbit().isBlank() ? "Penerbit belum dicatat" : bookDetail.ebook().getPenerbit());
        isbnValue.setText(bookDetail.ebook().getIsbn() == null || bookDetail.ebook().getIsbn().isBlank()
                ? "-" : bookDetail.ebook().getIsbn());
        publisherValue.setText(bookDetail.ebook().getPenerbit() == null
                || bookDetail.ebook().getPenerbit().isBlank() ? "-" : bookDetail.ebook().getPenerbit());
        yearValue.setText(FormatUtil.year(bookDetail.ebook().getTahunTerbit()));
        pagesValue.setText(FormatUtil.pages(bookDetail.ebook().getJumlahHalaman()));
        formatValue.setText(bookDetail.ebook().getFormat().displayName());
        categoryValue.setText(bookDetail.kategori() == null ? "-" : bookDetail.kategori().getNamaKategori());
        fileValue.setText(fileName(bookDetail.ebook().getFilePath()));
        applyFileState(bookDetail);
        applyShelfState(bookDetail.progress());
        applyProgress(bookDetail.progress());
        applyCover(bookDetail);
        synopsisPages = TextPaginator.paginate(bookDetail.ebook().getSinopsis(), 560);
        synopsisPage = 0;
        renderSynopsis();
        detailTabs.getSelectionModel().selectFirst();
    }

    private void applyFileState(BookDetail bookDetail) {
        boolean available = app.getEbookService().isFileAvailable(bookDetail);
        if (!bookDetail.ebook().isReaderSupported()) {
            fileStatusLabel.setText("Format " + bookDetail.ebook().getFormat().displayName()
                    + " belum didukung pembaca PDF.");
            fileStatusLabel.getStyleClass().setAll("file-unavailable");
            readButton.setText("Format belum didukung");
            readButton.setDisable(true);
        } else if (!available) {
            fileStatusLabel.setText("Berkas PDF tidak ditemukan di storage.");
            fileStatusLabel.getStyleClass().setAll("file-unavailable");
            readButton.setText("Berkas tidak tersedia");
            readButton.setDisable(true);
        } else {
            fileStatusLabel.setText("PDF siap dibaca di aplikasi.");
            fileStatusLabel.getStyleClass().setAll("file-available");
            readButton.setText("Baca Sekarang");
            readButton.setDisable(false);
        }
    }

    private void applyShelfState(ReadingProgress progress) {
        shelfButton.getStyleClass().removeAll("btn-secondary", "btn-danger-ghost");
        if (progress == null) {
            shelfButton.setText("Tambah ke Rak");
            shelfButton.getStyleClass().add("btn-secondary");
        } else {
            shelfButton.setText("Hapus dari Rak");
            shelfButton.getStyleClass().add("btn-danger-ghost");
        }
    }

    private void applyProgress(ReadingProgress progress) {
        if (progress == null) {
            statusBadge.setText("Belum ada progres");
            statusBadge.getStyleClass().setAll("badge", "badge-gray");
            detailProgressBar.setProgress(0);
            progressStatusLabel.setText("0% • Belum dimulai");
            progressDatesLabel.setText("Mulai membaca untuk mencatat progres otomatis.");
            return;
        }
        statusBadge.setText(progress.getStatus().databaseValue());
        statusBadge.getStyleClass().setAll("badge", progress.getStatus().styleClass());
        detailProgressBar.setProgress(progress.getProgressInt() / 100.0);
        progressStatusLabel.setText(FormatUtil.percent(progress.getProgressPercent())
                + " • " + (progress.getLastPosition() == null ? "0" : progress.getLastPosition())
                + " dari " + (detail.ebook().getJumlahHalaman() == null ? "-" : detail.ebook().getJumlahHalaman())
                + " halaman");
        StringBuilder dates = new StringBuilder();
        dates.append("Mulai: ").append(FormatUtil.date(progress.getStartDate()));
        dates.append("   •   Terakhir: ").append(FormatUtil.date(progress.getLastRead()));
        if (progress.getEndDate() != null) {
            dates.append("   •   Selesai: ").append(FormatUtil.date(progress.getEndDate()));
        }
        progressDatesLabel.setText(dates.toString());
    }

    private void applyCover(BookDetail bookDetail) {
        Image image = ImageUtil.loadCover(bookDetail.ebook().getCoverPath(), 180, 240);
        if (image == null) {
            coverPane.getChildren().setAll(Icons.icon(Icons.BOOK, 40, "cover-placeholder-icon"));
            return;
        }
        ImageView view = new ImageView(image);
        view.setFitWidth(180);
        view.setFitHeight(240);
        view.setPreserveRatio(true);
        coverPane.getChildren().setAll(view);
    }

    private void renderSynopsis() {
        if (synopsisPages.isEmpty()) {
            synopsisPages = List.of("Sinopsis belum tersedia.");
        }
        synopsisPage = Math.max(0, Math.min(synopsisPage, synopsisPages.size() - 1));
        synopsisLabel.setText(synopsisPages.get(synopsisPage));
        synopsisPageLabel.setText((synopsisPage + 1) + " / " + synopsisPages.size());
        synopsisPrevButton.setDisable(synopsisPage <= 0);
        synopsisNextButton.setDisable(synopsisPage >= synopsisPages.size() - 1);
    }

    private void changeSynopsisPage(int delta) {
        synopsisPage += delta;
        renderSynopsis();
    }

    @FXML
    private void handleRead() {
        if (detail == null) {
            return;
        }
        Ui.navigate(Route.READER, NavigationContext.reader(bookId, context().origin()));
    }

    @FXML
    private void handleShelf() {
        if (detail == null) {
            return;
        }
        try {
            if (detail.hasProgress()) {
                if (!confirm("Hapus dari Rak", "Hapus buku ini dari rak baca Anda? Progres membaca juga akan dihapus.",
                        "Hapus", true)) {
                    return;
                }
                app.getReadingProgressService().removeFromShelf(currentUser(), bookId);
                success("Buku dihapus dari rak.");
            } else {
                app.getReadingProgressService().addToShelf(currentUser(), bookId);
                success("Buku ditambahkan ke rak.");
            }
            load();
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML
    private void handleBack() {
        Route origin = context().origin();
        if (origin == null || origin == Route.BOOK_DETAIL || origin == Route.READER) {
            origin = Route.CATALOG;
        }
        Ui.navigate(origin, NavigationContext.empty());
    }

    private String fileName(String path) {
        if (path == null || path.isBlank()) {
            return "-";
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? path : path.substring(slash + 1);
    }
}
