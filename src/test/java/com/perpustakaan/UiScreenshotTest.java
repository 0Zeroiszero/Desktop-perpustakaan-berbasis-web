package com.perpustakaan;

import com.perpustakaan.config.AppContext;
import com.perpustakaan.model.User;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.RootNavigator;
import com.perpustakaan.view.Route;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class UiScreenshotTest {

    @Test
    void capturePages() throws Exception {
        AppContext app = AppContext.get();
        User admin = app.getAuthService().login("adminpustaka", "admin123");
        app.getSessionManager().login(admin);
        File dir = new File(System.getProperty("java.io.tmpdir"), "opencode/pustaka-screens");
        dir.mkdirs();

        CountDownLatch done = new CountDownLatch(1);
        Platform.startup(() -> {
        });
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                RootNavigator navigator = new RootNavigator(stage);
                stage.show();
                navigator.navigate(Route.ADMIN_DASHBOARD, NavigationContext.empty());

                List<Step> steps = List.of(
                        new Step("01-admin-dashboard", Route.ADMIN_DASHBOARD, NavigationContext.empty()),
                        new Step("02-books", Route.BOOKS, NavigationContext.empty()),
                        new Step("03-categories", Route.CATEGORIES, NavigationContext.empty()),
                        new Step("04-authors", Route.AUTHORS, NavigationContext.empty()),
                        new Step("05-users", Route.USERS, NavigationContext.empty()),
                        new Step("06-catalog", Route.CATALOG, NavigationContext.empty()),
                        new Step("07-detail", Route.BOOK_DETAIL,
                                NavigationContext.book("BK001", Route.CATALOG, false)),
                        new Step("08-member-dashboard", Route.MEMBER_DASHBOARD, NavigationContext.empty()),
                        new Step("09-shelf", Route.MY_SHELF, NavigationContext.empty()),
                        new Step("10-reader", Route.READER,
                                NavigationContext.book("BK001", Route.BOOK_DETAIL, false))
                );
                runStep(stage, navigator, steps, 0, dir, done);
            } catch (Throwable t) {
                t.printStackTrace();
                done.countDown();
            }
        });
        done.await(90, TimeUnit.SECONDS);
        Platform.exit();
    }

    private void runStep(Stage stage, RootNavigator navigator, List<Step> steps, int index,
                         File dir, CountDownLatch done) {
        if (index >= steps.size()) {
            done.countDown();
            return;
        }
        Step step = steps.get(index);
        if (index > 0) {
            navigator.navigate(step.route(), step.context());
        }
        PauseTransition wait = new PauseTransition(Duration.seconds(index == steps.size() - 1 ? 4 : 2));
        wait.setOnFinished(event -> {
            try {
                WritableImage image = stage.getScene().snapshot(null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png",
                        new File(dir, step.name() + ".png"));
                System.out.println("SHOT " + step.name());
            } catch (Exception e) {
                e.printStackTrace();
            }
            runStep(stage, navigator, steps, index + 1, dir, done);
        });
        wait.play();
    }

    private record Step(String name, Route route, NavigationContext context) {
    }
}
