package com.perpustakaan.controller;

import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.view.EmptyState;
import com.perpustakaan.view.FormDialogUtil;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.PaginationBar;
import com.perpustakaan.util.FormatUtil;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.List;

/**
 * Halaman admin untuk mengelola kategori.
 */
public class CategoriesController extends BaseController {

    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Label resultLabel;
    @FXML private TableView<Kategori> table;
    @FXML private TableColumn<Kategori, String> namaColumn;
    @FXML private TableColumn<Kategori, String> deskripsiColumn;
    @FXML private TableColumn<Kategori, String> jumlahColumn;
    @FXML private TableColumn<Kategori, Kategori> actionsColumn;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;

    @FXML
    private void initialize() {
        configureTable(table);
        addButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        refreshButton.setGraphic(Icons.icon(Icons.SEARCH, 16));
        namaColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNamaKategori()));
        deskripsiColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getDeskripsi() == null || cell.getValue().getDeskripsi().isBlank()
                        ? "-" : cell.getValue().getDeskripsi()));
        jumlahColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.number(cell.getValue().getJumlahBuku())));
        jumlahColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cell.getValue()));
        actionsColumn.setCellFactory(column -> new ActionCell());
        table.setPlaceholder(new Label("Memuat kategori..."));
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
        runAsync(() -> app.getKategoriService().list(currentUser(), searchField.getText(),
                currentPage, app.getConfig().getAdminPageSize()), result -> {
            applyResult(result);
        });
    }

    private void applyResult(PageResult<Kategori> result) {
        table.getItems().setAll(result.items());
        resultLabel.setText(FormatUtil.number(result.totalItems()) + " kategori ditemukan");
        paginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::load));
        if (result.items().isEmpty()) {
            table.setPlaceholder(new EmptyState(Icons.TAGS, "Belum ada kategori",
                    "Tambahkan kategori pertama untuk mulai menata koleksi perpustakaan.",
                    "Tambah Kategori", this::handleAdd));
        } else {
            table.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    @FXML
    private void handleAdd() {
        boolean saved = FormDialogUtil.show("/fxml/admin/kategori-form.fxml", window(addButton),
                "Tambah Kategori", 560, 430,
                controller -> controller.configure(null),
                KategoriFormController::save);
        if (saved) {
            success("Kategori berhasil ditambahkan.");
            load(1);
        }
    }

    private void edit(Kategori kategori) {
        boolean saved = FormDialogUtil.show("/fxml/admin/kategori-form.fxml", window(table),
                "Ubah Kategori", 560, 430,
                controller -> controller.configure(kategori),
                KategoriFormController::save);
        if (saved) {
            success("Kategori berhasil diperbarui.");
            load(currentPage);
        }
    }

    private void delete(Kategori kategori) {
        if (!confirm("Hapus Kategori",
                "Hapus kategori \"" + kategori.getNamaKategori() + "\"? Kategori yang masih dipakai buku tidak dapat dihapus.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getKategoriService().delete(currentUser(), kategori.getIdKategori());
            success("Kategori berhasil dihapus.");
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

    private final class ActionCell extends TableCell<Kategori, Kategori> {
        private final Button editButton = new Button();
        private final Button deleteButton = new Button();
        private final HBox box = new HBox(4, editButton, deleteButton);

        private ActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new javafx.scene.control.Tooltip("Ubah kategori"));
            deleteButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            deleteButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            deleteButton.setTooltip(new javafx.scene.control.Tooltip("Hapus kategori"));
            box.setAlignment(javafx.geometry.Pos.CENTER);
            editButton.setOnAction(event -> {
                Kategori item = getItem();
                if (item != null) {
                    edit(item);
                }
            });
            deleteButton.setOnAction(event -> {
                Kategori item = getItem();
                if (item != null) {
                    delete(item);
                }
            });
        }

        @Override
        protected void updateItem(Kategori item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
