package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.controller.admin.UserFormController;
import com.perpustakaan.model.User;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.RootNavigator;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.ViewAware;
import com.perpustakaan.view.ViewLifecycle;
import com.perpustakaan.view.ViewLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
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

/**
 * Smoke test GUI: memuat semua FXML, memeriksa validasi form, lalu menjalankan
 * shell aplikasi yang sebenarnya dan berpindah ke semua halaman admin/anggota.
 * Screenshot tiap halaman disimpan ke folder sementara untuk pemeriksaan visual.
 */
class UiSmokeTest {

    private static final List<String> FXML = List.of(
            "/fxml/auth/login.fxml",
            "/fxml/auth/register.fxml",
            "/fxml/shell/main-shell.fxml",
            "/fxml/admin/dashboard.fxml",
            "/fxml/admin/collection-management.fxml",
            "/fxml/admin/book-form.fxml",
            "/fxml/admin/users.fxml",
            "/fxml/admin/user-form.fxml",
            "/fxml/catalog/catalog.fxml",
            "/fxml/catalog/book-detail.fxml",
            "/fxml/catalog/profile.fxml",
            "/fxml/member/dashboard.fxml",
            "/fxml/member/my-shelf.fxml",
            "/fxml/reader/reader.fxml"
    );

    private static final List<String> DATA_PAGES = List.of(
            "/fxml/admin/dashboard.fxml",
            "/fxml/admin/collection-management.fxml",
            "/fxml/admin/users.fxml",
            "/fxml/member/dashboard.fxml",
            "/fxml/catalog/catalog.fxml",
            "/fxml/member/my-shelf.fxml"
    );

