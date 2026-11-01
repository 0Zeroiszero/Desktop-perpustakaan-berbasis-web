package com.pbo.ui.components;

import com.pbo.ui.Assets;
import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Sampul halaman detail: lebar tetap, rasio asli, dengan bayangan tipis. */
public class DetailCover extends JComponent {

    private static final int IMAGE_WIDTH = 280;
    private static final int SIDE_MARGIN = 12;
    private static final int BOTTOM_MARGIN = 18;

    private final BufferedImage image;
    private final int imageHeight;

    public DetailCover(String cover) {
        this.image = Assets.image(cover);
        this.imageHeight = image == null ? 360 : (int) Math.round(IMAGE_WIDTH * (double) image.getHeight() / image.getWidth());
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(IMAGE_WIDTH + SIDE_MARGIN * 2, imageHeight + BOTTOM_MARGIN);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int x = SIDE_MARGIN;
            int y = 0;

            for (int i = 0; i < 4; i++) {
                g2.setColor(Theme.alpha(Theme.DARK, 7));
                g2.fillRoundRect(x - 1 - i, y + 5 + i, IMAGE_WIDTH + 2 + i * 2,
                        imageHeight + 6 + i, 14, 14);
            }

            g2.setClip(new RoundRectangle2D.Double(x, y, IMAGE_WIDTH, imageHeight, 10, 10));
            if (image != null) {
                g2.drawImage(image, x, y, IMAGE_WIDTH, imageHeight, null);
            } else {
                g2.setColor(Theme.PURPLE_LIGHT);
                g2.fillRect(x, y, IMAGE_WIDTH, imageHeight);
                IconPainter.paint(g2, "book", x + (IMAGE_WIDTH - 56) / 2,
                        y + (imageHeight - 56) / 2, 56, Theme.PURPLE, 1.6f);
            }
        } finally {
            g2.dispose();
        }
    }
}
