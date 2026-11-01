package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.model.User;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.ViewAware;
import com.perpustakaan.view.ViewLifecycle;
import com.perpustakaan.view.ViewLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiSmokeTest {

    private static final List<String> FXML = List.of(
            "/fxml/auth/login.fxml",
            "/fxml/auth/register.fxml",
            "/fxml/shell/main-shell.fxml",
            "/fxml/admin/dashboard.fxml",
            "/fxml/admin/books.fxml",
            "/fxml/admin/book-form.fxml",
            "/fxml/admin/categories.fxml",
            "/fxml/admin/kategori-form.fxml",
            "/fxml/admin/authors.fxml",
            "/fxml/admin/author-form.fxml",
            "/fxml/admin/users.fxml",
            "/fxml/admin/user-form.fxml",
            "/fxml/catalog/catalog.fxml",
            "/fxml/catalog/book-detail.fxml",
            "/fxml/member/dashboard.fxml",
            "/fxml/member/my-shelf.fxml",
            "/fxml/reader/reader.fxml"
    );

    private static final List<String> DATA_PAGES = List.of(
            "/fxml/admin/dashboard.fxml",
            "/fxml/admin/books.fxml",
            "/fxml/admin/categories.fxml",
            "/fxml/admin/authors.fxml",
            "/fxml/admin/users.fxml",
            "/fxml/member/dashboard.fxml",
            "/fxml/catalog/catalog.fxml",
            "/fxml/member/my-shelf.fxml"
    );

    @Test
    void allFxmlLoadsStylesApplyAndPagesLoadData() throws Exception {
        AppContext app = AppContext.get();
        User admin = app.getAuthService().login("adminpustaka", "admin123");
        app.getSessionManager().login(admin);

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> {
        });
        Platform.setImplicitExit(false);

        Platform.runLater(() -> {
            try {
                Map<String, ViewLoader.LoadedView<Object>> views = new LinkedHashMap<>();
                for (String path : FXML) {
                    ViewLoader.LoadedView<Object> loaded = ViewLoader.load(path);
                    views.put(path, loaded);
                    Parent root = loaded.root();
                    Scene scene = new Scene(root, 1280, 720);
                    for (String css : List.of("theme", "layout", "components", "auth",
                            "dashboard", "catalog", "reader")) {
                        URL url = UiSmokeTest.class.getResource("/css/" + css + ".css");
                        assertTrue(url != null, "Missing CSS: " + css);
                        scene.getStylesheets().add(url.toExternalForm());
                    }
                    root.applyCss();
                    root.layout();
                    System.out.println("FXML OK " + path + " -> " + loaded.controller().getClass().getSimpleName());
                }

                Parent detail = views.get("/fxml/catalog/book-detail.fxml").root();
                System.out.println("STYLE categoryLabel=" + detail.lookup("#categoryLabel").getStyleClass());
                System.out.println("STYLE formatLabel=" + detail.lookup("#formatLabel").getStyleClass());
                System.out.println("STYLE statusBadge=" + detail.lookup("#statusBadge").getStyleClass());
                Parent shell = views.get("/fxml/shell/main-shell.fxml").root();
                System.out.println("STYLE userRoleLabel=" + shell.lookup("#userRoleLabel").getStyleClass());

                for (String path : DATA_PAGES) {
                    Object controller = views.get(path).controller();
                    if (controller instanceof ViewLifecycle lifecycle) {
                        lifecycle.onShow();
                    }
                }

                Object detailController = views.get("/fxml/catalog/book-detail.fxml").controller();
                if (detailController instanceof ViewAware aware) {
                    aware.setContext(NavigationContext.book("BK001", Route.CATALOG, false));
                }
                if (detailController instanceof ViewLifecycle lifecycle) {
                    lifecycle.onShow();
                }

                Object readerController = views.get("/fxml/reader/reader.fxml").controller();
                if (readerController instanceof ViewAware aware) {
                    aware.setContext(NavigationContext.book("BK001", Route.BOOK_DETAIL, false));
                }
                if (readerController instanceof ViewLifecycle lifecycle) {
                    lifecycle.onShow();
                }

                PauseTransition wait = new PauseTransition(Duration.seconds(10));
                wait.setOnFinished(event -> {
                    try {
                        assertTrue(((TableView<?>) views.get("/fxml/admin/dashboard.fxml").root()
                                .lookup("#recentTable")).getItems().size() > 0,
                                "Dashboard admin tidak memuat aktivitas");
                        assertTrue(((TableView<?>) views.get("/fxml/admin/books.fxml").root()
                                .lookup("#table")).getItems().size() > 0,
                                "Tabel buku kosong");
                        assertTrue(((TableView<?>) views.get("/fxml/admin/categories.fxml").root()
                                .lookup("#table")).getItems().size() > 0,
                                "Tabel kategori kosong");
                        assertTrue(((TableView<?>) views.get("/fxml/admin/authors.fxml").root()
                                .lookup("#table")).getItems().size() > 0,
                                "Tabel penulis kosong");
                        assertTrue(((TableView<?>) views.get("/fxml/admin/users.fxml").root()
                                .lookup("#table")).getItems().size() > 0,
                                "Tabel pengguna kosong");
                        assertTrue(((HBox) views.get("/fxml/member/dashboard.fxml").root()
                                .lookup("#statsRow")).getChildren().size() == 4,
                                "Statistik dashboard anggota tidak lengkap");
                        assertTrue(((GridPane) views.get("/fxml/catalog/catalog.fxml").root()
                                .lookup("#bookGrid")).getChildren().size() > 0,
                                "Grid katalog kosong");
                        assertTrue(((TableView<?>) views.get("/fxml/member/my-shelf.fxml").root()
                                .lookup("#table")).getItems().size() > 0,
                                "Rak baca kosong");

                        Label detailTitle = (Label) views.get("/fxml/catalog/book-detail.fxml").root()
                                .lookup("#titleLabel");
                        assertFalse("Judul Buku".equals(detailTitle.getText()),
                                "Detail buku tidak memuat data");
                        ImageView pageImage = (ImageView) views.get("/fxml/reader/reader.fxml").root()
                                .lookup("#pageImage");
                        assertNotNull(pageImage.getImage(), "Halaman PDF tidak ter-render");
                        System.out.println("DATA OK: semua halaman memuat data; reader merender PDF.");
                    } catch (Throwable error) {
                        failure.set(error);
                    } finally {
                        if (readerController instanceof ViewLifecycle lifecycle) {
                            lifecycle.onHide();
                        }
                        try {
                            app.getReadingProgressService().removeFromShelf(admin, "BK001");
                        } catch (RuntimeException ignored) {
                            // Baris progres uji mungkin memang tidak terbentuk.
                        }
                        done.countDown();
                    }
                });
                wait.play();
            } catch (Throwable t) {
                failure.set(t);
                done.countDown();
            }
        });

        assertTrue(done.await(120, TimeUnit.SECONDS), "UI smoke test timeout");
        Platform.exit();
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }
}
