package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Pil filter kategori; bisa aktif bersamaan (multi-pilih) dan menampilkan tanda centang. */
public class CategoryButton extends JButton {

    private final String label;
    private boolean active;
    private boolean hover;

    public CategoryButton(String label) {
        this.label = label;
        setText(label);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Theme.font(Font.BOLD, 15f));
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

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getFontMetrics(Theme.font(Font.BOLD, 15f)).stringWidth(label) + 54;
        return new Dimension(width, 38);
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
            g2.setColor(active ? Theme.PURPLE_LIGHT : Theme.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(active || hover ? Theme.PURPLE : Theme.BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

            g2.setColor(active ? Theme.PURPLE : Theme.DARK);
            g2.setFont(Theme.font(Font.BOLD, 15f));
            if (active) {
                IconPainter.paint(g2, "check", 11, (getHeight() - 13) / 2, 13, Theme.PURPLE, 2.2f);
            }
            int x = (getWidth() - g2.getFontMetrics().stringWidth(label)) / 2;
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(label, x, y);

            if (isFocusOwner()) {
                g2.setColor(Theme.PURPLE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, getHeight(), getHeight());
            }
        } finally {
            g2.dispose();
        }
    }
}