    @Test
    void allFxmlLoadsAndGuiFlowWorks() throws Exception {
        AppContext app = AppContext.get();
        User admin = app.getAuthService().login("admin@pustaka.com", "admin123");
        User member = app.getAuthService().login("andi@email.com", "andi123");
        app.getSessionManager().login(admin);

        File shots = new File(System.getProperty("java.io.tmpdir"), "opencode/pustaka-gui-smoke");
        assertTrue(shots.mkdirs() || shots.isDirectory(), "Gagal membuat folder screenshot.");

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        if (System.getProperty("pustaka.fx.started") == null) {
            Platform.startup(() -> {
            });
            System.setProperty("pustaka.fx.started", "true");
        }
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
                        assertNotNull(url, "CSS tidak ditemukan: " + css);
                        scene.getStylesheets().add(url.toExternalForm());
                    }
                    root.applyCss();
                    root.layout();
                    System.out.println("FXML OK " + path + " -> " + loaded.controller().getClass().getSimpleName());
                }

                verifyRequiredFieldStates(views, admin);

                Parent detail = views.get("/fxml/catalog/book-detail.fxml").root();
                assertNotNull(detail.lookup("#categoryLabel"));
                assertNotNull(detail.lookup("#statusChoice"));
                assertNotNull(detail.lookup("#publisherValue"));

                for (String path : DATA_PAGES) {
                    Object controller = views.get(path).controller();
                    if (controller instanceof ViewLifecycle lifecycle) {
                        lifecycle.onShow();
                    }
                }

                Object detailController = views.get("/fxml/catalog/book-detail.fxml").controller();
                if (detailController instanceof ViewAware aware) {
                    aware.setContext(NavigationContext.book("1", Route.CATALOG, false));
                }
                if (detailController instanceof ViewLifecycle lifecycle) {
                    lifecycle.onShow();
                }

                Object readerController = views.get("/fxml/reader/reader.fxml").controller();
                if (readerController instanceof ViewAware aware) {
                    aware.setContext(NavigationContext.book("84", Route.BOOK_DETAIL, false));
                }
                if (readerController instanceof ViewLifecycle lifecycle) {
                    lifecycle.onShow();
                }

                PauseTransition wait = new PauseTransition(Duration.seconds(3));
                wait.setOnFinished(event -> {
                    try {
                        Parent collection = views.get("/fxml/admin/collection-management.fxml").root();
                        assertTrue(((TableView<?>) collection.lookup("#booksTable")).getItems().size() > 0,
                                "Tabel buku kosong");
                        assertTrue(((TableView<?>) collection.lookup("#authorsTable")).getItems().size() > 0,
                                "Tabel penulis kosong");
                        assertTrue(((TableView<?>) collection.lookup("#kategoriTable")).getItems().size() > 0,
                                "Tabel kategori kosong");
                        assertTrue(((TableView<?>) views.get("/fxml/admin/users.fxml").root()
                                .lookup("#table")).getItems().size() > 0, "Tabel pengguna kosong");
                        assertTrue(((HBox) views.get("/fxml/member/dashboard.fxml").root()
                                .lookup("#statsRow")).getChildren().size() == 4,
                                "Statistik dashboard anggota tidak lengkap");
                        assertTrue(((GridPane) views.get("/fxml/catalog/catalog.fxml").root()
                                .lookup("#bookGrid")).getChildren().size() > 0, "Grid katalog kosong");
                        assertTrue(((TableView<?>) views.get("/fxml/member/my-shelf.fxml").root()
                                .lookup("#table")).getItems().size() > 0, "Rak baca kosong");
                        assertFalse("Judul Buku".equals(((Label) views.get("/fxml/catalog/book-detail.fxml")
                                .root().lookup("#titleLabel")).getText()), "Detail buku tidak memuat data");

                        ImageView pageImage = (ImageView) views.get("/fxml/reader/reader.fxml").root()
                                .lookup("#pageImage");
                        assertNotNull(pageImage.getImage(), "PDF Atomic Habits tidak ter-render di reader");
                        System.out.println("DATA OK: semua halaman memuat data; PDF lokal terbaca.");
                        runGuiSteps(new Stage(), null, app, admin, member, shots, done, failure, 0);
                    } catch (Throwable error) {
                        failure.set(error);
                        done.countDown();
                    }
                });
                wait.play();
            } catch (Throwable t) {
                failure.set(t);
                done.countDown();
            }
        });

        assertTrue(done.await(180, TimeUnit.SECONDS), "GUI smoke test timeout");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        System.out.println("GUI OK: screenshot tersimpan di " + shots.getAbsolutePath());
    }

    private static void runGuiSteps(Stage stage, RootNavigator existingNavigator, AppContext app,
                                    User admin, User member, File dir, CountDownLatch done,
                                    AtomicReference<Throwable> failure, int index) {
        List<GuiStep> steps = List.of(
                new GuiStep("01-admin-dashboard", Route.ADMIN_DASHBOARD, NavigationContext.empty(), admin),
                new GuiStep("02-collection", Route.COLLECTION_MANAGEMENT, NavigationContext.empty(), admin),
                new GuiStep("03-users", Route.USERS, NavigationContext.empty(), admin),
                new GuiStep("04-catalog", Route.CATALOG, NavigationContext.empty(), admin),
                new GuiStep("05-detail", Route.BOOK_DETAIL, NavigationContext.book("1", Route.CATALOG, false), admin),
                new GuiStep("06-member-dashboard", Route.MEMBER_DASHBOARD, NavigationContext.empty(), member),
                new GuiStep("07-shelf", Route.MY_SHELF, NavigationContext.empty(), member),
                new GuiStep("08-reader", Route.READER, NavigationContext.book("84", Route.BOOK_DETAIL, false), member)
        );
        if (index >= steps.size()) {
            finishGui(app, admin, done, failure);
            return;
        }
        GuiStep step = steps.get(index);
        try {
            app.getSessionManager().login(step.user());
            RootNavigator navigator = existingNavigator == null ? new RootNavigator(stage) : existingNavigator;
            if (index == 0) {
                navigator.start();
            }
            navigator.navigate(step.route(), step.context());
            PauseTransition wait = new PauseTransition(Duration.seconds(index == steps.size() - 1 ? 3 : 1));
            wait.setOnFinished(event -> {
                try {
                    assertNotNull(stage.getScene(), "Scene tidak ada pada " + step.name());
                    assertNotNull(stage.getScene().getRoot(), "Root tidak ada pada " + step.name());
                    javafx.scene.image.WritableImage image = stage.getScene().snapshot(null);
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(dir, step.name() + ".png"));
                    System.out.println("GUI STEP " + step.name() + " -> " + step.route());
                } catch (Throwable error) {
                    failure.set(error);
                }
                runGuiSteps(stage, navigator, app, admin, member, dir, done, failure, index + 1);
            });
            wait.play();
        } catch (Throwable error) {
            failure.set(error);
            finishGui(app, admin, done, failure);
        }
    }

    private static void finishGui(AppContext app, User admin, CountDownLatch done,
                                  AtomicReference<Throwable> failure) {
        try {
            app.getReadingProgressService().removeFromShelf(admin, "1");
        } catch (RuntimeException ignored) {
            // Tidak ada baris uji yang perlu dibersihkan.
        }
        done.countDown();
    }

    private record GuiStep(String name, Route route, NavigationContext context, User user) {
    }

    private static void verifyRequiredFieldStates(Map<String, ViewLoader.LoadedView<Object>> views, User admin) {
        Parent register = views.get("/fxml/auth/register.fxml").root();
        Button registerButton = (Button) register.lookup("#registerButton");
        assertNotNull(registerButton, "Tombol daftar tidak ditemukan");
        assertTrue(registerButton.isDisabled(), "Tombol daftar harus nonaktif saat form kosong");

        setText(register, "#emailField", "calon@email.com");
        setText(register, "#displayNameField", "Calon Anggota");
        setText(register, "#passwordField", "password123");
        setText(register, "#confirmPasswordField", "password123");
        assertFalse(registerButton.isDisabled(), "Tombol daftar harus aktif setelah kolom wajib terisi");

        setText(register, "#confirmPasswordField", "password124");
        assertTrue(registerButton.isDisabled(), "Tombol daftar harus nonaktif saat konfirmasi tidak sama");
        setText(register, "#confirmPasswordField", "");
        assertTrue(registerButton.isDisabled(), "Tombol daftar harus nonaktif jika konfirmasi kosong");

        Parent userForm = views.get("/fxml/admin/user-form.fxml").root();
        UserFormController userController = (UserFormController) views.get("/fxml/admin/user-form.fxml").controller();
        userController.configure(null);
        assertTrue(userController.saveDisabledProperty().get(), "Simpan pengguna baru harus nonaktif saat kosong");

        setText(userForm, "#emailField", "pengguna@email.com");
        setText(userForm, "#displayNameField", "Pengguna Baru");
        @SuppressWarnings("unchecked")
        ComboBox<?> roleCombo = (ComboBox<?>) userForm.lookup("#roleCombo");
        roleCombo.getSelectionModel().select(1);
        setText(userForm, "#passwordField", "password123");
        assertFalse(userController.saveDisabledProperty().get(), "Simpan pengguna baru harus aktif setelah lengkap");

        userController.configure(admin);
        assertFalse(userController.saveDisabledProperty().get(), "Edit pengguna dengan data lengkap harus aktif");
        assertFalse(((Label) userForm.lookup("#passwordRequiredMark")).isVisible(),
                "Tanda wajib password harus disembunyikan saat edit");
        setText(userForm, "#emailField", "");
        assertTrue(userController.saveDisabledProperty().get(), "Simpan edit harus nonaktif jika email kosong");

        System.out.println("VALIDATION OK: tombol simpan mengikuti kolom wajib dan tanda * konsisten.");
    }

    private static void setText(Parent root, String selector, String value) {
        TextField field = (TextField) root.lookup(selector);
        assertNotNull(field, "Field tidak ditemukan: " + selector);
        field.setText(value);
    }
}
