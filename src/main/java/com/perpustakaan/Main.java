package com.perpustakaan;

import com.perpustakaan.config.AppPaths;
import com.perpustakaan.config.DatabaseConnection;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.Async;
import com.perpustakaan.view.DialogUtil;
import com.perpustakaan.view.RootNavigator;
import com.perpustakaan.view.Ui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Entry point JavaFX PustakaDigital.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            AppLogger.error("Uncaught exception pada thread " + thread.getName(), error);
            Platform.runLater(() -> Ui.error("Terjadi kesalahan tak terduga. Periksa log aplikasi."));
        });
        try {
            AppPaths.ensureDirectories();
            AppLogger.init();
            AppLogger.info("Memulai PustakaDigital Desktop.");
            DatabaseConnection.testConnection();
            RootNavigator navigator = new RootNavigator(stage);
            Ui.setHost(navigator);
            navigator.start();
        } catch (RuntimeException error) {
            AppLogger.error("Aplikasi gagal dimulai.", error);
            showStartupError(error);
        }
    }

    private void showStartupError(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PustakaDigital - Gagal Dimulai");
        alert.setHeaderText("Aplikasi tidak dapat dimulai");
        alert.setContentText((error.getMessage() == null ? "Terjadi kesalahan." : error.getMessage())
                + "\n\nPastikan MySQL berjalan dan konfigurasi database pada config/database.properties benar.");
        DialogUtil.apply(alert.getDialogPane());
        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void stop() {
        Async.shutdown();
        AppLogger.info("PustakaDigital Desktop ditutup.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
