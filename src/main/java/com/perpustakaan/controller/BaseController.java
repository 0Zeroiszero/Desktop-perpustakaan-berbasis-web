package com.perpustakaan.controller;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.exception.AccessDeniedException;
import com.perpustakaan.exception.AppException;
import com.perpustakaan.exception.ValidationException;
import com.perpustakaan.model.User;
import com.perpustakaan.security.SessionManager;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.Async;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import com.perpustakaan.view.ViewAware;
import com.perpustakaan.view.ViewLifecycle;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base seluruh controller halaman.
 */
public abstract class BaseController implements ViewAware, ViewLifecycle {

    protected final AppContext app = AppContext.get();
    protected final SessionManager session = app.getSessionManager();
    private NavigationContext context = NavigationContext.empty();
    private boolean active;

    @Override
    public void setContext(NavigationContext context) {
        this.context = context == null ? NavigationContext.empty() : context;
    }

    protected NavigationContext context() {
        return context;
    }

    @Override
    public void onShow() {
        active = true;
    }

    @Override
    public void onHide() {
        active = false;
    }

    protected boolean isActive() {
        return active;
    }

    protected User currentUser() {
        return session.requireAuthenticated();
    }

    protected Optional<User> currentUserOptional() {
        return session.currentUser();
    }

    protected boolean isAdmin() {
        return session.isAdmin();
    }

    protected void navigate(Route route, NavigationContext context) {
        Ui.navigate(route, context);
    }

    protected void toast(String message, boolean success) {
        if (success) {
            Ui.success(message);
        } else {
            Ui.error(message);
        }
    }

    protected void success(String message) {
        Ui.success(message);
    }

    protected void info(String message) {
        Ui.info(message);
    }

    protected void setBusy(boolean busy, String message) {
        Ui.busy(busy, message);
    }

    protected boolean confirm(String title, String message, String confirmText, boolean danger) {
        return Ui.confirm(title, message, confirmText, danger);
    }

    protected <T> void runAsync(Supplier<T> work, Consumer<T> onSuccess) {
        runAsync(work, onSuccess, this::handleError);
    }

    protected <T> void runAsync(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Async.run(work, result -> {
            if (active) {
                onSuccess.accept(result);
            }
        }, onError);
    }

    public void handleError(Throwable error) {
        if (error == null) {
            Ui.error("Terjadi kesalahan yang tidak diketahui.");
            return;
        }
        Throwable cause = unwrap(error);
        if (cause instanceof ValidationException validation) {
            Ui.error(validation.getMessage());
            return;
        }
        if (cause instanceof AccessDeniedException denied) {
            Ui.error(denied.getMessage());
            return;
        }
        if (cause instanceof AppException appException) {
            AppLogger.error(appException.getMessage(), appException);
            Ui.error(appException.getMessage());
            return;
        }
        AppLogger.error("Kesalahan tak terduga.", cause);
        Ui.error("Terjadi kesalahan. Silakan coba lagi.");
    }

    protected Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null
                && (current instanceof java.util.concurrent.CompletionException
                || current instanceof java.util.concurrent.ExecutionException)) {
            current = current.getCause();
        }
        return current;
    }

    protected void configureTable(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(52);
        table.setFocusTraversable(false);
        table.setPlaceholder(new Label("Memuat data..."));
    }

    protected Window window(Node node) {
        return node == null || node.getScene() == null ? null : node.getScene().getWindow();
    }
}
