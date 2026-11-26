package com.perpustakaan.view;

import com.perpustakaan.util.FormatUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.function.IntConsumer;

/**
 * Navigasi halaman tanpa scrollbar.
 */
public class PaginationBar extends HBox {

    public PaginationBar(int page, int totalPages, long totalItems, IntConsumer onPage) {
        getStyleClass().add("pagination-bar");
        setAlignment(Pos.CENTER);
        setSpacing(10);

        Button previous = new Button();
        previous.setGraphic(Icons.icon(Icons.CHEVRON_LEFT, 16));
        previous.getStyleClass().addAll("btn-icon", "btn-ghost");
        previous.setDisable(page <= 1);
        previous.setOnAction(event -> onPage.accept(page - 1));

        Button next = new Button();
        next.setGraphic(Icons.icon(Icons.CHEVRON_RIGHT, 16));
        next.getStyleClass().addAll("btn-icon", "btn-ghost");
        next.setDisable(totalPages == 0 || page >= totalPages);
        next.setOnAction(event -> onPage.accept(page + 1));

        Label pageLabel = new Label(totalPages == 0
                ? "Tidak ada data"
                : "Halaman " + page + " dari " + totalPages);
        pageLabel.getStyleClass().add("pagination-page");

        Label totalLabel = new Label(FormatUtil.number(totalItems) + " data");
        totalLabel.getStyleClass().add("pagination-total");

        getChildren().addAll(previous, pageLabel, next, totalLabel);
    }
}
