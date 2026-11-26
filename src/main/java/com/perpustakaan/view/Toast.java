package com.perpustakaan.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Notifikasi toast di kanan atas.
 */
public final class Toast {

    private Toast() {
    }

    public static void show(VBox host, String message, ToastType type) {
        if (host == null || message == null || message.isBlank()) {
            return;
        }
        if (host.getChildren().size() >= 3) {
            host.getChildren().remove(0);
        }

        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(330);
        text.getStyleClass().add("toast-message");
        HBox.setHgrow(text, Priority.ALWAYS);

        Button close = new Button();
        close.setGraphic(Icons.icon(Icons.CLOSE, 12));
        close.getStyleClass().add("toast-close");
        close.setFocusTraversable(false);

        HBox toast = new HBox(10);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast", type.styleClass());
        toast.setMaxWidth(410);
        toast.setPadding(new Insets(12, 12, 12, 14));
        toast.getChildren().addAll(Icons.icon(type.iconPath(), 18, "toast-icon"), text, close);

        host.getChildren().add(toast);
        toast.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
        fadeIn.setToValue(1);
        PauseTransition pause = new PauseTransition(Duration.seconds(3.8));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(260), toast);
        fadeOut.setToValue(0);
        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.setOnFinished(event -> host.getChildren().remove(toast));
        sequence.play();

        close.setOnAction(event -> {
            sequence.stop();
            host.getChildren().remove(toast);
        });
    }
}
