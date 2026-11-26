package com.perpustakaan.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/**
 * Dialog konfirmasi dengan gaya aplikasi.
 */
public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    public static boolean show(Window owner, String title, String message, String confirmText, boolean danger) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        Label content = new Label(message);
        content.setWrapText(true);
        content.setMaxWidth(440);
        content.getStyleClass().add("dialog-message");
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setMinWidth(480);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        DialogUtil.apply(alert.getDialogPane());

        ButtonType confirmType = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(cancelType, confirmType);
        if (owner != null) {
            alert.initOwner(owner);
        }
        DialogUtil.styleSecondaryButton(alert.getDialogPane(), cancelType);
        DialogUtil.stylePrimaryButton(alert.getDialogPane(), confirmType, danger);
        return alert.showAndWait().filter(result -> result == confirmType).isPresent();
    }
}
