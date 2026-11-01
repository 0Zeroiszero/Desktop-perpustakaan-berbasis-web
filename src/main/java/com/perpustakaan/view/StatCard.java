package com.perpustakaan.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Kartu statistik ringkas.
 */
public class StatCard extends HBox {

    public StatCard(String iconPath, String label, String value, String hint, String accentClass) {
        getStyleClass().add("stat-card");
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14));
        setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this, Priority.ALWAYS);

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().addAll("stat-icon", accentClass);
        iconBox.setMinSize(42, 42);
        iconBox.setPrefSize(42, 42);
        iconBox.setMaxSize(42, 42);
        iconBox.getChildren().add(Icons.icon(iconPath, 21, "stat-icon-glyph"));

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-label");
        VBox text = new VBox(1, valueLabel, labelText);
        if (hint != null && !hint.isBlank()) {
            Label hintLabel = new Label(hint);
            hintLabel.getStyleClass().add("stat-hint");
            text.getChildren().add(hintLabel);
        }
        HBox.setHgrow(text, Priority.ALWAYS);
        getChildren().addAll(iconBox, text);
    }
}
