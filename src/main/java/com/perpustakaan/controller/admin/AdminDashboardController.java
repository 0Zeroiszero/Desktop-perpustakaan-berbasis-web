package com.perpustakaan.controller.admin;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.Publisher;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Dashboard admin: ringkasan koleksi dan kartu lima item teratas.
 */
public class AdminDashboardController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private Label welcomeSubtitle;
    @FXML private HBox statsRow;
    @FXML private GridPane summaryGrid;
    @FXML private Button addBookButton;
    @FXML private Button manageUsersButton;
    @FXML private Button openCatalogButton;

    @FXML
    private void initialize() {
        addBookButton.setGraphic(Icons.icon(Icons.PLUS, 15));
        manageUsersButton.setGraphic(Icons.icon(Icons.USERS, 15));
        openCatalogButton.setGraphic(Icons.icon(Icons.LIBRARY, 15));
    }

    @Override
    public void onShow() {
        super.onShow();
        welcomeLabel.setText(FormatUtil.greeting() + ", " + currentUser().getNamaLengkap() + ".");
        welcomeSubtitle.setText("Berikut ringkasan koleksi perpustakaan.");
        load();
    }

    private void load() {
        runAsync(() -> app.getDashboardService().admin(currentUser()), stats -> {
            statsRow.getChildren().setAll(
                    new StatCard(Icons.BOOK, "Total Buku", FormatUtil.number(stats.totalBooks()),
                            "Koleksi terdaftar", "stat-icon-purple"),
                    new StatCard(Icons.USERS, "Anggota", FormatUtil.number(stats.totalMembers()),
                            "Akun terdaftar", "stat-icon-blue"),
                    new StatCard(Icons.TAGS, "Kategori", FormatUtil.number(stats.totalCategories()),
                            "Rak koleksi", "stat-icon-green"),
                    new StatCard(Icons.USER, "Penulis", FormatUtil.number(stats.totalAuthors()),
                            "Kreator buku", "stat-icon-red"));
        });
        runAsync(() -> app.getDashboardService().summary(currentUser()), this::renderSummary);
    }

    private void renderSummary(com.perpustakaan.service.DashboardService.Summary summary) {
        summaryGrid.getChildren().clear();
        summaryGrid.add(card("Buku terbaru", summary.books().stream()
                .map(b -> row(b.judul(), FormatUtil.year(b.tahunTerbit()))).toList()), 0, 0);
        summaryGrid.add(card("Genre terbanyak", summary.genres().stream()
                .map(g -> row(g.getNamaKategori(), FormatUtil.number(g.getJumlahBuku()) + " buku")).toList()), 1, 0);
        summaryGrid.add(card("Penulis terbanyak", summary.authors().stream()
                .map(a -> row(a.getNamaLengkap(), FormatUtil.number(a.getJumlahBuku()) + " buku")).toList()), 0, 1);
        summaryGrid.add(card("Penerbit terbanyak", summary.publishers().stream()
                .map(p -> row(p.getNama(), FormatUtil.number(p.getJumlahBuku()) + " buku")).toList()), 1, 1);
    }

    private VBox card(String title, List<HBox> rows) {
        VBox card = new VBox(8);
        card.getStyleClass().add("table-card");
        Label heading = new Label(title);
        heading.getStyleClass().add("dashboard-section-title");
        card.getChildren().add(heading);
        if (rows.isEmpty()) {
            Label empty = new Label("Belum ada data.");
            empty.getStyleClass().add("muted");
            card.getChildren().add(empty);
        } else {
            card.getChildren().addAll(rows);
        }
        return card;
    }

    private HBox row(String name, String detail) {
        Label label = new Label(name);
        label.getStyleClass().add("summary-name");
        label.setMaxWidth(260);
        label.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label count = new Label(detail);
        count.getStyleClass().add("summary-count");
        HBox row = new HBox(8, label, spacer, count);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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
