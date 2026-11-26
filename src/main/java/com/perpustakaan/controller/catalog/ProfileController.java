package com.perpustakaan.controller.catalog;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.service.ProfileService;
import com.perpustakaan.util.FormatUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
            Label name = new Label("• " + item.judul());
            name.getStyleClass().add("summary-name");
            name.setMaxWidth(320);
            name.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label meta = new Label(FormatUtil.year(item.tahunTerbit()));
            meta.getStyleClass().add("summary-count");
            HBox row = new HBox(8, name, spacer, meta);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            booksBox.getChildren().add(row);
        }
    }
}
