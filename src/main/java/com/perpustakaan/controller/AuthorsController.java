package com.perpustakaan.controller;

import com.perpustakaan.model.Author;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.util.FormatUtil;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Halaman admin untuk mengelola penulis.
 */
public class AuthorsController extends BaseController {

    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Label resultLabel;
    @FXML private TableView<Author> table;
    @FXML private TableColumn<Author, String> nameColumn;
    @FXML private TableColumn<Author, String> emailColumn;
    @FXML private TableColumn<Author, String> countryColumn;
    @FXML private TableColumn<Author, String> booksColumn;
    @FXML private TableColumn<Author, Author> actionsColumn;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;

    @FXML
    private void initialize() {
        configureTable(table);
        addButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        refreshButton.setGraphic(Icons.icon(Icons.SEARCH, 16));
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNamaLengkap()));
        emailColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getEmail() == null || cell.getValue().getEmail().isBlank()
                        ? "-" : cell.getValue().getEmail()));
        countryColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getNegaraAsal() == null || cell.getValue().getNegaraAsal().isBlank()
                        ? "-" : cell.getValue().getNegaraAsal()));
        booksColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.number(cell.getValue().getJumlahBuku())));
        booksColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        actionsColumn.setCellFactory(column -> new ActionCell());
        table.setPlaceholder(new Label("Memuat penulis..."));
        searchDelay.setOnFinished(event -> load(1));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchDelay.playFromStart());
    }

    @Override
    public void onShow() {
        super.onShow();
        load(1);
    }

    private void load(int page) {
        currentPage = Math.max(1, page);
        runAsync(() -> app.getAuthorService().list(currentUser(), searchField.getText(),
                currentPage, app.getConfig().getAdminPageSize()), this::applyResult);
    }

    private void applyResult(PageResult<Author> result) {
        table.getItems().setAll(result.items());
        resultLabel.setText(FormatUtil.number(result.totalItems()) + " penulis ditemukan");
        paginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::load));
        if (result.items().isEmpty()) {
            table.setPlaceholder(new EmptyState(Icons.USER, "Belum ada penulis",
                    "Tambahkan penulis agar setiap buku memiliki identitas kreator yang jelas.",
                    "Tambah Penulis", this::handleAdd));
        } else {
            table.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    @FXML
    private void handleAdd() {
        boolean saved = FormDialogUtil.show("/fxml/admin/author-form.fxml", window(addButton),
                "Tambah Penulis", 620, 510,
                controller -> controller.configure(null),
                AuthorFormController::save);
        if (saved) {
            success("Penulis berhasil ditambahkan.");
            load(1);
        }
    }

    private void edit(Author author) {
        boolean saved = FormDialogUtil.show("/fxml/admin/author-form.fxml", window(table),
                "Ubah Penulis", 620, 510,
                controller -> controller.configure(author),
                AuthorFormController::save);
        if (saved) {
            success("Penulis berhasil diperbarui.");
            load(currentPage);
        }
    }

    private void delete(Author author) {
        if (!confirm("Hapus Penulis",
                "Hapus penulis \"" + author.getNamaLengkap() + "\"? Penulis yang masih terhubung dengan buku tidak dapat dihapus.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getAuthorService().delete(currentUser(), author.getIdAuthor());
            success("Penulis berhasil dihapus.");
            load(currentPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        load(1);
    }

    private final class ActionCell extends TableCell<Author, Author> {
        private final Button editButton = new Button();
        private final Button deleteButton = new Button();
        private final HBox box = new HBox(4, editButton, deleteButton);

        private ActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah penulis"));
            deleteButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            deleteButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            deleteButton.setTooltip(new Tooltip("Hapus penulis"));
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                Author item = getItem();
                if (item != null) {
                    edit(item);
                }
            });
            deleteButton.setOnAction(event -> {
                Author item = getItem();
                if (item != null) {
                    delete(item);
                }
            });
        }

        @Override
        protected void updateItem(Author item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
