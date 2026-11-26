package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.config.AppPaths;
import com.perpustakaan.config.DatabaseConnection;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.Jdbc;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.RootNavigator;
import com.perpustakaan.view.Route;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test skenario pengguna sungguhan. Menghitung klik untuk setiap operasi CRUD
 * dan memastikan tidak ada operasi yang butuh lebih dari tiga klik.
 */
class UserScenarioTest {

    private static final int MAX_CLICKS = 3;
    private static final String MARK = "SCENARIO" + System.currentTimeMillis();
    private static final String DUMMY_TITLE = "Buku " + MARK;
    private static String dummyBookId;

    @Test
    void clickCrudScenarios() throws Exception {
        AppContext app = AppContext.get();
        User admin = app.getAuthService().login("admin@pustaka.com", "admin123");
        User member = app.getAuthService().login("andi@email.com", "andi123");

        dummyBookId = app.getEbookService().create(admin, new com.perpustakaan.service.EbookService.BookFormData(
                DUMMY_TITLE, "2026", "1", null, "Penerbit " + MARK, "Genre " + MARK,
                "Penulis " + MARK, "Buku uji skenario", AppPaths.getCoverDir().resolve("1.jpg"),
                AppPaths.getEbookDir().resolve("1.pdf"))).getIdBook();

        File dir = new File(System.getProperty("java.io.tmpdir"), "opencode/pustaka-user-scenarios");
        assertTrue(dir.mkdirs() || dir.isDirectory(), "Gagal membuat folder screenshot.");

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        if (System.getProperty("pustaka.fx.started") == null) {
            Platform.startup(() -> {
            });
            System.setProperty("pustaka.fx.started", "true");
        }
        Platform.setImplicitExit(false);

        Platform.runLater(() -> {
            Flow flow = new Flow(app, admin, member, dir, done, failure);
            try {
                Stage stage = new Stage();
                RootNavigator navigator = new RootNavigator(stage);
                navigator.start();
                flow.stage = stage;
                flow.navigator = navigator;
                app.getSessionManager().login(member);
                navigator.navigate(Route.MEMBER_DASHBOARD, NavigationContext.empty());
                flow.after(700, flow::memberScenarios);
            } catch (Throwable t) {
                failure.set(t);
                done.countDown();
            }
        });

        boolean finished = done.await(120, TimeUnit.SECONDS);
        cleanup();
        assertTrue(finished, "User scenario timeout");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        flowReport();
        System.out.println("SCENARIO OK: screenshot di " + dir.getAbsolutePath());
    }

    private static void flowReport() {
        System.out.println("RINGKASAN KLIK (batas " + MAX_CLICKS + " klik per operasi):");
        for (Map.Entry<String, Integer> e : RESULTS.entrySet()) {
            System.out.printf("  %-28s %d klik  %s%n", e.getKey(), e.getValue(),
                    e.getValue() <= MAX_CLICKS ? "LULUS" : "GAGAL");
        }
    }

    private static final Map<String, Integer> RESULTS = new LinkedHashMap<>();

