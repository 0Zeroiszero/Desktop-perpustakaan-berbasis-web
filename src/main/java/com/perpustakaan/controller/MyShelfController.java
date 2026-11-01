package com.perpustakaan.controller;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.model.ReadingStatus;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.EmptyState;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.PaginationBar;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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

/**
 * Rak baca pribadi anggota dengan progres dan aksi lanjut membaca.
 */
public class MyShelfController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button refreshButton;
    @FXML private Button browseButton;
    @FXML private Label resultLabel;
    @FXML private TableView<BookListItem> table;
    @FXML private TableColumn<BookListItem, BookListItem> titleColumn;
    @FXML private TableColumn<BookListItem, String> categoryColumn;
    @FXML private TableColumn<BookListItem, BookListItem> progressColumn;
    @FXML private TableColumn<BookListItem, ReadingStatus> statusColumn;
    @FXML private TableColumn<BookListItem, String> lastReadColumn;
    @FXML private TableColumn<BookListItem, BookListItem> actionsColumn;
    @FXML private HBox paginationBox;

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
    private int currentPage = 1;

    @FXML
    private void initialize() {
        configureTable(table);
        refreshButton.setGraphic(Icons.icon(Icons.SEARCH, 16));
        browseButton.setGraphic(Icons.icon(Icons.LIBRARY, 15));
        sortCombo.getItems().setAll("Terakhir dibaca", "Judul A-Z", "Progres tertinggi");
        sortCombo.getSelectionModel().selectFirst();
        titleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        titleColumn.setCellFactory(column -> new TitleCell());
        categoryColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().kategori()));
        progressColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        progressColumn.setCellFactory(column -> new ProgressCell());
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().status()));
        statusColumn.setCellFactory(column -> new StatusCell());
        lastReadColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.date(cell.getValue().lastRead())));
        actionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        actionsColumn.setCellFactory(column -> new ActionCell());
        table.setPlaceholder(new Label("Memuat rak baca..."));
        searchDelay.setOnFinished(event -> load(1));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchDelay.playFromStart());
        sortCombo.valueProperty().addListener((observable, oldValue, newValue) -> load(1));
    }

    @Override
    public void onShow() {
        super.onShow();
        load(1);
    }

    private String selectedSort() {
        String value = sortCombo.getValue();
        if (value == null) {
            return "terakhir";
        }
        return switch (value) {
            case "Judul A-Z" -> "judul_az";
            case "Progres tertinggi" -> "progres_desc";
            default -> "terakhir";
        };
    }

    private void load(int page) {
        currentPage = Math.max(1, page);
        runAsync(() -> app.getReadingProgressService().shelf(currentUser(), searchField.getText(),
                selectedSort(), currentPage, app.getConfig().getShelfPageSize()), this::applyResult);
    }

    private void applyResult(PageResult<BookListItem> result) {
        table.getItems().setAll(result.items());
        resultLabel.setText(FormatUtil.number(result.totalItems()) + " buku di rak");
        paginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::load));
        if (result.items().isEmpty()) {
            table.setPlaceholder(new EmptyState(Icons.BOOKMARK, "Rak baca masih kosong",
                    "Tambahkan buku dari katalog, lalu lanjutkan membaca kapan saja.",
                    "Jelajahi Katalog", this::handleBrowse));
        } else {
            table.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    private void openDetail(BookListItem item) {
        Ui.navigate(Route.BOOK_DETAIL, NavigationContext.book(item.idBook(), Route.MY_SHELF, false));
    }

    private void continueReading(BookListItem item) {
        if (item.format().supportsReader()) {
            Ui.navigate(Route.READER, NavigationContext.book(item.idBook(), Route.MY_SHELF, false));
        } else {
            openDetail(item);
        }
    }

    private void remove(BookListItem item) {
        if (!confirm("Hapus dari Rak", "Hapus \"" + item.judul() + "\" dari rak baca? Progres membaca akan dihapus.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getReadingProgressService().removeFromShelf(currentUser(), item.idBook());
            success("Buku dihapus dari rak.");
            load(currentPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML
    private void handleBrowse() {
        Ui.navigate(Route.CATALOG, NavigationContext.empty());
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        sortCombo.getSelectionModel().selectFirst();
        load(1);
    }

    private final class TitleCell extends TableCell<BookListItem, BookListItem> {
        @Override
        protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Image image = ImageUtil.loadCover(item.coverPath(), 34, 46);
            ImageView cover;
            if (image == null) {
                cover = null;
            } else {
                cover = new ImageView(image);
                cover.setFitWidth(34);
                cover.setFitHeight(46);
                cover.setPreserveRatio(true);
            }
            Label title = new Label(item.judul());
            title.getStyleClass().add("table-book-title");
            title.setWrapText(true);
            title.setMaxWidth(250);
            Label subtitle = new Label(item.penulis());
            subtitle.getStyleClass().add("table-book-subtitle");
            VBox text = new VBox(2, title, subtitle);
            HBox box = new HBox(9);
            box.setAlignment(Pos.CENTER_LEFT);
            if (cover != null) {
                box.getChildren().add(cover);
            } else {
                box.getChildren().add(Icons.icon(Icons.BOOK, 20, "cover-placeholder-icon"));
            }
            box.getChildren().add(text);
            setGraphic(box);
        }
    }

    private static final class ProgressCell extends TableCell<BookListItem, BookListItem> {
        @Override
        protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            ProgressBar bar = new ProgressBar(item.progressInt() / 100.0);
            bar.setPrefWidth(100);
            Label label = new Label(item.progressInt() + "%");
            label.getStyleClass().add("table-progress-label");
            VBox box = new VBox(3, bar, label);
            setGraphic(box);
        }
    }

    private static final class StatusCell extends TableCell<BookListItem, ReadingStatus> {
        @Override
        protected void updateItem(ReadingStatus item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label badge = new Label(item.databaseValue());
            badge.getStyleClass().addAll("badge", item.styleClass());
            setGraphic(badge);
        }
    }

    private final class ActionCell extends TableCell<BookListItem, BookListItem> {
        private final Button continueButton = new Button();
        private final Button detailButton = new Button();
        private final Button removeButton = new Button();
        private final HBox box = new HBox(3, continueButton, detailButton, removeButton);

        private ActionCell() {
            continueButton.setGraphic(Icons.icon(Icons.ARROW_RIGHT, 14));
            continueButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            continueButton.setTooltip(new Tooltip("Lanjutkan membaca"));
            detailButton.setGraphic(Icons.icon(Icons.INFO, 14));
            detailButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            detailButton.setTooltip(new Tooltip("Lihat detail"));
            removeButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            removeButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            removeButton.setTooltip(new Tooltip("Hapus dari rak"));
            box.setAlignment(Pos.CENTER);
            continueButton.setOnAction(event -> {
                BookListItem item = getItem();
                if (item != null) {
                    continueReading(item);
                }
            });
            detailButton.setOnAction(event -> {
                BookListItem item = getItem();
                if (item != null) {
                    openDetail(item);
                }
            });
            removeButton.setOnAction(event -> {
                BookListItem item = getItem();
                if (item != null) {
                    remove(item);
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
