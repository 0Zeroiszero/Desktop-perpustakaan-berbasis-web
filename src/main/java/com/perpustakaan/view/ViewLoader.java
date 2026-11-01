package com.perpustakaan.view;

import com.perpustakaan.exception.AppException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

/**
 * Memuat FXML dan mengembalikan root beserta controller-nya.
 */
public final class ViewLoader {

    private ViewLoader() {
    }

    public static LoadedView<Object> load(Route route) {
        return load(route.fxmlPath());
    }

    public static LoadedView<Object> load(String fxmlPath) {
        URL resource = ViewLoader.class.getResource(fxmlPath);
        if (resource == null) {
            throw new AppException("File tampilan tidak ditemukan: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        try {
            Parent root = loader.load();
            return new LoadedView<>(root, loader.getController());
        } catch (IOException e) {
            throw new AppException("Gagal memuat tampilan: " + fxmlPath, e);
        }
    }

    public record LoadedView<T>(Parent root, T controller) {
    }
}
