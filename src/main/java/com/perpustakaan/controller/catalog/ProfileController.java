package com.perpustakaan.controller.catalog;

import com.perpustakaan.controller.BaseController;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.service.ProfileService;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.view.BookCard;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Halaman profil penulis, penerbit, atau genre. Jenis dan ID dibawa lewat
 * NavigationContext.profile(id, jenis, origin).
 */
public class ProfileController extends BaseController {

    @FXML private Button backButton;
    @FXML private Label jenisLabel;
    @FXML private Label namaLabel;
    @FXML private Label identitasLabel;
    @FXML private Label keteranganLabel;
    @FXML private Label jumlahLabel;
    @FXML private Label bioLabel;
    @FXML private VBox booksBox;

    @FXML
    private void initialize() {
        backButton.setGraphic(Icons.icon(Icons.BACK, 16));
    }

    @Override
    public void onShow() {
        super.onShow();
        String type = context().message();
        String id = context().bookId();
        Route origin = context().origin() == null ? Route.CATALOG : context().origin();
        if (type == null || id == null) {
            Ui.error("Profil tidak dipilih.");
            navigate(origin, NavigationContext.empty());
            return;
        }
        runAsync(() -> app.getProfileService().load(currentUser(), type, id), info -> {
            if (!isActive()) {
                return;
            }
            jenisLabel.setText(info.jenis());
            namaLabel.setText(info.nama());
            identitasLabel.setText(info.identitas());
            keteranganLabel.setText(info.keterangan() == null ? "" : info.keterangan());
            bioLabel.setText(info.bio() == null || info.bio().isBlank() ? "" : info.bio());
            jumlahLabel.setText(FormatUtil.number(info.jumlahBuku()) + " buku");
            booksBox.getChildren().clear();
            if (info.books().isEmpty()) {
                booksBox.getChildren().add(new Label("Belum ada buku pada profil ini."));
                return;
            }
            for (BookListItem item : info.books()) {
                booksBox.getChildren().add(new BookCard(item, "Lihat detail",
                        selected -> navigate(Route.BOOK_DETAIL,
                                NavigationContext.book(selected.idBook(), Route.CATALOG, false))));
            }
        });
    }

    @FXML
    private void handleBack() {
        Route origin = context().origin() == null ? Route.CATALOG : context().origin();
        navigate(origin, NavigationContext.empty());
    }
}
