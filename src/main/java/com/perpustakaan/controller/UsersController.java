package com.perpustakaan.controller;

import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Halaman admin untuk mengelola pengguna dan role.
 */
public class UsersController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Label resultLabel;
    @FXML private TableView<User> table;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> genderColumn;
    @FXML private TableColumn<User, User> actionsColumn;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;

    @FXML
    private void initialize() {
        configureTable(table);
        addButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        refreshButton.setGraphic(Icons.icon(Icons.SEARCH, 16));
        roleFilterCombo.getItems().setAll("Semua role", "Admin", "Anggota");
        roleFilterCombo.getSelectionModel().selectFirst();
        roleFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNamaLengkap()));
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper("@" + cell.getValue().getUsername()));
        emailColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEmail()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getRole().displayName()));
        roleColumn.setCellFactory(column -> new RoleCell());
        genderColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getJenisKelaminLabel()));
        actionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        actionsColumn.setCellFactory(column -> new ActionCell());
        table.setPlaceholder(new Label("Memuat pengguna..."));
        searchDelay.setOnFinished(event -> load(1));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchDelay.playFromStart());
    }

    @Override
    public void onShow() {
        super.onShow();
        load(1);
    }

    private Role selectedRole() {
        String value = roleFilterCombo.getValue();
        if ("Admin".equals(value)) {
            return Role.ADMIN;
        }
        if ("Anggota".equals(value)) {
            return Role.MEMBER;
        }
        return null;
    }

    private void load(int page) {
        currentPage = Math.max(1, page);
        runAsync(() -> app.getUserService().list(currentUser(), searchField.getText(),
                selectedRole(), currentPage, app.getConfig().getAdminPageSize()), this::applyResult);
    }

    private void applyResult(PageResult<User> result) {
        table.getItems().setAll(result.items());
        resultLabel.setText(FormatUtil.number(result.totalItems()) + " pengguna ditemukan");
        paginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::load));
        if (result.items().isEmpty()) {
            table.setPlaceholder(new EmptyState(Icons.USERS, "Belum ada pengguna",
                    "Tambahkan akun admin atau anggota untuk mengelola akses perpustakaan.",
                    "Tambah Pengguna", this::handleAdd));
        } else {
            table.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    @FXML
    private void handleAdd() {
        boolean saved = FormDialogUtil.show("/fxml/admin/user-form.fxml", window(addButton),
                "Tambah Pengguna", 860, 640,
                controller -> controller.configure(null),
                UserFormController::save);
        if (saved) {
            success("Pengguna berhasil ditambahkan.");
            load(1);
        }
    }

    private void edit(User user) {
        boolean saved = FormDialogUtil.show("/fxml/admin/user-form.fxml", window(table),
                "Ubah Pengguna", 860, 640,
                controller -> controller.configure(user),
                UserFormController::save);
        if (saved) {
            success("Pengguna berhasil diperbarui.");
            load(currentPage);
        }
    }

    private void delete(User user) {
        if (!confirm("Hapus Pengguna",
                "Hapus akun \"" + user.getNamaLengkap() + "\"? Seluruh progres membaca pengguna ini juga akan dihapus.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getUserService().delete(currentUser(), user.getIdUser());
            success("Pengguna berhasil dihapus.");
            load(currentPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        roleFilterCombo.getSelectionModel().selectFirst();
        load(1);
    }

    private static final class RoleCell extends TableCell<User, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label badge = new Label(item);
            badge.getStyleClass().addAll("badge", "Admin".equals(item) ? "badge-purple" : "badge-blue");
            setGraphic(badge);
        }
    }

    private final class ActionCell extends TableCell<User, User> {
        private final Button editButton = new Button();
        private final Button deleteButton = new Button();
        private final HBox box = new HBox(4, editButton, deleteButton);

        private ActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah pengguna"));
            deleteButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            deleteButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            deleteButton.setTooltip(new Tooltip("Hapus pengguna"));
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                User item = getItem();
                if (item != null) {
                    edit(item);
                }
            });
            deleteButton.setOnAction(event -> {
                User item = getItem();
                if (item != null) {
                    delete(item);
                }
            });
        }

        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
