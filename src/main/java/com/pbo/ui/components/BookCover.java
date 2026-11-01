package com.pbo.ui.components;

import com.pbo.ui.Assets;
import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Sampul grid: rasio tetap, object-fit cover, sudut membulat. */
public class BookCover extends JComponent {

    private final BufferedImage image;
    private final Color background;
    private final boolean allCorners;

    public BookCover(String cover, Color background) {
        this(cover, background, false);
    }

    public BookCover(String cover, Color background, boolean allCorners) {
        this.image = Assets.image(cover);
        this.background = background;
        this.allCorners = allCorners;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(background);
            g2.fillRect(0, 0, w, h);

            Path2D clip = new Path2D.Double();
            int r = 12;
            if (allCorners) {
                clip.append(new RoundRectangle2D.Double(0, 0, w, h, r, r), false);
            } else {
                clip.moveTo(0, r);
                clip.quadTo(0, 0, r, 0);
                clip.lineTo(w - r, 0);
                clip.quadTo(w, 0, w, r);
                clip.lineTo(w, h);
                clip.lineTo(0, h);
                clip.closePath();
            }
            g2.clip(clip);

            if (image != null) {
                double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
                int drawW = (int) Math.ceil(image.getWidth() * scale);
                int drawH = (int) Math.ceil(image.getHeight() * scale);
                int x = (w - drawW) / 2;
                int y = (h - drawH) / 2;
                g2.drawImage(image, x, y, drawW, drawH, null);
            } else {
                IconPainter.paint(g2, "book", (w - 40) / 2, (h - 40) / 2, 40, Theme.GRAY, 1.6f);
            }
        } finally {
            g2.dispose();
        }
    }
}
