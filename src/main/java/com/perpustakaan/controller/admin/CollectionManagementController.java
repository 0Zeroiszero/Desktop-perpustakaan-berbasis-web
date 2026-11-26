package com.perpustakaan.controller.admin;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.PageResult;
import com.perpustakaan.service.AuthorService;
import com.perpustakaan.service.KategoriService;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.DialogUtil;
import com.perpustakaan.view.EmptyState;
import com.perpustakaan.view.FormDialogUtil;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.PaginationBar;
import com.perpustakaan.view.Ui;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/**
 * Satu halaman admin untuk mengelola buku, penulis, dan kategori.
 */
public class CollectionManagementController extends BaseController {

    @FXML private TextField bookSearchField;
    @FXML private ComboBox<Kategori> bookCategoryCombo;
    @FXML private ComboBox<String> bookSortCombo;
    @FXML private Button bookAddButton;
    @FXML private Label bookResultLabel;
    @FXML private TableView<BookListItem> booksTable;
    @FXML private TableColumn<BookListItem, BookListItem> coverColumn;
    @FXML private TableColumn<BookListItem, BookListItem> bookTitleColumn;
    @FXML private TableColumn<BookListItem, String> bookAuthorColumn;
    @FXML private TableColumn<BookListItem, String> bookCategoryColumn;
    @FXML private TableColumn<BookListItem, String> pagesColumn;
    @FXML private TableColumn<BookListItem, BookListItem> bookActionsColumn;
    @FXML private HBox bookPaginationBox;

    @FXML private TextField authorSearchField;
    @FXML private Button authorAddButton;
    @FXML private Label authorResultLabel;
    @FXML private TableView<Author> authorsTable;
    @FXML private TableColumn<Author, String> authorNameColumn;
    @FXML private TableColumn<Author, String> countryColumn;
    @FXML private TableColumn<Author, String> authorBooksColumn;
    @FXML private TableColumn<Author, Author> authorActionsColumn;
    @FXML private HBox authorPaginationBox;

    @FXML private TextField kategoriSearchField;
    @FXML private Button kategoriAddButton;
    @FXML private Label kategoriResultLabel;
    @FXML private TableView<Kategori> kategoriTable;
    @FXML private TableColumn<Kategori, String> kategoriNamaColumn;
    @FXML private TableColumn<Kategori, String> kategoriDeskripsiColumn;
    @FXML private TableColumn<Kategori, String> kategoriJumlahColumn;
    @FXML private TableColumn<Kategori, Kategori> kategoriActionsColumn;
    @FXML private HBox kategoriPaginationBox;

    private final PauseTransition bookSearchDelay = new PauseTransition(Duration.millis(300));
    private final PauseTransition authorSearchDelay = new PauseTransition(Duration.millis(300));
    private final PauseTransition kategoriSearchDelay = new PauseTransition(Duration.millis(300));
    private int bookPage = 1;
    private int authorPage = 1;
    private int kategoriPage = 1;

