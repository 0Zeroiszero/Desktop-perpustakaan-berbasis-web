package com.perpustakaan.controller;

import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.BookFormat;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.EmptyState;
import com.perpustakaan.view.FormDialogUtil;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.PaginationBar;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/**
 * Halaman admin untuk CRUD buku dan unggahan berkas.
 */
public class BooksController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Kategori> categoryCombo;
    @FXML private ComboBox<String> formatCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button refreshButton;
    @FXML private Button addButton;
    @FXML private Label resultLabel;
    @FXML private TableView<BookListItem> table;
    @FXML private TableColumn<BookListItem, BookListItem> coverColumn;
    @FXML private TableColumn<BookListItem, BookListItem> titleColumn;
    @FXML private TableColumn<BookListItem, String> authorColumn;
    @FXML private TableColumn<BookListItem, String> categoryColumn;
    @FXML private TableColumn<BookListItem, String> formatColumn;
    @FXML private TableColumn<BookListItem, String> pagesColumn;
    @FXML private TableColumn<BookListItem, BookListItem> actionsColumn;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;

    @FXML
    private void initialize() {
        configureTable(table);
        addButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        refreshButton.setGraphic(Icons.icon(Icons.SEARCH, 16));
        formatCombo.getItems().setAll("Semua format", "PDF", "EPUB", "MOBI");
        formatCombo.getSelectionModel().selectFirst();
        sortCombo.getItems().setAll("Terbaru", "Judul A-Z", "Tahun terbaru", "Penulis A-Z");
        sortCombo.getSelectionModel().selectFirst();

        coverColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        coverColumn.setCellFactory(column -> new CoverCell());
        titleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        titleColumn.setCellFactory(column -> new TitleCell());
        authorColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().penulis()));
        categoryColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().kategori()));
        formatColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().format().displayName()));
        formatColumn.setCellFactory(column -> new FormatCell());
        pagesColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.pages(cell.getValue().jumlahHalaman())));
        actionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        actionsColumn.setCellFactory(column -> new ActionCell());

        table.setPlaceholder(new Label("Memuat buku..."));
        searchDelay.setOnFinished(event -> load(1));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchDelay.playFromStart());
        categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
        formatCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
        sortCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
        loadCategories();
    }

    @Override
    public void onShow() {
        super.onShow();
        load(1);
    }

    private void loadCategories() {
        try {
            List<Kategori> categories = app.getEbookService().categories(currentUser());
            categoryCombo.getItems().setAll(new Kategori(null, "Semua kategori", null, 0));
            categoryCombo.getItems().addAll(categories);
            categoryCombo.getSelectionModel().selectFirst();
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
        runAsync(() -> app.getEbookService().listAdmin(currentUser(), searchField.getText(),
                selectedCategoryId(), selectedFormat(), selectedSort(),
                currentPage, app.getConfig().getAdminPageSize()), this::applyResult);
    }

    private void applyResult(PageResult<BookListItem> result) {
        table.getItems().setAll(result.items());
        resultLabel.setText(FormatUtil.number(result.totalItems()) + " buku ditemukan");
        paginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::load));
        if (result.items().isEmpty()) {
            table.setPlaceholder(new EmptyState(Icons.BOOK, "Belum ada buku",
                    "Tambahkan e-book pertama untuk mulai mengisi katalog perpustakaan.",
                    "Tambah Buku", this::handleAdd));
        } else {
            table.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    @FXML
    private void handleAdd() {
        boolean saved = FormDialogUtil.show("/fxml/admin/book-form.fxml", window(addButton),
                "Tambah Buku", 900, 650,
                controller -> controller.configure(null),
                BookFormController::save);
        if (saved) {
            success("Buku berhasil ditambahkan.");
            load(1);
        }
    }

    private void edit(BookListItem item) {
        runAsync(() -> app.getEbookService().detail(currentUser(), item.idBook()), detail -> {
            boolean saved = FormDialogUtil.show("/fxml/admin/book-form.fxml", window(table),
                    "Ubah Buku", 900, 650,
                    controller -> controller.configure(detail),
                    BookFormController::save);
            if (saved) {
                success("Buku berhasil diperbarui.");
                load(currentPage);
            }
        });
    }

    private void delete(BookListItem item) {
        if (!confirm("Hapus Buku",
                "Hapus buku \"" + item.judul() + "\"? Progres membaca seluruh pengguna untuk buku ini juga akan dihapus. Tindakan ini tidak dapat dibatalkan.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getEbookService().delete(currentUser(), item.idBook());
            success("Buku berhasil dihapus.");
            load(currentPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        categoryCombo.getSelectionModel().selectFirst();
        formatCombo.getSelectionModel().selectFirst();
        sortCombo.getSelectionModel().selectFirst();
        load(1);
    }

    private final class CoverCell extends TableCell<BookListItem, BookListItem> {
        @Override
        protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Image image = ImageUtil.loadCover(item.coverPath(), 34, 46);
            if (image == null) {
                setGraphic(Icons.icon(Icons.BOOK, 22, "cover-placeholder-icon"));
                return;
            }
            ImageView view = new ImageView(image);
            view.setFitWidth(34);
            view.setFitHeight(46);
            view.setPreserveRatio(true);
            setGraphic(view);
        }
    }

    private static final class TitleCell extends TableCell<BookListItem, BookListItem> {
        @Override
        protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label title = new Label(item.judul());
            title.getStyleClass().add("table-book-title");
            title.setWrapText(true);
            title.setMaxWidth(230);
            Label subtitle = new Label(item.idBook() + "  •  " + FormatUtil.year(item.tahunTerbit()));
            subtitle.getStyleClass().add("table-book-subtitle");
            setGraphic(new VBox(2, title, subtitle));
        }
    }

    private static final class FormatCell extends TableCell<BookListItem, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label badge = new Label(item);
            badge.getStyleClass().addAll("badge", "PDF".equals(item) ? "badge-green" : "badge-gray");
            setGraphic(badge);
        }
    }

    private final class ActionCell extends TableCell<BookListItem, BookListItem> {
        private final Button editButton = new Button();
        private final Button deleteButton = new Button();
        private final HBox box = new HBox(4, editButton, deleteButton);

        private ActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah buku"));
            deleteButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            deleteButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            deleteButton.setTooltip(new Tooltip("Hapus buku"));
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                BookListItem item = getItem();
                if (item != null) {
                    edit(item);
                }
            });
            deleteButton.setOnAction(event -> {
                BookListItem item = getItem();
                if (item != null) {
                    delete(item);
                }
            });
        }

        @Override
        protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
