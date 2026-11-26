package com.perpustakaan.controller.catalog;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.service.ProfileService;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.view.BookCard;
import com.perpustakaan.view.Route;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Isi popup profil penulis, penerbit, atau genre.
 */
public class ProfileController {

    @FXML private Label jenisLabel;
    @FXML private Label namaLabel;
    @FXML private Label identitasLabel;
    @FXML private Label keteranganLabel;
    @FXML private Label jumlahLabel;
    @FXML private Label bioLabel;
    @FXML private VBox booksBox;

    public void configure(ProfileService.ProfileInfo info) {
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
                    selected -> com.perpustakaan.view.Ui.navigate(Route.BOOK_DETAIL,
                            com.perpustakaan.view.NavigationContext.book(selected.idBook(), Route.CATALOG, false))));
        }
    }
}
