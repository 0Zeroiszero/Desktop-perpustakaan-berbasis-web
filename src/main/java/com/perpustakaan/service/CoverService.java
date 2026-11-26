package com.perpustakaan.service;

import com.perpustakaan.config.AppPaths;
import com.perpustakaan.exception.AppException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Membuat cover bawaan 600x800 mengikuti includes/cover_helper.php pada proyek web.
 * Font dibundel di resources supaya tidak bergantung folder web.
 */
public class CoverService {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static final int MAX_TEXT_WIDTH = 504;
    private static final Color BLUE = new Color(2, 106, 161);
    private static final Color WHITE = Color.WHITE;
    private static final Color LIGHT_BLUE = new Color(206, 228, 252);

    public String generate(String bookId, String title, String author, String genre) {
        try {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(BLUE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            Font bold = loadFont("/fonts/Inter-Bold.ttf", 44f);
            Font regular = loadFont("/fonts/Inter-Regular.ttf", 24f);
            g.setColor(WHITE);
            g.setFont(bold.deriveFont(22f));
            g.drawString("PUSTAKADIGITAL", 48, 62);
            g.fillRect(48, 80, 504, 2);

            g.setFont(bold);
            List<String> lines = wrap(g, safe(title), 3);
            int y = 210;
            for (String line : lines) {
                g.drawString(line, 48, y);
                y += 58;
            }

            g.setFont(regular);
            g.setColor(LIGHT_BLUE);
            g.drawString(trim(safe(author), 42), 48, 390);

            g.setColor(WHITE);
            g.drawString(trim(safe(genre), 20), 48, 728);
            g.drawString("PDF", 552 - g.getFontMetrics().stringWidth("PDF"), 728);
            g.dispose();

            Path destination = AppPaths.getCoverDir().resolve(bookId + ".jpg");
            Files.createDirectories(destination.getParent());
            ImageIO.write(image, "jpg", destination.toFile());
            return AppPaths.coverStoredPath(bookId);
        } catch (Exception e) {
            throw new AppException("Gagal membuat cover otomatis.", e);
        }
    }

    private Font loadFont(String resource, float size) throws Exception {
        try (InputStream stream = CoverService.class.getResourceAsStream(resource)) {
            if (stream == null) {
                return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size);
            }
            return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
        }
    }

    private List<String> wrap(Graphics2D g, String text, int maxLines) {
        FontMetrics metrics = g.getFontMetrics();
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (metrics.stringWidth(candidate) > MAX_TEXT_WIDTH && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        if (lines.size() > maxLines) {
            lines = new ArrayList<>(lines.subList(0, maxLines));
        }
        if (!lines.isEmpty()) {
            int last = lines.size() - 1;
            String value = lines.get(last);
            while (!value.isEmpty() && metrics.stringWidth(value + "…") > MAX_TEXT_WIDTH) {
                value = value.substring(0, value.length() - 1);
            }
            lines.set(last, value + (value.length() < lines.get(last).length() ? "…" : ""));
        }
        return lines;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Tanpa judul" : value.trim();
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
