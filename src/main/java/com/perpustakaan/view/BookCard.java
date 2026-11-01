package com.perpustakaan.view;

import com.perpustakaan.model.BookListItem;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

/**
 * Kartu buku reusable untuk katalog, rekomendasi, dan dashboard.
 */
public class BookCard extends VBox {

    private static final double COVER_WIDTH = 74;
    private static final double COVER_HEIGHT = 100;

    public BookCard(BookListItem item, String actionText, Consumer<BookListItem> onAction) {
        getStyleClass().add("book-card");
        setSpacing(8);
        setPadding(new Insets(12));
        setMinHeight(172);
        setPrefHeight(172);
        setMaxHeight(172);
        setMaxWidth(Double.MAX_VALUE);
        setCursor(Cursor.HAND);
        setOnMouseClicked(event -> onAction.accept(item));

        HBox top = new HBox(12);
        top.setAlignment(Pos.TOP_LEFT);
        top.setMaxWidth(Double.MAX_VALUE);

        VBox info = new VBox(4);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label category = new Label(item.kategori());
        category.getStyleClass().addAll("badge", "badge-purple");
        category.setTextOverrun(OverrunStyle.ELLIPSIS);
        category.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(item.judul());
        title.setWrapText(true);
        title.setMaxHeight(38);
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("book-card-title");

        Label author = new Label(item.penulis());
        author.setTextOverrun(OverrunStyle.ELLIPSIS);
        author.setMaxWidth(Double.MAX_VALUE);
        author.getStyleClass().add("book-card-author");

        Label meta = new Label(FormatUtil.year(item.tahunTerbit()) + "  •  "
                + (item.jumlahHalaman() == null ? "-" : FormatUtil.number(item.jumlahHalaman()) + " hlm"));
        meta.getStyleClass().add("book-card-meta");

        info.getChildren().addAll(category, title, author, meta);
        top.getChildren().addAll(createCover(item), info);

        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.CENTER_LEFT);
        if (item.hasProgress()) {
            Label progress = new Label(item.progressInt() + "% dibaca");
            progress.getStyleClass().add("book-card-progress");
            bottom.getChildren().add(progress);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottom.getChildren().add(spacer);

        Button action = new Button(actionText);
        action.getStyleClass().addAll("btn-ghost", "btn-sm");
        action.setOnAction(event -> {
            event.consume();
            onAction.accept(item);
        });
        bottom.getChildren().add(action);

        getChildren().addAll(top, bottom);
    }

    private StackPane createCover(BookListItem item) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("book-cover");
        pane.setMinSize(COVER_WIDTH, COVER_HEIGHT);
        pane.setPrefSize(COVER_WIDTH, COVER_HEIGHT);
        pane.setMaxSize(COVER_WIDTH, COVER_HEIGHT);

        Image image = ImageUtil.loadCover(item.coverPath(), COVER_WIDTH, COVER_HEIGHT);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(COVER_WIDTH);
            imageView.setFitHeight(COVER_HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            Rectangle clip = new Rectangle(COVER_WIDTH, COVER_HEIGHT);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            imageView.setClip(clip);
            pane.getChildren().add(imageView);
        } else {
            pane.getChildren().add(Icons.icon(Icons.BOOK, 26, "cover-placeholder-icon"));
        }
        return pane;
    }
}
