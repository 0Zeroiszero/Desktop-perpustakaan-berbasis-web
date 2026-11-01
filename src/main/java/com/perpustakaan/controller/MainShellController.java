package com.perpustakaan.controller;

import com.perpustakaan.model.Role;
import com.perpustakaan.model.User;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.RootNavigator;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Toast;
import com.perpustakaan.view.ToastType;
import com.perpustakaan.view.ViewAware;
import com.perpustakaan.view.ViewLifecycle;
import com.perpustakaan.view.ViewLoader;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Shell utama: sidebar, header, konten, toast, dan busy overlay.
 */
public class MainShellController extends BaseController {

    @FXML private BorderPane shellRoot;
    @FXML private VBox sidebar;
    @FXML private HBox header;
    @FXML private StackPane centerStack;
    @FXML private StackPane contentPane;
    @FXML private VBox toastBox;
    @FXML private StackPane busyLayer;
    @FXML private Label busyLabel;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label avatarLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardButton;
    @FXML private Button catalogButton;
    @FXML private Button booksButton;
    @FXML private Button categoriesButton;
    @FXML private Button authorsButton;
    @FXML private Button usersButton;
    @FXML private Button shelfButton;
    @FXML private Button logoutButton;

    private RootNavigator navigator;
    private Object currentController;
    private Route currentRoute;

    @FXML
    private void initialize() {
        setIcon(dashboardButton, Icons.DASHBOARD);
        setIcon(catalogButton, Icons.LIBRARY);
        setIcon(booksButton, Icons.BOOK);
        setIcon(categoriesButton, Icons.TAGS);
        setIcon(authorsButton, Icons.USER);
        setIcon(usersButton, Icons.USERS);
        setIcon(shelfButton, Icons.BOOKMARK);
        setIcon(logoutButton, Icons.LOGOUT);
        busyLayer.setVisible(false);
        busyLayer.setManaged(false);
        toastBox.setPickOnBounds(false);
        toastBox.setAlignment(Pos.TOP_RIGHT);
        if (contentPane != null) {
            contentPane.setPickOnBounds(true);
        }
    }

    private void setIcon(Button button, String path) {
        if (button == null) {
            return;
        }
        button.setGraphic(Icons.icon(path, 18));
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(11);
        button.setAlignment(Pos.CENTER_LEFT);
    }

    public void setNavigator(RootNavigator navigator) {
        this.navigator = navigator;
    }

    public void initializeShell() {
        User user = session.requireAuthenticated();
        refreshUserChip();
        boolean admin = user.isAdmin();
        setVisibleManaged(booksButton, admin);
        setVisibleManaged(categoriesButton, admin);
        setVisibleManaged(authorsButton, admin);
        setVisibleManaged(usersButton, admin);
        setVisibleManaged(shelfButton, !admin);
        setImmersive(false);
    }

    private void setVisibleManaged(Button button, boolean visible) {
        if (button == null) {
            return;
        }
        button.setVisible(visible);
        button.setManaged(visible);
    }

    public void refreshUserChip() {
        User user = session.currentUser().orElse(null);
        if (user == null) {
            return;
        }
        avatarLabel.setText(user.getInisial());
        userNameLabel.setText(user.getNamaLengkap());
        userRoleLabel.setText(user.getRole().displayName());
        userRoleLabel.getStyleClass().removeAll("role-admin", "role-member");
        userRoleLabel.getStyleClass().add(user.getRole() == Role.ADMIN ? "role-admin" : "role-member");
    }

    public void navigateTo(Route route, NavigationContext context) {
        if (route == null) {
            route = session.isAdmin() ? Route.ADMIN_DASHBOARD : Route.MEMBER_DASHBOARD;
        }
        if (route.adminOnly() && !session.isAdmin()) {
            showToast("Akses ditolak. Halaman ini hanya untuk admin.", ToastType.ERROR);
            return;
        }
        try {
            ViewLoader.LoadedView<Object> loaded = ViewLoader.load(route);
            if (currentController instanceof ViewLifecycle lifecycle) {
                lifecycle.onHide();
            }
            contentPane.getChildren().setAll(loaded.root());
            currentController = loaded.controller();
            currentRoute = route;
            if (loaded.controller() instanceof ViewAware aware) {
                aware.setContext(context == null ? NavigationContext.empty() : context);
            }
            if (loaded.controller() instanceof ViewLifecycle lifecycle) {
                lifecycle.onShow();
            }
            pageTitle.setText(route.title());
            pageSubtitle.setText(route.subtitle());
            updateActiveNav(route);
            setImmersive(route.immersive());
        } catch (RuntimeException e) {
            AppLogger.error("Gagal membuka halaman " + route.name(), e);
            showToast("Gagal membuka halaman. Periksa log aplikasi.", ToastType.ERROR);
        }
    }

    private void updateActiveNav(Route route) {
        List<Button> buttons = List.of(dashboardButton, catalogButton, booksButton,
                categoriesButton, authorsButton, usersButton, shelfButton);
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("active");
            }
        }
        Button active = switch (route) {
            case ADMIN_DASHBOARD, MEMBER_DASHBOARD -> dashboardButton;
            case CATALOG, BOOK_DETAIL -> catalogButton;
            case BOOKS -> booksButton;
            case CATEGORIES -> categoriesButton;
            case AUTHORS -> authorsButton;
            case USERS -> usersButton;
            case MY_SHELF -> shelfButton;
            default -> null;
        };
        if (active != null) {
            active.getStyleClass().add("active");
        }
    }

    public void setImmersive(boolean immersive) {
        shellRoot.setLeft(immersive ? null : sidebar);
        shellRoot.setTop(immersive ? null : header);
        centerStack.getStyleClass().remove("immersive");
        if (immersive) {
            centerStack.getStyleClass().add("immersive");
        }
    }

    public void showToast(String message, ToastType type) {
        Toast.show(toastBox, message, type);
    }

    public void setBusy(boolean busy, String message) {
        busyLayer.setVisible(busy);
        busyLayer.setManaged(busy);
        if (message != null) {
            busyLabel.setText(message);
        }
    }

    public void dispose() {
        if (currentController instanceof ViewLifecycle lifecycle) {
            lifecycle.onHide();
        }
        currentController = null;
        contentPane.getChildren().clear();
    }

    @FXML
    private void handleDashboard() {
        navigateTo(session.isAdmin() ? Route.ADMIN_DASHBOARD : Route.MEMBER_DASHBOARD,
                NavigationContext.empty());
    }

    @FXML
    private void handleCatalog() {
        navigateTo(Route.CATALOG, NavigationContext.empty());
    }

    @FXML
    private void handleBooks() {
        navigateTo(Route.BOOKS, NavigationContext.empty());
    }

    @FXML
    private void handleCategories() {
        navigateTo(Route.CATEGORIES, NavigationContext.empty());
    }

    @FXML
    private void handleAuthors() {
        navigateTo(Route.AUTHORS, NavigationContext.empty());
    }

    @FXML
    private void handleUsers() {
        navigateTo(Route.USERS, NavigationContext.empty());
    }

    @FXML
    private void handleShelf() {
        navigateTo(Route.MY_SHELF, NavigationContext.empty());
    }

    @FXML
    private void handleLogout() {
        if (!confirm("Keluar dari Aplikasi", "Anda yakin ingin keluar dari akun ini?",
                "Keluar", false)) {
            return;
        }
        session.logout();
        navigate(Route.LOGIN, NavigationContext.empty());
    }
}
