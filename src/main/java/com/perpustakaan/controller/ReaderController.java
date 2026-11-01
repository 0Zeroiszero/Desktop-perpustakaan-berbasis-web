package com.perpustakaan.controller;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.model.BookDetail;
import com.perpustakaan.model.ReadingProgress;
import com.perpustakaan.service.PdfService;
import com.perpustakaan.util.AppLogger;
import com.perpustakaan.util.Async;
import com.perpustakaan.util.FormatUtil;
import com.perpustakaan.util.ImageUtil;
import com.perpustakaan.view.Icons;
import com.perpustakaan.view.NavigationContext;
import com.perpustakaan.view.Route;
import com.perpustakaan.view.Ui;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Pembaca PDF immersive dengan navigasi halaman dan penyimpanan progres.
 */
public class ReaderController extends BaseController {

    @FXML private BorderPane readerRoot;
    @FXML private Button backButton;
    @FXML private Label bookTitleLabel;
    @FXML private Label bookAuthorLabel;
    @FXML private Label pageLabel;
    @FXML private StackPane pageArea;
    @FXML private ImageView pageImage;
    @FXML private Label pagePlaceholder;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Slider pageSlider;
    @FXML private Label percentLabel;
    @FXML private Label statusLabel;

    private final EventHandler<KeyEvent> keyHandler = this::handleKey;
    private PdfService.PdfSession pdfSession;
    private BookDetail detail;
    private String bookId;
    private int totalPages;
    private int currentPage = 1;
    private int renderGeneration;
    private boolean updatingSlider;
    private javafx.scene.Node placeholderIcon;