    @FXML
    private void initialize() {
        configureTable(booksTable);
        configureTable(authorsTable);
        configureTable(kategoriTable);
        bookAddButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        authorAddButton.setGraphic(Icons.icon(Icons.PLUS, 16));
        kategoriAddButton.setGraphic(Icons.icon(Icons.PLUS, 16));

        bookSortCombo.getItems().setAll("Terbaru", "Judul A-Z", "Tahun terbaru", "Penulis A-Z");
        bookSortCombo.getSelectionModel().selectFirst();
        coverColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        coverColumn.setCellFactory(column -> new CoverCell());
        bookTitleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        bookTitleColumn.setCellFactory(column -> new BookTitleCell());
        bookAuthorColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().penulis()));
        bookCategoryColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().kategori()));
        pagesColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.pages(cell.getValue().jumlahHalaman())));
        bookActionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        bookActionsColumn.setCellFactory(column -> new BookActionCell());

        authorNameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNamaLengkap()));
        countryColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getNegaraAsal() == null || cell.getValue().getNegaraAsal().isBlank()
                        ? "-" : cell.getValue().getNegaraAsal()));
        authorBooksColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.number(cell.getValue().getJumlahBuku())));
        authorBooksColumn.setStyle("-fx-alignment: CENTER;");
        authorActionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        authorActionsColumn.setCellFactory(column -> new AuthorActionCell());

        kategoriNamaColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNamaKategori()));
        kategoriDeskripsiColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getDeskripsi() == null || cell.getValue().getDeskripsi().isBlank()
                        ? "-" : cell.getValue().getDeskripsi()));
        kategoriJumlahColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                FormatUtil.number(cell.getValue().getJumlahBuku())));
        kategoriJumlahColumn.setStyle("-fx-alignment: CENTER;");
        kategoriActionsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        kategoriActionsColumn.setCellFactory(column -> new KategoriActionCell());

        bookSearchDelay.setOnFinished(event -> loadBooks(1));
        authorSearchDelay.setOnFinished(event -> loadAuthors(1));
        kategoriSearchDelay.setOnFinished(event -> loadKategori(1));
        bookSearchField.textProperty().addListener((o, a, b) -> bookSearchDelay.playFromStart());
        authorSearchField.textProperty().addListener((o, a, b) -> authorSearchDelay.playFromStart());
        kategoriSearchField.textProperty().addListener((o, a, b) -> kategoriSearchDelay.playFromStart());
        bookCategoryCombo.valueProperty().addListener((o, a, b) -> loadBooks(1));
        bookSortCombo.valueProperty().addListener((o, a, b) -> loadBooks(1));
        loadBookCategories();
    }

    @Override
    public void onShow() {
        super.onShow();
        loadBooks(1);
        loadAuthors(1);
        loadKategori(1);
    }

    private void loadBookCategories() {
        try {
            List<Kategori> categories = app.getEbookService().categories(currentUser());
            bookCategoryCombo.getItems().setAll(new Kategori(null, "Semua kategori", null, 0));
            bookCategoryCombo.getItems().addAll(categories);
            bookCategoryCombo.getSelectionModel().selectFirst();
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private String selectedCategoryId() {
        Kategori value = bookCategoryCombo.getValue();
        return value == null ? null : value.getIdKategori();
    }

    private String selectedBookSort() {
        String value = bookSortCombo.getValue();
        if (value == null) return "terbaru";
        return switch (value) {
            case "Judul A-Z" -> "judul_az";
            case "Tahun terbaru" -> "tahun_desc";
            case "Penulis A-Z" -> "penulis_az";
            default -> "terbaru";
        };
    }

    private void loadBooks(int page) {
        bookPage = Math.max(1, page);
        runAsync(() -> app.getEbookService().listAdmin(currentUser(), bookSearchField.getText(),
                selectedCategoryId(), selectedBookSort(), bookPage, app.getConfig().getAdminPageSize()),
                this::applyBooks);
    }

    private void applyBooks(PageResult<BookListItem> result) {
        booksTable.getItems().setAll(result.items());
        bookResultLabel.setText(FormatUtil.number(result.totalItems()) + " buku ditemukan");
        bookPaginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::loadBooks));
        if (result.items().isEmpty()) {
            booksTable.setPlaceholder(new EmptyState(Icons.BOOK, "Belum ada buku",
                    "Tambahkan buku pertama untuk mulai mengisi katalog perpustakaan.",
                    "Tambah Buku", this::handleAddBook));
        } else {
            booksTable.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    private void loadAuthors(int page) {
        authorPage = Math.max(1, page);
        runAsync(() -> app.getAuthorService().list(currentUser(), authorSearchField.getText(),
                authorPage, app.getConfig().getAdminPageSize()), this::applyAuthors);
    }

    private void applyAuthors(PageResult<Author> result) {
        authorsTable.getItems().setAll(result.items());
        authorResultLabel.setText(FormatUtil.number(result.totalItems()) + " penulis ditemukan");
        authorPaginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::loadAuthors));
        if (result.items().isEmpty()) {
            authorsTable.setPlaceholder(new EmptyState(Icons.USER, "Belum ada penulis",
                    "Tambahkan penulis agar setiap buku memiliki identitas kreator yang jelas.",
                    "Tambah Penulis", this::handleAddAuthor));
        } else {
            authorsTable.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    private void loadKategori(int page) {
        kategoriPage = Math.max(1, page);
        runAsync(() -> app.getKategoriService().list(currentUser(), kategoriSearchField.getText(),
                kategoriPage, app.getConfig().getAdminPageSize()), this::applyKategori);
    }

    private void applyKategori(PageResult<Kategori> result) {
        kategoriTable.getItems().setAll(result.items());
        kategoriResultLabel.setText(FormatUtil.number(result.totalItems()) + " kategori ditemukan");
        kategoriPaginationBox.getChildren().setAll(new PaginationBar(result.page(), result.totalPages(),
                result.totalItems(), this::loadKategori));
        if (result.items().isEmpty()) {
            kategoriTable.setPlaceholder(new EmptyState(Icons.TAGS, "Belum ada kategori",
                    "Tambahkan kategori pertama untuk mulai menata koleksi perpustakaan.",
                    "Tambah Kategori", this::handleAddKategori));
        } else {
            kategoriTable.setPlaceholder(new Label("Tidak ada data pada halaman ini."));
        }
    }

    @FXML private void handleAddBook() {
        boolean saved = FormDialogUtil.show("/fxml/admin/book-form.fxml", window(bookAddButton),
                "Tambah Buku", 900, 650,
                controller -> controller.configure(null),
                BookFormController::save,
                BookFormController::saveDisabledProperty);
        if (saved) {
            success("Buku berhasil ditambahkan.");
            loadBooks(1);
        }
    }

    private void editBook(BookListItem item) {
        runAsync(() -> app.getEbookService().detail(currentUser(), item.idBook()), detail -> {
            boolean saved = FormDialogUtil.show("/fxml/admin/book-form.fxml", window(booksTable),
                    "Ubah Buku", 900, 650,
                    controller -> controller.configure(detail),
                    BookFormController::save,
                    BookFormController::saveDisabledProperty);
            if (saved) {
                success("Buku berhasil diperbarui.");
                loadBooks(bookPage);
            }
        });
    }

    private void toggleBook(BookListItem item) {
        boolean activate = !item.isActive();
        String aksi = activate ? "Aktifkan" : "Nonaktifkan";
        if (!confirm(aksi + " Buku",
                "Buku \"" + item.judul() + "\" akan " + aksi.toLowerCase()
                        + ". Progres membaca tidak dihapus.", aksi, !activate)) {
            return;
        }
        try {
            app.getEbookService().setActive(currentUser(), item.idBook(), activate);
            success("Buku berhasil " + aksi.toLowerCase() + ".");
            loadBooks(bookPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML private void handleAddAuthor() {
        editAuthor(null);
    }

    private void editAuthor(Author author) {
        TextField namaField = new TextField(author == null ? "" : safe(author.getNamaLengkap()));
        TextField negaraField = new TextField(author == null ? "" : safe(author.getNegaraAsal()));
        TextArea bioArea = new TextArea(author == null ? "" : safe(author.getBio()));
        bioArea.setPrefRowCount(3);
        bioArea.setWrapText(true);
        VBox content = new VBox(7,
                new Label("Nama lengkap *"), namaField,
                new Label("Negara asal"), negaraField,
                new Label("Bio"), bioArea);
        content.setPadding(new Insets(14));
        boolean saved = showEditor(author == null ? "Tambah Penulis" : "Ubah Penulis",
                window(authorAddButton), content, namaField);
        if (!saved) return;
        try {
            AuthorService.AuthorFormData data = new AuthorService.AuthorFormData(
                    namaField.getText().trim(), negaraField.getText().trim(), bioArea.getText());
            if (author == null) {
                app.getAuthorService().create(currentUser(), data);
                success("Penulis berhasil ditambahkan.");
                loadAuthors(1);
            } else {
                app.getAuthorService().update(currentUser(), author.getIdAuthor(), data);
                success("Penulis berhasil diperbarui.");
                loadAuthors(authorPage);
            }
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private void deleteAuthor(Author author) {
        if (!confirm("Hapus Penulis",
                "Hapus penulis \"" + author.getNamaLengkap() + "\"? Penulis yang masih terhubung dengan buku tidak dapat dihapus.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getAuthorService().delete(currentUser(), author.getIdAuthor());
            success("Penulis berhasil dihapus.");
            loadAuthors(authorPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    @FXML private void handleAddKategori() {
        editKategori(null);
    }

    private void editKategori(Kategori kategori) {
        TextField namaField = new TextField(kategori == null ? "" : safe(kategori.getNamaKategori()));
        TextArea deskripsiArea = new TextArea(kategori == null ? "" : safe(kategori.getDeskripsi()));
        deskripsiArea.setPrefRowCount(3);
        deskripsiArea.setWrapText(true);
        VBox content = new VBox(7,
                new Label("Nama kategori *"), namaField,
                new Label("Deskripsi"), deskripsiArea);
        content.setPadding(new Insets(14));
        boolean saved = showEditor(kategori == null ? "Tambah Kategori" : "Ubah Kategori",
                window(kategoriAddButton), content, namaField);
        if (!saved) return;
        try {
            KategoriService.KategoriFormData data = new KategoriService.KategoriFormData(
                    namaField.getText().trim(), deskripsiArea.getText());
            if (kategori == null) {
                app.getKategoriService().create(currentUser(), data);
                success("Kategori berhasil ditambahkan.");
                loadKategori(1);
            } else {
                app.getKategoriService().update(currentUser(), kategori.getIdKategori(), data);
                success("Kategori berhasil diperbarui.");
                loadKategori(kategoriPage);
            }
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private void deleteKategori(Kategori kategori) {
        if (!confirm("Hapus Kategori",
                "Hapus kategori \"" + kategori.getNamaKategori() + "\"? Kategori yang masih dipakai buku tidak dapat dihapus.",
                "Hapus", true)) {
            return;
        }
        try {
            app.getKategoriService().delete(currentUser(), kategori.getIdKategori());
            success("Kategori berhasil dihapus.");
            loadKategori(kategoriPage);
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private boolean showEditor(String title, javafx.stage.Window owner, VBox content, TextField required) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        DialogUtil.configureFormDialog(dialog, owner, 560, 400);
        DialogUtil.stylePrimaryButton(dialog.getDialogPane(), ButtonType.OK, false);
        boolean confirmed = dialog.showAndWait().filter(type -> type == ButtonType.OK).isPresent();
        if (confirmed && required.getText().isBlank()) {
            Ui.error("Nama wajib diisi.");
            return false;
        }
        return confirmed;
    }

    @FXML private void handleRefreshBooks() {
        bookSearchField.clear();
        bookCategoryCombo.getSelectionModel().selectFirst();
        bookSortCombo.getSelectionModel().selectFirst();
        loadBooks(1);
    }

    @FXML private void handleRefreshAuthors() {
        authorSearchField.clear();
        loadAuthors(1);
    }

    @FXML private void handleRefreshKategori() {
        kategoriSearchField.clear();
        loadKategori(1);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private final class CoverCell extends TableCell<BookListItem, BookListItem> {
        @Override protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }
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

    private static final class BookTitleCell extends TableCell<BookListItem, BookListItem> {
        @Override protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }
            Label title = new Label(item.judul());
            title.getStyleClass().add("table-book-title");
            title.setWrapText(true);
            title.setMaxWidth(230);
            String keterangan = item.idBook() + "  •  " + FormatUtil.year(item.tahunTerbit());
            if (!item.isActive()) {
                keterangan += "  •  Nonaktif";
                title.getStyleClass().add("muted");
            }
            Label subtitle = new Label(keterangan);
            subtitle.getStyleClass().add("table-book-subtitle");
            setGraphic(new VBox(2, title, subtitle));
        }
    }

    private final class BookActionCell extends TableCell<BookListItem, BookListItem> {
        private final Button editButton = new Button();
        private final Button toggleButton = new Button();
        private final HBox box = new HBox(4, editButton, toggleButton);

        private BookActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah buku"));
            toggleButton.getStyleClass().addAll("btn-ghost", "btn-sm");
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> { if (getItem() != null) editBook(getItem()); });
            toggleButton.setOnAction(event -> { if (getItem() != null) toggleBook(getItem()); });
        }

        @Override protected void updateItem(BookListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                toggleButton.setText(item.isActive() ? "Nonaktifkan" : "Aktifkan");
                toggleButton.getStyleClass().removeAll("btn-danger-ghost", "btn-success-ghost");
                toggleButton.getStyleClass().add(item.isActive() ? "btn-danger-ghost" : "btn-success-ghost");
            }
            setGraphic(empty || item == null ? null : box);
        }
    }

    private final class AuthorActionCell extends TableCell<Author, Author> {
        private final Button editButton = new Button();
        private final Button deleteButton = new Button();
        private final HBox box = new HBox(4, editButton, deleteButton);

        private AuthorActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah penulis"));
            deleteButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            deleteButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            deleteButton.setTooltip(new Tooltip("Hapus penulis"));
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> { if (getItem() != null) editAuthor(getItem()); });
            deleteButton.setOnAction(event -> { if (getItem() != null) deleteAuthor(getItem()); });
        }

        @Override protected void updateItem(Author item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }

    private final class KategoriActionCell extends TableCell<Kategori, Kategori> {
        private final Button editButton = new Button();
        private final Button deleteButton = new Button();
        private final HBox box = new HBox(4, editButton, deleteButton);

        private KategoriActionCell() {
            editButton.setGraphic(Icons.icon(Icons.EDIT, 14));
            editButton.getStyleClass().addAll("btn-icon", "btn-ghost");
            editButton.setTooltip(new Tooltip("Ubah kategori"));
            deleteButton.setGraphic(Icons.icon(Icons.TRASH, 14));
            deleteButton.getStyleClass().addAll("btn-icon", "btn-ghost", "btn-danger-ghost");
            deleteButton.setTooltip(new Tooltip("Hapus kategori"));
            box.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> { if (getItem() != null) editKategori(getItem()); });
            deleteButton.setOnAction(event -> { if (getItem() != null) deleteKategori(getItem()); });
        }

        @Override protected void updateItem(Kategori item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
