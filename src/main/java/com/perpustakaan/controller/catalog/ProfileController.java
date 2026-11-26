package com.perpustakaan.controller.catalog;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.service.ProfileService;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.Icons;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Isi popup profil penulis, penerbit, atau genre.
 */
public class ProfileController {

    private static final double THUMB_WIDTH = 38;
    private static final double THUMB_HEIGHT = 52;

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
            Label empty = new Label("Belum ada buku pada profil ini.");
            empty.getStyleClass().add("muted");
            booksBox.getChildren().add(empty);
            return;
        }
        for (BookListItem item : info.books()) {
            booksBox.getChildren().add(bookRow(item));
        }
    }

    private HBox bookRow(BookListItem item) {
        ImageView thumb = new ImageView();
        thumb.setFitWidth(THUMB_WIDTH);
        thumb.setFitHeight(THUMB_HEIGHT);
        thumb.setPreserveRatio(true);
        thumb.setSmooth(true);
        thumb.getStyleClass().add("profile-thumb");
        Image cover = ImageUtil.loadCover(item.coverPath(), THUMB_WIDTH, THUMB_HEIGHT);
        if (cover != null) {
            thumb.setImage(cover);
        }

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label title = new Label(item.judul());
        title.getStyleClass().add("profile-book-title");
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.setMaxWidth(300);
        Label year = new Label(FormatUtil.year(item.tahunTerbit()));
        year.getStyleClass().add("profile-book-year");
        text.getChildren().addAll(title, year);

        HBox row = new HBox(10, thumb, text);
        row.getStyleClass().add("profile-book-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
