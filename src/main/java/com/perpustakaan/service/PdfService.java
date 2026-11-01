package com.perpustakaan.service;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.util.AppLogger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Membuka dan merender PDF dengan PDFBox.
 */
public class PdfService {

    public PdfSession open(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            throw new AppException("Berkas PDF tidak ditemukan di storage.");
        }
        try {
            PDDocument document = Loader.loadPDF(file.toFile());
            if (document.isEncrypted()) {
                document.close();
                throw new AppException("PDF terenkripsi dan tidak dapat dibuka.");
            }
            int pages = document.getNumberOfPages();
            if (pages <= 0) {
                document.close();
                throw new AppException("PDF tidak memiliki halaman.");
            }
            return new PdfSession(document, pages);
        } catch (IOException e) {
            throw new AppException("Berkas PDF tidak dapat dibuka. Pastikan berkas tidak rusak.", e);
        }
    }

    public int getPageCount(Path file) {
        try (PdfSession session = open(file)) {
            return session.pageCount();
        }
    }

    public static final class PdfSession implements AutoCloseable {

        private final PDDocument document;
        private final PDFRenderer renderer;
        private final int pageCount;
        private boolean closed;

        private PdfSession(PDDocument document, int pageCount) {
            this.document = document;
            this.renderer = new PDFRenderer(document);
            this.renderer.setSubsamplingAllowed(true);
            this.pageCount = pageCount;
        }

        public int pageCount() {
            return pageCount;
        }

        public synchronized BufferedImage renderPage(int zeroBasedPage, float dpi) {
            if (closed) {
                throw new AppException("Sesi PDF sudah ditutup.");
            }
            if (zeroBasedPage < 0 || zeroBasedPage >= pageCount) {
                throw new AppException("Halaman PDF tidak valid.");
            }
            try {
                return renderer.renderImageWithDPI(zeroBasedPage, dpi, ImageType.RGB);
            } catch (IOException e) {
                throw new AppException("Halaman PDF gagal dirender.", e);
            }
        }

        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                document.close();
            } catch (IOException e) {
                AppLogger.warn("Gagal menutup dokumen PDF.", e);
            }
        }
    }
}
