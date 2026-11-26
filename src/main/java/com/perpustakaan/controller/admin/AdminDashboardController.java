package com.perpustakaan.controller.admin;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.model.DashboardStats;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.view.FormDialogUtil;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.StatCard;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard admin: ringkasan koleksi dan distribusi kategori.
 */
public class AdminDashboardController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private Label welcomeSubtitle;
    @FXML private HBox statsRow;
    @FXML private VBox categoryStatsBox;
    @FXML private Label averageProgressLabel;
    @FXML private ProgressBar averageProgressBar;
    @FXML private Button addBookButton;
    @FXML private Button manageUsersButton;
    @FXML private Button openCatalogButton;

    @FXML
    private void initialize() {
        addBookButton.setGraphic(Icons.icon(Icons.PLUS, 15));
        manageUsersButton.setGraphic(Icons.icon(Icons.USERS, 15));
        openCatalogButton.setGraphic(Icons.icon(Icons.LIBRARY, 15));
        averageProgressBar.setProgress(0);
    }

    @Override
    public void onShow() {
        super.onShow();
        welcomeLabel.setText(FormatUtil.greeting() + ", " + currentUser().getNamaLengkap() + ".");
        welcomeSubtitle.setText("Berikut ringkasan koleksi perpustakaan.");
        load();
    }

    private void load() {
        runAsync(() -> app.getDashboardService().admin(currentUser()), this::applyStats);
    }

    private void applyStats(DashboardStats stats) {
        statsRow.getChildren().setAll(
                new StatCard(Icons.BOOK, "Total Buku", FormatUtil.number(stats.totalBooks()),
                        "Koleksi terdaftar", "stat-icon-purple"),
                new StatCard(Icons.USERS, "Anggota", FormatUtil.number(stats.totalMembers()),
                        "Akun terdaftar", "stat-icon-blue"),
                new StatCard(Icons.TAGS, "Kategori", FormatUtil.number(stats.totalCategories()),
                        "Rak koleksi", "stat-icon-green"),
                new StatCard(Icons.USER, "Penulis", FormatUtil.number(stats.totalAuthors()),
                        "Kreator buku", "stat-icon-red"));
        averageProgressLabel.setText(FormatUtil.percent(stats.averageProgress()));
        averageProgressBar.setProgress(stats.averageProgress()
                .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP).doubleValue());
        renderCategoryStats(stats.categoryStats());
    }

    private void renderCategoryStats(List<DashboardStats.CategoryStat> categories) {
        categoryStatsBox.getChildren().clear();
        if (categories.isEmpty()) {
            Label empty = new Label("Belum ada data kategori.");
            empty.getStyleClass().add("muted");
            categoryStatsBox.getChildren().add(empty);
            return;
        }
        long max = categories.stream().mapToLong(DashboardStats.CategoryStat::bookCount).max().orElse(1);
        for (DashboardStats.CategoryStat stat : categories) {
            Label name = new Label(stat.categoryName());
            name.getStyleClass().add("category-name");
            name.setMaxWidth(150);
            Label count = new Label(FormatUtil.number(stat.bookCount()) + " buku");
            count.getStyleClass().add("category-count");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox header = new HBox(8, name, spacer, count);
            header.setAlignment(Pos.CENTER_LEFT);
            ProgressBar bar = new ProgressBar(max <= 0 ? 0 : (double) stat.bookCount() / max);
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.getStyleClass().add("category-bar");
            VBox row = new VBox(4, header, bar);
            row.getStyleClass().add("category-row");
            categoryStatsBox.getChildren().add(row);
        }
    }

    @FXML
    private void handleAddBook() {
        boolean saved = FormDialogUtil.show("/fxml/admin/book-form.fxml", window(addBookButton),
                "Tambah Buku", 900, 650,
                controller -> controller.configure(null),
                BookFormController::save,
                BookFormController::saveDisabledProperty);
        if (saved) {
            Ui.success("Buku berhasil ditambahkan.");
            load();
        }
    }

    @FXML
    private void handleManageUsers() {
        navigate(Route.USERS, NavigationContext.empty());
    }

    @FXML
    private void handleOpenCatalog() {
        navigate(Route.CATALOG, NavigationContext.empty());
    }
}
