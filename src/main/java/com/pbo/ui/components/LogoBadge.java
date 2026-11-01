package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Kotak logo ungu dengan huruf P putih. */
public class LogoBadge extends JComponent {

    private final int size;

    public LogoBadge(int size) {
        this.size = size;
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
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
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.PURPLE);
            g2.fillRoundRect(0, 0, size, size, 12, 12);
            g2.setColor(Theme.WHITE);
            g2.setFont(Theme.font(Font.BOLD, size * 0.52f));
            String text = "P";
            int x = (size - g2.getFontMetrics().stringWidth(text)) / 2;
            int y = (size - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(text, x, y);
        } finally {
            g2.dispose();
        }
    }
}