    @FXML
    private void initialize() {
        backButton.setGraphic(Icons.icon(Icons.BACK, 16));
        prevButton.setGraphic(Icons.icon(Icons.CHEVRON_LEFT, 16));
        nextButton.setGraphic(Icons.icon(Icons.CHEVRON_RIGHT, 16));
        prevButton.setMinWidth(118);
        prevButton.setPrefWidth(118);
        nextButton.setMinWidth(118);
        nextButton.setPrefWidth(118);
        placeholderIcon = Icons.icon(Icons.BOOK, 48, "reader-placeholder-icon");
        pageArea.getChildren().add(0, placeholderIcon);
        pagePlaceholder.setText("Memuat halaman...");
        pageSlider.setMin(1);
        pageSlider.setMax(1);
        pageSlider.setValue(1);
        pageSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (updatingSlider || totalPages <= 0) {
                return;
            }
            int page = clamp((int) Math.round(newValue.doubleValue()));
            pageLabel.setText("Halaman " + page + " dari " + totalPages);
        });
        pageSlider.setOnMouseReleased(event -> commitSlider());
        pageSlider.setOnKeyReleased(event -> commitSlider());
        pageArea.widthProperty().addListener((observable, oldValue, newValue) -> fitImage());
        pageArea.heightProperty().addListener((observable, oldValue, newValue) -> fitImage());
        updateControls();
    }

    @Override
    public void onShow() {
        super.onShow();
        bookId = context().bookId();
        if (bookId == null || bookId.isBlank()) {
            Ui.error("Buku tidak dipilih.");
            Ui.navigate(Route.MY_SHELF, NavigationContext.empty());
            return;
        }
        readerRoot.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        openDocument();
    }

    private void openDocument() {
        setBusy(true, "Membuka dokumen PDF...");
        Async.run(() -> {
            BookDetail bookDetail = app.getEbookService().detail(currentUser(), bookId);
            if (!bookDetail.ebook().isReaderSupported()) {
                throw new AppException("Format " + bookDetail.ebook().getFormat().displayName()
                        + " belum didukung pembaca PDF.");
            }
            PdfService.PdfSession session = app.getEbookService().openPdf(currentUser(), bookId);
            try {
                ReadingProgress progress = app.getReadingProgressService()
                        .startReading(currentUser(), bookId, session.pageCount());
                return new ReaderData(bookDetail, session, progress);
            } catch (RuntimeException e) {
                session.close();
                throw e;
            }
        }, data -> {
            if (!isActive()) {
                data.session().close();
                return;
            }
            detail = data.detail();
            pdfSession = data.session();
            totalPages = pdfSession.pageCount();
            bookTitleLabel.setText(detail.ebook().getJudul());
            bookAuthorLabel.setText(detail.penulis());
            pageSlider.setMin(1);
            pageSlider.setMax(totalPages);
            int start = context().restart() || data.progress().getLastPosition() == null
                    ? 1 : data.progress().getLastPosition();
            currentPage = clamp(start);
            setBusy(false, null);
            renderPage(currentPage, true);
        }, error -> {
            if (isActive()) {
                setBusy(false, null);
                pagePlaceholder.setText("PDF gagal dibuka.");
                handleError(error);
            }
        });
    }

    private void renderPage(int page, boolean save) {
        if (pdfSession == null) {
            return;
        }
        currentPage = clamp(page);
        final int pageToRender = currentPage;
        final int generation = ++renderGeneration;
        updateControls();
        pagePlaceholder.setVisible(false);
        pagePlaceholder.setManaged(false);
        setBusy(true, "Merender halaman " + currentPage + "...");
        runAsync(() -> pdfSession.renderPage(pageToRender - 1, app.getConfig().getReaderDpi()), image -> {
            if (generation != renderGeneration) {
                return;
            }
            setBusy(false, null);
            pageImage.setImage(ImageUtil.toFxImage(image));
            placeholderIcon.setVisible(false);
            pagePlaceholder.setVisible(false);
            pagePlaceholder.setManaged(false);
            fitImage();
            updateControls();
            if (save) {
                saveProgress(pageToRender);
            }
        }, error -> {
            if (generation == renderGeneration) {
                setBusy(false, null);
                placeholderIcon.setVisible(true);
                pagePlaceholder.setText("Halaman gagal dirender.");
                pagePlaceholder.setVisible(true);
                pagePlaceholder.setManaged(true);
                handleError(error);
            }
        });
    }

    private void saveProgress(int page) {
        if (pdfSession == null) {
            return;
        }
        runAsync(() -> app.getReadingProgressService().saveProgress(currentUser(), bookId, page, totalPages),
                progress -> {
                    statusLabel.setText(progress.getStatus().databaseValue() + "  •  "
                            + FormatUtil.percent(progress.getProgressPercent()));
                });
    }

    private void saveProgressSync() {
        if (pdfSession == null || totalPages <= 0) {
            return;
        }
        try {
            app.getReadingProgressService().saveProgress(currentUser(), bookId, currentPage, totalPages);
        } catch (RuntimeException e) {
            AppLogger.warn("Gagal menyimpan progres terakhir.", e);
        }
    }

    private void commitSlider() {
        if (totalPages <= 0) {
            return;
        }
        int page = clamp((int) Math.round(pageSlider.getValue()));
        if (page != currentPage) {
            renderPage(page, true);
        } else {
            updateControls();
        }
    }

    private void updateControls() {
        int max = Math.max(1, totalPages);
        updatingSlider = true;
        pageSlider.setMax(max);
        pageSlider.setValue(currentPage);
        updatingSlider = false;
        pageLabel.setText("Halaman " + currentPage + " dari " + max);
        int percent = totalPages <= 0 ? 0 : (int) Math.round(currentPage * 100.0 / totalPages);
        percentLabel.setText(percent + "%");
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(totalPages <= 0 || currentPage >= totalPages);
        pageSlider.setDisable(totalPages <= 0);
        statusLabel.setText("Progres tersimpan otomatis");
    }

    private void fitImage() {
        if (pageImage.getImage() == null) {
            return;
        }
        double width = Math.max(200, pageArea.getWidth() - 36);
        double height = Math.max(200, pageArea.getHeight() - 36);
        pageImage.setFitWidth(width);
        pageImage.setFitHeight(height);
    }

    private int clamp(int page) {
        return Math.max(1, Math.min(page, Math.max(1, totalPages)));
    }

    private void handleKey(KeyEvent event) {
        if (event.getTarget() instanceof Slider) {
            return;
        }
        if (event.getCode() == KeyCode.LEFT) {
            if (currentPage > 1) {
                renderPage(currentPage - 1, true);
            }
            event.consume();
        } else if (event.getCode() == KeyCode.RIGHT) {
            if (currentPage < totalPages) {
                renderPage(currentPage + 1, true);
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            handleBack();
            event.consume();
        }
    }

    @FXML
    private void handlePrev() {
        if (currentPage > 1) {
            renderPage(currentPage - 1, true);
        }
    }

    @FXML
    private void handleNext() {
        if (currentPage < totalPages) {
            renderPage(currentPage + 1, true);
        }
    }

    @FXML
    private void handleBack() {
        saveProgressSync();
        closeSession();
        Route origin = context().origin();
        if (origin == Route.BOOK_DETAIL) {
            Route detailOrigin = context().detailOrigin() == null
                    ? Route.CATALOG : context().detailOrigin();
            Ui.navigate(Route.BOOK_DETAIL,
                    NavigationContext.book(bookId, detailOrigin, false));
            return;
        }
        if (origin == null || origin == Route.READER) {
            origin = Route.MY_SHELF;
        }
        Ui.navigate(origin, NavigationContext.empty());
    }

    @Override
    public void onHide() {
        readerRoot.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        closeSession();
        setBusy(false, null);
        super.onHide();
    }

    private void closeSession() {
        PdfService.PdfSession session = pdfSession;
        pdfSession = null;
        if (session != null) {
            session.close();
        }
    }

    private record ReaderData(BookDetail detail, PdfService.PdfSession session, ReadingProgress progress) {
    }
}
