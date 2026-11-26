package com.perpustakaan.controller.admin;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.model.PageResult;
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
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Label resultLabel;
    @FXML private TableView<User> table;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, User> actionsColumn;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;

    @FXML
    private void initialize() {
        configureTable(table);
        addButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        refreshButton.setGraphic(Icons.icon(Icons.SEARCH, 16));
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNamaLengkap()));
        emailColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEmail()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getRole().displayName()));
        roleColumn.setCellFactory(column -> new RoleCell());
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().isActive() ? "Aktif" : "Nonaktif"));
        statusColumn.setCellFactory(column -> new StatusCell());
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

    private void load(int page) {
        currentPage = Math.max(1, page);
        runAsync(() -> app.getUserService().list(currentUser(), searchField.getText(),
                currentPage, app.getConfig().getAdminPageSize()), this::applyResult);
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
                "Tambah Pengguna", 700, 520,
                controller -> controller.configure(null),
                UserFormController::save,
                UserFormController::saveDisabledProperty);
        if (saved) {
            success("Pengguna berhasil ditambahkan.");
            load(1);
        }
    }

    private void edit(User user) {
        boolean saved = FormDialogUtil.show("/fxml/admin/user-form.fxml", window(table),
                "Ubah Pengguna", 700, 520,
                controller -> controller.configure(user),
                UserFormController::save,
                UserFormController::saveDisabledProperty);
        if (saved) {
            success("Pengguna berhasil diperbarui.");
            load(currentPage);
        }
    }

    private void toggleActive(User user) {
        boolean nextState = !user.isActive();
        String action = nextState ? "Aktifkan" : "Nonaktifkan";
        String detail = nextState
                ? "Aktifkan kembali akun \"" + user.getNamaLengkap() + "\"? Pengguna dapat login kembali."
                : "Nonaktifkan akun \"" + user.getNamaLengkap() + "\"? Data dan progres membaca tetap disimpan, tetapi pengguna tidak dapat login.";
        if (!confirm(action + " Pengguna", detail, action, !nextState)) return;
        try {
            app.getUserService().setActive(currentUser(), user.getIdUser(), nextState);
            success("Pengguna berhasil " + (nextState ? "diaktifkan." : "dinonaktifkan."));
            load(currentPage);
        } catch (RuntimeException e) { handleError(e); }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
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

    private static final class StatusCell extends TableCell<User, String> {
        @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }
            Label badge = new Label(item);
            badge.getStyleClass().addAll("badge", "Aktif".equals(item) ? "badge-green" : "badge-red");
            setGraphic(badge);
        }
    }

    private final class ActionCell extends TableCell<User, User> {
        private final Button editButton = new Button();
        private final Button activeButton = new Button();
        private final HBox box = new HBox(4, editButton, activeButton);

        private ActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah pengguna"));
            activeButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                User item = getItem();
                if (item != null) {
                    edit(item);
                }
            });
            activeButton.setOnAction(event -> {
                User item = getItem();
                if (item != null) toggleActive(item);
            });
        }

        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }
            activeButton.setText(item.isActive() ? "Nonaktifkan" : "Aktifkan");
            activeButton.setTooltip(new Tooltip(item.isActive() ? "Nonaktifkan pengguna" : "Aktifkan kembali pengguna"));
            activeButton.getStyleClass().removeAll("btn-danger-ghost", "btn-success-ghost");
            activeButton.getStyleClass().add(item.isActive() ? "btn-danger-ghost" : "btn-success-ghost");
            setGraphic(box);
        }
    }
}
