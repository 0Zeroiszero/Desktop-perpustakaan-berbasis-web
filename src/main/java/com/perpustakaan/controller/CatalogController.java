package com.perpustakaan.controller;

import com.perpustakaan.model.BookFormat;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.view.BookCard;
import com.perpustakaan.view.EmptyState;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.PaginationBar;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.util.List;

/**
 * Katalog e-book untuk anggota.
 */
public class CatalogController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Kategori> categoryCombo;
    @FXML private ComboBox<String> formatCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button resetButton;
    @FXML private Label resultLabel;
    @FXML private GridPane bookGrid;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;
    private boolean categoriesLoaded;

    @FXML
    private void initialize() {
        formatCombo.getItems().setAll("Semua format", "PDF", "EPUB", "MOBI");
        formatCombo.getSelectionModel().selectFirst();
        sortCombo.getItems().setAll("Terbaru", "Judul A-Z", "Tahun terbaru", "Penulis A-Z");
        sortCombo.getSelectionModel().selectFirst();
        resetButton.setGraphic(Icons.icon(Icons.CLOSE, 14));
        searchDelay.setOnFinished(event -> load(1));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchDelay.playFromStart());
        categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
        formatCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
        sortCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
    }

    @Override
    public void onShow() {
        super.onShow();
        if (!categoriesLoaded) {
            loadCategories();
        }
        load(1);
    }

    private void loadCategories() {
        try {
            List<Kategori> categories = app.getEbookService().categories(currentUser());
            categoryCombo.getItems().setAll(new Kategori(null, "Semua kategori", null, 0));
            categoryCombo.getItems().addAll(categories);
            categoryCombo.getSelectionModel().selectFirst();
            categoriesLoaded = true;
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private String selectedCategoryId() {
        Kategori kategori = categoryCombo.getValue();
        return kategori == null ? null : kategori.getIdKategori();
    }

    private BookFormat selectedFormat() {
        String value = formatCombo.getValue();
        if (value == null || "Semua format".equals(value)) {
            return null;
        }
        return BookFormat.valueOf(value);
    }

    private String selectedSort() {
        String value = sortCombo.getValue();
        if (value == null) {
            return "terbaru";
        }
        return switch (value) {
            case "Judul A-Z" -> "judul_az";
            case "Tahun terbaru" -> "tahun_desc";
            case "Penulis A-Z" -> "penulis_az";
            default -> "terbaru";
        };
    }

    private void load(int page) {
        currentPage = Math.max(1, page);
        runAsync(() -> app.getEbookService().listCatalog(currentUser(), searchField.getText(),
                selectedCategoryId(), selectedFormat(), selectedSort(),
                currentPage, app.getConfig().getCatalogPageSize()), this::applyResult);
    }

    private void applyResult(PageResult<BookListItem> result) {
        bookGrid.getChildren().clear();
        resultLabel.setText(FormatUtil.number(result.totalItems()) + " buku ditemukan");
        if (result.items().isEmpty()) {
            EmptyState empty = new EmptyState(Icons.SEARCH, "Buku tidak ditemukan",
                    "Coba ubah kata kunci atau filter. Anda juga dapat mengatur ulang pencarian.",
                    "Atur ulang filter", this::handleReset);
            bookGrid.add(empty, 0, 0, 4, 2);
            GridPane.setHgrow(empty, Priority.ALWAYS);
            GridPane.setVgrow(empty, Priority.ALWAYS);
        } else {
            int column = 0;
            int row = 0;
            for (BookListItem item : result.items()) {
                String action = item.hasProgress() ? "Lanjutkan" : "Lihat detail";
                BookCard card = new BookCard(item, action, selected -> {
                    if (selected.hasProgress() && selected.format().supportsReader()) {
                        Ui.navigate(Route.READER,
                                NavigationContext.book(selected.idBook(), Route.CATALOG, false));
                    } else {
                        Ui.navigate(Route.BOOK_DETAIL,
                                NavigationContext.book(selected.idBook(), Route.CATALOG, false));
                    }
                });
                card.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(card, Priority.ALWAYS);
                GridPane.setVgrow(card, Priority.NEVER);
                bookGrid.add(card, column, row);
                column++;
                if (column == 4) {
                    column = 0;
                    row++;
                }
            }
        }
        paginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::load));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
        formatCombo.getSelectionModel().selectFirst();
        sortCombo.getSelectionModel().selectFirst();
        load(1);
    }
}
