package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Tombol khusus ikon: cari, kembali, tutup, dan sosial media. */
public class IconButton extends JButton {

    public enum Style {
        OUTLINE,
        DARK
    }

    private final String iconName;
    private final Style style;
    private final int iconSize;
    private final int boxWidth;
    private final int boxHeight;
    private boolean hover;

    public IconButton(String iconName, Style style, int iconSize, int boxWidth, int boxHeight,
                      String accessibleName) {
        this.iconName = iconName;
        this.style = style;
        this.iconSize = iconSize;
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(accessibleName);
        setToolTipText(accessibleName);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(boxWidth, boxHeight);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    private Color background() {
        return switch (style) {
            case OUTLINE -> hover ? Theme.PURPLE : Theme.WHITE;
            case DARK -> hover ? Theme.PURPLE : Theme.alpha(Theme.WHITE, 20);
        };
    }

    private Color iconColor() {
        return switch (style) {
            case OUTLINE -> hover ? Theme.WHITE : Theme.PURPLE;
            case DARK -> Theme.WHITE;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color background = background();
            if (background != null) {
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            if (style == Style.OUTLINE) {
                g2.setColor(Theme.PURPLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            if (isFocusOwner()) {
                g2.setColor(Theme.PURPLE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
            }
            int x = (getWidth() - iconSize) / 2;
            int y = (getHeight() - iconSize) / 2;
            IconPainter.paint(g2, iconName, x, y, iconSize, iconColor(), 2f);
        } finally {
            g2.dispose();
        }
    }
}
