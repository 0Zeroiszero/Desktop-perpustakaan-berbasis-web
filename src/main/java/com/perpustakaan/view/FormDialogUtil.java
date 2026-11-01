package com.perpustakaan.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Menampilkan form FXML di dalam dialog modal dengan tombol simpan/batal.
 */
public final class FormDialogUtil {

    private FormDialogUtil() {
    }

    public static <T> boolean show(String fxmlPath, Window owner, String title,
                                   double width, double height,
                                   Consumer<T> configure, Predicate<T> save) {
        URL resource = FormDialogUtil.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalStateException("Form tidak ditemukan: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        Parent content;
        try {
            content = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Gagal memuat form: " + fxmlPath, e);
        }
        T controller = loader.getController();
        if (configure != null) {
            configure.accept(controller);
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        ButtonType saveType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelType, saveType);
        DialogUtil.configureFormDialog(dialog, owner, width, height);
        DialogUtil.styleSecondaryButton(dialog.getDialogPane(), cancelType);
        DialogUtil.stylePrimaryButton(dialog.getDialogPane(), saveType, false);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!save.test(controller)) {
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button);
        return dialog.showAndWait().filter(result -> result == saveType).isPresent();
    }
}