    private static void cleanup() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            List<Object> title = List.of("Buku " + MARK + "%");
            List<String> ids = Jdbc.queryList(connection, "SELECT book_id FROM books WHERE title LIKE ?",
                    title, rs -> String.valueOf(rs.getInt("book_id")));
            for (String id : ids) {
                Jdbc.update(connection, "DELETE FROM collections WHERE book_id = ?", List.of(Integer.valueOf(id)));
                Jdbc.update(connection, "DELETE FROM reading_progress WHERE book_id = ?", List.of(Integer.valueOf(id)));
                Jdbc.update(connection, "DELETE FROM books WHERE book_id = ?", List.of(Integer.valueOf(id)));
                for (String stored : List.of(AppPaths.ebookStoredPath(id), AppPaths.coverStoredPath(id))) {
                    java.nio.file.Path file = AppPaths.resolveStoredPath(stored);
                    if (file != null) {
                        java.nio.file.Files.deleteIfExists(file);
                    }
                }
            }
            Jdbc.update(connection, "DELETE FROM users WHERE email LIKE ?", List.of(MARK + "@uji.com"));
            Jdbc.update(connection, "DELETE FROM authors WHERE name LIKE ?", List.of("Penulis " + MARK + "%"));
            Jdbc.update(connection, "DELETE FROM genres WHERE name LIKE ?", List.of("Genre " + MARK + "%"));
            Jdbc.update(connection, "DELETE FROM publishers WHERE name LIKE ?", List.of("Penerbit " + MARK + "%"));
        } catch (Exception e) {
            System.out.println("Peringatan cleanup: " + e.getMessage());
        }
    }

    private static final class Flow {
        private final AppContext app;
        private final User admin;
        private final User member;
        private final File dir;
        private final CountDownLatch done;
        private final AtomicReference<Throwable> failure;

        private Stage stage;
        private RootNavigator navigator;
        private int clicks;
        private String operation;
        private int step;

        Flow(AppContext app, User admin, User member, File dir, CountDownLatch done,
             AtomicReference<Throwable> failure) {
            this.app = app;
            this.admin = admin;
            this.member = member;
            this.dir = dir;
            this.done = done;
            this.failure = failure;
        }

        private Parent root() {
            return stage.getScene().getRoot();
        }

        private void after(double millis, Runnable next) {
            PauseTransition wait = new PauseTransition(Duration.millis(millis));
            wait.setOnFinished(event -> Platform.runLater(() -> {
                try {
                    next.run();
                } catch (Throwable t) {
                    System.out.println("FLOW error: " + t);
                    t.printStackTrace();
                    failure.set(t);
                    done.countDown();
                }
            }));
            wait.play();
        }

        private Node find(String selector) {
            Node node = root().lookup(selector);
            assertNotNull(node, "Node tidak ditemukan: " + selector);
            return node;
        }

        private Button buttonByText(String text) {
            for (Node node : root().lookupAll(".button")) {
                if (node instanceof Button button && text.equals(button.getText())) {
                    return button;
                }
            }
            return null;
        }

        private Button button(String selector) {
            Node node = find(selector);
            assertTrue(node instanceof Button, "Bukan tombol: " + selector);
            return (Button) node;
        }

        private void click(Node node) {
            clicks++;
            assertTrue(node instanceof Button, "Yang diklik bukan tombol: " + node);
            ((Button) node).fire();
        }

        private void begin(String name) {
            operation = name;
            clicks = 0;
        }

        private void end() {
            RESULTS.put(operation, clicks);
            assertTrue(clicks <= MAX_CLICKS,
                    operation + " butuh " + clicks + " klik (batas " + MAX_CLICKS + ")");
            System.out.println("KLIK " + operation + " = " + clicks);
        }

        private void shot(String name) {
            try {
                WritableImage image = stage.getScene().snapshot(null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png",
                        new File(dir, step++ + "-" + name + ".png"));
            } catch (Exception e) {
                failure.set(e);
            }
        }

        private Button dialogButton(String text) {
            for (Window window : Window.getWindows()) {
                if (!(window instanceof Stage dialog) || dialog == stage || dialog.getScene() == null) {
                    continue;
                }
                for (Node node : dialog.getScene().getRoot().lookupAll(".button")) {
                    if (node instanceof Button button
                            && (text.equals(button.getText())
                            || ("Simpan".equals(text) && "OK".equals(button.getText())))) {
                        return button;
                    }
                }
            }
            return null;
        }

        /** Klik tombol dialog yang akan muncul setelah aksi berikutnya. */
        private void confirmLater(String text, Runnable after) {
            PauseTransition wait = new PauseTransition(Duration.millis(500));
            wait.setOnFinished(event -> Platform.runLater(() -> {
                try {
                    Button button = dialogButton(text);
                    assertNotNull(button, "Tombol dialog tidak ditemukan: " + text);
                    clicks++;
                    button.fire();
                    after.run();
                } catch (Throwable t) {
                    failure.set(t);
                    done.countDown();
                }
            }));
            wait.play();
        }

        private Button bookCardButton(String text) {
            for (Node card : root().lookupAll(".book-card")) {
                for (Node node : card.lookupAll(".button")) {
                    if (node instanceof Button button
                            && (text.equals(button.getText())
                            || ("Simpan".equals(text) && "OK".equals(button.getText())))) {
                        return button;
                    }
                }
            }
            return null;
        }

        private Button dummyCardButton() {
            for (Node card : root().lookupAll(".book-card")) {
                if (!card.toString().contains(DUMMY_TITLE) && !nodeText(card).contains(DUMMY_TITLE)) {
                    continue;
                }
                for (Node node : card.lookupAll(".button")) {
                    if (node instanceof Button button) {
                        return button;
                    }
                }
            }
            return null;
        }

        private String nodeText(Node node) {
            if (node instanceof javafx.scene.control.Labeled labeled) {
                return labeled.getText() == null ? "" : labeled.getText();
            }
            StringBuilder text = new StringBuilder();
            if (node instanceof javafx.scene.Parent parent) {
                for (Node child : parent.getChildrenUnmodifiable()) {
                    text.append(nodeText(child));
                }
            }
            return text.toString();
        }

        private ComboBox<?> statusComboForDummy(TableView<?> table) {
            for (Node row : table.lookupAll(".table-row-cell")) {
                if (!(row instanceof javafx.scene.control.TableRow<?> tableRow)) {
                    continue;
                }
                Object item = tableRow.getItem();
                if (item instanceof com.perpustakaan.model.BookListItem book
                        && book.judul().startsWith(DUMMY_TITLE)) {
                    for (Node node : row.lookupAll(".combo-box")) {
                        if (node instanceof ComboBox<?> combo) {
                            return combo;
                        }
                    }
                }
            }
            return null;
        }

        private List<Button> rowButtonsForTitle(TableView<?> table, String title) {
            for (Node row : table.lookupAll(".table-row-cell")) {
                if (!(row instanceof javafx.scene.control.TableRow<?> tableRow)) {
                    continue;
                }
                Object item = tableRow.getItem();
                if (!(item instanceof com.perpustakaan.model.BookListItem book)
                        || !book.judul().startsWith(title)) {
                    continue;
                }
                List<Button> result = new ArrayList<>();
                for (Node node : row.lookupAll(".button")) {
                    if (node instanceof Button button) {
                        result.add(button);
                    }
                }
                return result;
            }
            return List.of();
        }

        private void memberScenarios() {
            begin("Anggota-Create");
            click(button("#browseButton"));
            after(700, () -> {
                Button card = dummyCardButton();
                assertNotNull(card, "Kartu buku dummy tidak ditemukan");
                click(card);
                after(1400, () -> {
                    Button addToShelf = buttonByText("Tambah ke Rak");
                    assertNotNull(addToShelf, "Tombol Tambah ke Rak tidak ditemukan");
                    click(addToShelf);
                    after(700, () -> {
                        shot("member-create");
                        end();
                        memberRead();
                    });
                });
            });
        }

        private void memberRead() {
            begin("Anggota-Read");
            click(button("#catalogButton"));
            after(700, () -> {
                Button card = dummyCardButton();
                assertNotNull(card, "Kartu buku dummy tidak ditemukan");
                click(card);
                after(1000, () -> {
                    shot("member-read");
                    end();
                    memberUpdate();
                });
            });
        }

        private void memberUpdate() {
            begin("Anggota-Update");
            app.getSessionManager().login(member);
            navigator.navigate(Route.MY_SHELF, NavigationContext.empty());
            after(700, () -> {
                TableView<?> table = (TableView<?>) find("#table");
                assertFalse(table.getItems().isEmpty(), "Rak kosong");
                ComboBox<?> status = statusComboForDummy(table);
                assertNotNull(status, "Dropdown status buku dummy tidak ditemukan");
                clicks++;
                status.getSelectionModel().select((status.getItems().size() - 1) % status.getItems().size());
                after(700, () -> {
                    shot("member-update");
                    end();
                    memberDelete();
                });
            });
        }

        private void memberDelete() {
            begin("Anggota-Delete");
            TableView<?> table = (TableView<?>) find("#table");
            List<Button> buttons = rowButtonsForTitle(table, DUMMY_TITLE);
            assertTrue(buttons.size() >= 3, "Tombol aksi rak tidak lengkap");
            Button remove = buttons.get(2);
            confirmLater("Hapus", () -> after(700, () -> {
                shot("member-delete");
                end();
                adminScenarios();
            }));
            click(remove);
        }

        private void adminScenarios() {
            app.getSessionManager().login(admin);
            navigator.navigate(Route.COLLECTION_MANAGEMENT, NavigationContext.empty());
            after(700, this::adminCreate);
        }

        private javafx.scene.control.TextField dialogTextField(int index) {
            for (Window window : Window.getWindows()) {
                if (!(window instanceof Stage dialog) || dialog.getScene() == null) {
                    continue;
                }
                List<javafx.scene.control.TextField> fields = new ArrayList<>();
                for (Node node : dialog.getScene().getRoot().lookupAll(".text-field")) {
                    if (node instanceof javafx.scene.control.TextField field) {
                        fields.add(field);
                    }
                }
                if (fields.size() > index) {
                    return fields.get(index);
                }
            }
            return null;
        }

        private void adminCreate() {
            begin("Admin-Create");
            clicks++;
            javafx.scene.control.TabPane tabs = null;
            for (Node node : root().lookupAll(".tab-pane")) {
                if (node instanceof javafx.scene.control.TabPane pane && pane.getTabs().size() >= 2) {
                    tabs = pane;
                    break;
                }
            }
            assertNotNull(tabs, "Tab Kelola Koleksi tidak ditemukan");
            tabs.getSelectionModel().select(1);
            after(500, () -> {
                // Jadwalkan isian dan klik Simpan dulu, karena klik tombol membuka dialog modal.
                after(500, () -> {
                    javafx.scene.control.TextField name = dialogTextField(0);
                    assertNotNull(name, "Kolom nama penulis tidak ditemukan");
                    name.setText("Penulis " + MARK);
                    confirmLater("Simpan", () -> after(900, () -> {
                        shot("admin-create");
                        end();
                        adminRead();
                    }));
                });
                click(button("#authorAddButton"));
            });
        }

        private void adminRead() {
            begin("Admin-Read");
            navigator.navigate(Route.ADMIN_DASHBOARD, NavigationContext.empty());
            after(700, () -> {
                click(button("#openCatalogButton"));
                after(700, () -> {
                    Button card = bookCardButton("Lihat detail");
                    assertNotNull(card, "Kartu buku tidak ditemukan");
                    click(card);
                    after(700, () -> {
                        shot("admin-read");
                        end();
                        adminUpdate();
                    });
                });
            });
        }

        private void adminUpdate() {
            begin("Admin-Update");
            navigator.navigate(Route.COLLECTION_MANAGEMENT, NavigationContext.empty());
            after(700, () -> {
                TableView<?> table = (TableView<?>) find("#booksTable");
                assertFalse(table.getItems().isEmpty(), "Tabel buku kosong");
                List<Button> buttons = rowButtonsForTitle(table, DUMMY_TITLE);
                assertTrue(buttons.size() >= 2, "Tombol aksi buku tidak lengkap");
                confirmLater("Simpan", () -> after(900, () -> {
                    shot("admin-update");
                    end();
                    adminDelete();
                }));
                click(buttons.get(0));
            });
        }

        private void adminDelete() {
            begin("Admin-Delete");
            TableView<?> table = (TableView<?>) find("#booksTable");
            List<Button> buttons = rowButtonsForTitle(table, DUMMY_TITLE);
            assertTrue(buttons.size() >= 2, "Tombol aksi buku tidak lengkap");
            confirmLater("Nonaktifkan", () -> after(900, () -> {
                shot("admin-delete");
                end();
                done.countDown();
            }));
            click(buttons.get(1));
        }
    }
}
