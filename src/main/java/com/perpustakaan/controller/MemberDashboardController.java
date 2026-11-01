package com.perpustakaan.controller;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.ReadingStats;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.BookCard;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.StatCard;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Dashboard anggota dengan ringkasan rak dan rekomendasi.
 */
public class MemberDashboardController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private Label welcomeSubtitle;
    @FXML private HBox statsRow;
    @FXML private HBox lastReadCard;
    @FXML private StackPane lastReadCover;
    @FXML private Label lastReadTitle;
    @FXML private Label lastReadAuthor;
    @FXML private Label lastReadProgressLabel;
    @FXML private ProgressBar lastReadProgressBar;
    @FXML private Button continueButton;
    @FXML private HBox recommendationRow;
    @FXML private Button browseButton;
    @FXML private Button viewAllButton;

    private BookListItem lastRead;

    @FXML
    private void initialize() {
        browseButton.setGraphic(Icons.icon(Icons.LIBRARY, 15));
        continueButton.setGraphic(Icons.icon(Icons.ARROW_RIGHT, 15));
        viewAllButton.setGraphic(Icons.icon(Icons.ARROW_RIGHT, 14));
        setCoverPlaceholder();
    }

    @Override
    public void onShow() {
        super.onShow();
        welcomeLabel.setText(FormatUtil.greeting() + ", " + currentUser().getNamaDepan() + ".");
        welcomeSubtitle.setText("Temukan bacaan berikutnya dan lanjutkan progres Anda.");
        load();
    }

    private void load() {
        runAsync(() -> app.getDashboardService().member(currentUser()), this::applyStats);
    }

    private void applyStats(ReadingStats stats) {
        statsRow.getChildren().setAll(
                new StatCard(Icons.BOOK, "Total Buku", FormatUtil.number(stats.totalBooks()),
                        "Siap dijelajahi", "stat-icon-purple"),
                new StatCard(Icons.BOOKMARK, "Rak Saya", FormatUtil.number(stats.shelfCount()),
                        "Buku tersimpan", "stat-icon-blue"),
                new StatCard(Icons.PAGES, "Sedang Dibaca", FormatUtil.number(stats.currentlyReading()),
                        "Bacaan aktif", "stat-icon-green"),
                new StatCard(Icons.CHECK, "Selesai", FormatUtil.number(stats.completed()),
                        "Telah dibaca", "stat-icon-red"));
        lastRead = stats.lastRead();
        renderLastRead();
        renderRecommendations(stats);
    }

    private void renderLastRead() {
        if (lastRead == null) {
            lastReadTitle.setText("Belum ada bacaan terakhir");
            lastReadAuthor.setText("Tambahkan buku ke rak atau mulai membaca untuk melihat progres di sini.");
            lastReadProgressLabel.setText("0%");
            lastReadProgressBar.setProgress(0);
            continueButton.setVisible(false);
            continueButton.setManaged(false);
            setCoverPlaceholder();
            return;
        }
        lastReadTitle.setText(lastRead.judul());
        lastReadAuthor.setText(lastRead.penulis() + "  •  " + lastRead.statusLabel());
        lastReadProgressLabel.setText(lastRead.progressInt() + "% dibaca  •  " + lastRead.halamanLabel());
        lastReadProgressBar.setProgress(lastRead.progressInt() / 100.0);
        continueButton.setVisible(true);
        continueButton.setManaged(true);
        Image image = ImageUtil.loadCover(lastRead.coverPath(), 70, 96);
        if (image == null) {
            setCoverPlaceholder();
        } else {
            ImageView view = new ImageView(image);
            view.setFitWidth(70);
            view.setFitHeight(96);
            view.setPreserveRatio(true);
            lastReadCover.getChildren().setAll(view);
        }
    }

    private void renderRecommendations(ReadingStats stats) {
        recommendationRow.getChildren().clear();
        if (stats.recommendations().isEmpty()) {
            Label empty = new Label("Belum ada rekomendasi buku.");
            empty.getStyleClass().add("muted");
            recommendationRow.getChildren().add(empty);
            return;
        }
        for (BookListItem item : stats.recommendations()) {
            BookCard card = new BookCard(item, "Lihat detail",
                    selected -> Ui.navigate(Route.BOOK_DETAIL,
                            NavigationContext.book(selected.idBook(), Route.MEMBER_DASHBOARD, false)));
            HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
            recommendationRow.getChildren().add(card);
        }
    }

    private void setCoverPlaceholder() {
        lastReadCover.getChildren().setAll(Icons.icon(Icons.BOOK, 24, "cover-placeholder-icon"));
    }

    @FXML
    private void handleContinue() {
        if (lastRead == null) {
            Ui.navigate(Route.CATALOG, NavigationContext.empty());
            return;
        }
        if (lastRead.format().supportsReader()) {
            Ui.navigate(Route.READER,
                    NavigationContext.book(lastRead.idBook(), Route.MEMBER_DASHBOARD, false));
        } else {
            Ui.navigate(Route.BOOK_DETAIL,
                    NavigationContext.book(lastRead.idBook(), Route.MEMBER_DASHBOARD, false));
        }
    }

    @FXML
    private void handleBrowse() {
        Ui.navigate(Route.CATALOG, NavigationContext.empty());
    }

    @FXML
    private void handleViewAll() {
        Ui.navigate(Route.CATALOG, NavigationContext.empty());
    }
}
