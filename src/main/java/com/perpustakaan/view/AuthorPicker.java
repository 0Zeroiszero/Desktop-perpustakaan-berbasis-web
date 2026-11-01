package com.perpustakaan.view;

import com.perpustakaan.model.Author;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pemilih penulis dengan pencarian dan paginasi internal (tanpa scrollbar).
 */
public class AuthorPicker extends VBox {

    private static final int PAGE_SIZE = 6;

    private final List<Author> allAuthors;
    private final Set<String> selectedIds;
    private final TextField searchField = new TextField();
    private final GridPane grid = new GridPane();
    private final HBox paginationBox = new HBox();
    private int page = 1;

    public AuthorPicker(List<Author> authors, List<String> selectedAuthorIds) {
        this.allAuthors = authors == null ? List.of() : new ArrayList<>(authors);
        this.selectedIds = new LinkedHashSet<>();
        if (selectedAuthorIds != null) {
            this.selectedIds.addAll(selectedAuthorIds);
        }

        getStyleClass().add("author-picker");
        setSpacing(8);

        searchField.setPromptText("Cari penulis...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            page = 1;
            render();
        });

        Button selectAll = new Button("Pilih semua");
        selectAll.getStyleClass().addAll("btn-ghost", "btn-sm");
        selectAll.setOnAction(event -> {
            for (Author author : filteredAuthors()) {
                selectedIds.add(author.getIdAuthor());
            }
            render();
        });

        Button clear = new Button("Bersihkan");
        clear.getStyleClass().addAll("btn-ghost", "btn-sm");
        clear.setOnAction(event -> {
            selectedIds.clear();
            render();
        });

        HBox tools = new HBox(8, searchField, selectAll, clear);
        tools.setAlignment(Pos.CENTER_LEFT);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        grid.getColumnConstraints().addAll(left, right);
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setMaxWidth(Double.MAX_VALUE);

        paginationBox.setAlignment(Pos.CENTER);

        getChildren().addAll(tools, grid, paginationBox);
        render();
    }

    public List<String> getSelectedAuthorIds() {
        return List.copyOf(selectedIds);
    }

    private void render() {
        List<Author> filtered = filteredAuthors();
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (totalPages == 0) {
            page = 1;
        } else {
            page = Math.max(1, Math.min(page, totalPages));
        }

        grid.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("Penulis tidak ditemukan.");
            empty.getStyleClass().add("muted");
            grid.add(empty, 0, 0, 2, 1);
        } else {
            int from = (page - 1) * PAGE_SIZE;
            int to = Math.min(from + PAGE_SIZE, filtered.size());
            for (int i = from; i < to; i++) {
                Author author = filtered.get(i);
                CheckBox checkBox = new CheckBox(author.getNamaLengkap()
                        + "  (" + author.getJumlahBuku() + " buku)");
                checkBox.setWrapText(true);
                checkBox.setMaxWidth(Double.MAX_VALUE);
                checkBox.getStyleClass().add("author-check");
                checkBox.setSelected(selectedIds.contains(author.getIdAuthor()));
                checkBox.selectedProperty().addListener((observable, oldValue, selected) -> {
                    if (selected) {
                        selectedIds.add(author.getIdAuthor());
                    } else {
                        selectedIds.remove(author.getIdAuthor());
                    }
                });
                int index = i - from;
                GridPane.setHgrow(checkBox, Priority.ALWAYS);
                grid.add(checkBox, index % 2, index / 2);
            }
        }

        paginationBox.getChildren().setAll(
                new PaginationBar(page, totalPages, filtered.size(), newPage -> {
                    page = newPage;
                    render();
                }));
        paginationBox.setVisible(totalPages > 0);
        paginationBox.setManaged(totalPages > 0);
    }

    private List<Author> filteredAuthors() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return allAuthors;
        }
        List<Author> result = new ArrayList<>();
        for (Author author : allAuthors) {
            String haystack = (author.getNamaLengkap() + " " + safe(author.getEmail()) + " "
                    + safe(author.getNegaraAsal())).toLowerCase(Locale.ROOT);
            if (haystack.contains(query)) {
                result.add(author);
            }
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
