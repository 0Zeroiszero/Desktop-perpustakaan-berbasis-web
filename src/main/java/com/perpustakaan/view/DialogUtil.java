package com.perpustakaan.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.net.URL;

/**
 * Utilitas tampilan dialog dan stylesheet.
 */
public final class DialogUtil {

    private DialogUtil() {
    }

    public static void apply(DialogPane pane) {
        addStylesheet(pane, "/css/theme.css");
        addStylesheet(pane, "/css/layout.css");
        addStylesheet(pane, "/css/components.css");
        pane.getStyleClass().add("app-dialog");
    }

    public static void configureFormDialog(Dialog<?> dialog, Window owner, double preferredWidth, double preferredHeight) {
        DialogPane pane = dialog.getDialogPane();
        apply(pane);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(preferredWidth, bounds.getWidth() - 80);
        double height = Math.min(preferredHeight, bounds.getHeight() - 80);
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
        pane.setMinWidth(Math.min(680, width));
        pane.setMinHeight(Math.min(460, height));
        pane.setMaxWidth(width);
        pane.setMaxHeight(height);
        dialog.setResizable(true);
    }

    public static void stylePrimaryButton(DialogPane pane, ButtonType type, boolean danger) {
        Button button = (Button) pane.lookupButton(type);
        if (button == null) {
            return;
        }
        button.getStyleClass().removeAll("btn-primary", "btn-danger", "btn-secondary");
        button.getStyleClass().add(danger ? "btn-danger" : "btn-primary");
    }

    public static void styleSecondaryButton(DialogPane pane, ButtonType type) {
        Button button = (Button) pane.lookupButton(type);
        if (button == null) {
            return;
        }
        button.getStyleClass().removeAll("btn-primary", "btn-danger", "btn-secondary");
        button.getStyleClass().add("btn-secondary");
    }

    public static Button getButton(DialogPane pane, ButtonType type) {
        return (Button) pane.lookupButton(type);
    }

    private static void addStylesheet(DialogPane pane, String resource) {
        URL url = DialogUtil.class.getResource(resource);
        if (url != null && !pane.getStylesheets().contains(url.toExternalForm())) {
            pane.getStylesheets().add(url.toExternalForm());
        }
    }
}
