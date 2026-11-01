package com.perpustakaan.view;

import com.perpustakaan.config.AppConfig;
import com.perpustakaan.config.AppContext;
import com.perpustakaan.controller.MainShellController;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.security.SessionManager;
import com.perpustakaan.util.AppLogger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Mengatur perpindahan antara halaman autentikasi dan shell aplikasi.
 */
public class RootNavigator implements UiHost {

    private final Stage stage;
    private final Scene scene;
    private final AppConfig config;
    private final SessionManager session;
    private MainShellController shell;
    private Object currentAuthController;

    public RootNavigator(Stage stage) {
        this.stage = stage;
        this.config = AppConfig.getInstance();
        this.session = AppContext.get().getSessionManager();
        this.scene = new Scene(new StackPane(), config.getWindowWidth(), config.getWindowHeight());
        configureScene();
        configureStage();
        Ui.setHost(this);
    }

    private void configureScene() {
        scene.setFill(javafx.scene.paint.Color.web("#FBFAFF"));
        addStylesheet("/css/theme.css");
        addStylesheet("/css/layout.css");
        addStylesheet("/css/components.css");
        addStylesheet("/css/auth.css");
        addStylesheet("/css/dashboard.css");
        addStylesheet("/css/catalog.css");
        addStylesheet("/css/reader.css");
    }

    private void addStylesheet(String resource) {
        URL url = RootNavigator.class.getResource(resource);
        if (url == null) {
            AppLogger.warn("Stylesheet tidak ditemukan: " + resource, null);
            return;
        }
        scene.getStylesheets().add(url.toExternalForm());
    }

    private void configureStage() {
        stage.setScene(scene);
        stage.setTitle(config.getAppName());
        stage.setWidth(config.getWindowWidth());
        stage.setHeight(config.getWindowHeight());
        stage.setMinWidth(config.getMinWidth());
        stage.setMinHeight(config.getMinHeight());
        stage.centerOnScreen();
    }

    public void start() {
        stage.show();
        navigate(Route.LOGIN, NavigationContext.empty());
    }

    @Override
    public void navigate(Route route, NavigationContext context) {
        Route target = route == null ? Route.LOGIN : route;
        if (target.isAuthPage()) {
            showAuth(target, context);
            return;
        }
        if (!session.isAuthenticated()) {
            showAuth(Route.LOGIN, NavigationContext.message("Silakan masuk terlebih dahulu."));
            return;
        }
        if (target.adminOnly() && !session.isAdmin()) {
            toast("Akses ditolak. Halaman ini hanya untuk admin.", ToastType.ERROR);
            return;
        }
        if (shell == null) {
            ViewLoader.LoadedView<Object> loaded = ViewLoader.load("/fxml/shell/main-shell.fxml");
            if (!(loaded.controller() instanceof MainShellController shellController)) {
                throw new AppException("Controller shell tidak valid.");
            }
            shell = shellController;
            scene.setRoot(loaded.root());
            shell.setNavigator(this);
            shell.initializeShell();
        }
        shell.navigateTo(target, context);
    }

    private void showAuth(Route route, NavigationContext context) {
        if (shell != null) {
            shell.dispose();
            shell = null;
        }
        if (currentAuthController instanceof ViewLifecycle lifecycle) {
            lifecycle.onHide();
        }
        ViewLoader.LoadedView<Object> loaded = ViewLoader.load(route);
        Parent root = loaded.root();
        currentAuthController = loaded.controller();
        scene.setRoot(root);
        if (loaded.controller() instanceof ViewAware aware) {
            aware.setContext(context);
        }
        if (loaded.controller() instanceof ViewLifecycle lifecycle) {
            lifecycle.onShow();
        }
        stage.setTitle(route.title() + " - " + config.getAppName());
    }

    @Override
    public void toast(String message, ToastType type) {
        if (shell != null) {
            shell.showToast(message, type);
        } else {
            AppLogger.info("Notifikasi: " + message);
        }
    }

    @Override
    public void setBusy(boolean busy, String message) {
        if (shell != null) {
            shell.setBusy(busy, message);
        }
    }

    @Override
    public void setImmersive(boolean immersive) {
        if (shell != null) {
            shell.setImmersive(immersive);
        }
    }

    @Override
    public boolean confirm(String title, String message, String confirmText, boolean danger) {
        return ConfirmDialog.show(stage, title, message, confirmText, danger);
    }

    @Override
    public void refreshUser() {
        if (shell != null) {
            shell.refreshUserChip();
        }
    }

    public Stage getStage() {
        return stage;
    }
}
