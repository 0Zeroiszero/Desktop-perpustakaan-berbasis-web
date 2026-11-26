package com.perpustakaan.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Tampilan kosong dengan aksi opsional.
 */
public class EmptyState extends VBox {

    public EmptyState(String iconPath, String title, String message, String actionText, Runnable onAction) {
        getStyleClass().add("empty-state");
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(28));
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("empty-icon");
        iconBox.setMinSize(58, 58);
        iconBox.setPrefSize(58, 58);
        iconBox.setMaxSize(58, 58);
        iconBox.getChildren().add(Icons.icon(iconPath, 28, "empty-icon-glyph"));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("empty-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(420);
        messageLabel.setAlignment(Pos.CENTER);

        getChildren().addAll(iconBox, titleLabel, messageLabel);
        if (actionText != null && onAction != null) {
            Button action = new Button(actionText);
            action.getStyleClass().add("btn-primary");
            action.setOnAction(event -> onAction.run());
            getChildren().add(action);
        }
    }
}
