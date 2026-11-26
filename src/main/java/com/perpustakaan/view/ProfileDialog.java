package com.perpustakaan.view;

import com.perpustakaan.controller.catalog.ProfileController;
import com.perpustakaan.service.ProfileService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

/**
 * Popup profil penulis/penerbit/genre dari saran di form buku.
 */
public final class ProfileDialog {

    private ProfileDialog() {
    }

    public static void show(Window owner, ProfileService.ProfileInfo info) {
        URL resource = ProfileDialog.class.getResource("/fxml/catalog/profile.fxml");
        if (resource == null) {
            throw new IllegalStateException("FXML profil tidak ditemukan.");
        }
        FXMLLoader loader = new FXMLLoader(resource);
        Parent content;
        try {
            content = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Gagal memuat profil.", e);
        }
        ((ProfileController) loader.getController()).configure(info);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Profil " + info.jenis());
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        DialogUtil.configureFormDialog(dialog, owner, 720, 520);
        DialogUtil.stylePrimaryButton(dialog.getDialogPane(), ButtonType.CLOSE, false);
        dialog.showAndWait();
    }
}
