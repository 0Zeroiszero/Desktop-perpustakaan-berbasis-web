package com.perpustakaan.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * TextField dengan saran dari daftar yang sudah ada. Saran dipilih dengan klik.
 * Bila saran punya info profil, tombol "!" membuka halaman profil terkait.
 */
public class AutoCompleteTextField extends TextField {

    private static final int MAX_SUGGESTIONS = 8;

    private final ContextMenu suggestions = new ContextMenu();
    private List<Suggestion> entries = List.of();
    private Consumer<Suggestion> profileOpener;

    public AutoCompleteTextField() {
        getStyleClass().add("auto-complete-field");
        suggestions.getStyleClass().add("auto-complete-popup");
        setPromptText("Ketik untuk mencari...");
        textProperty().addListener((observable, oldValue, newValue) -> refreshSuggestions());
        focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) {
                suggestions.hide();
            } else {
                refreshSuggestions();
            }
        });
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                suggestions.hide();
            }
        });
    }

    /** Daftar nilai teks biasa, tanpa profil. */
    public void setSuggestions(List<String> values) {
        this.entries = values == null ? List.of()
                : values.stream().map(value -> new Suggestion(value, null, null)).toList();
        refreshSuggestions();
    }

    /** Daftar saran yang bisa membuka profil lewat tombol "!". */
    public void setProfileSuggestions(List<Suggestion> values) {
        this.entries = values == null ? List.of() : List.copyOf(values);
        refreshSuggestions();
    }

    public void setProfileOpener(Consumer<Suggestion> opener) {
        this.profileOpener = opener;
    }

    private void refreshSuggestions() {
        String query = getText() == null ? "" : getText().trim().toLowerCase(Locale.ROOT);
        List<MenuItem> matches = entries.stream()
                .filter(value -> query.isEmpty()
                        || value.label().toLowerCase(Locale.ROOT).contains(query))
                .limit(MAX_SUGGESTIONS)
                .map(value -> {
                    Label name = new Label(value.label());
                    name.getStyleClass().add("auto-complete-item-label");
                    HBox graphic = new HBox(6, name);
                    if (value.type() != null && value.id() != null && profileOpener != null) {
                        Button info = new Button("!");
                        info.getStyleClass().addAll("btn-icon", "btn-ghost", "auto-complete-info");
                        info.setTooltip(new javafx.scene.control.Tooltip("Lihat profil"));
                        info.setOnAction(event -> {
                            event.consume();
                            suggestions.hide();
                            profileOpener.accept(value);
                        });
                        graphic.getChildren().add(info);
                    }
                    HBox.setHgrow(name, Priority.ALWAYS);
                    MenuItem item = new MenuItem();
                    item.setGraphic(graphic);
                    item.getStyleClass().add("auto-complete-item");
                    item.setOnAction(event -> {
                        setText(value.label());
                        positionCaret(value.label().length());
                    });
                    return item;
                })
                .toList();
        suggestions.getItems().setAll(FXCollections.observableArrayList(matches));
        if (!matches.isEmpty() && isFocused() && getScene() != null) {
            if (!suggestions.isShowing()) {
                suggestions.show(this, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        } else {
            suggestions.hide();
        }
    }

    public record Suggestion(String label, String type, String id) {
    }
}
