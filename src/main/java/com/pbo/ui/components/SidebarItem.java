package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Item navigasi sidebar dengan ikon dan keadaan aktif. */
public class SidebarItem extends JButton {

    private static final int HEIGHT = 42;

    private final String label;
    private final String iconName;
    private boolean active;
    private boolean hover;

    public SidebarItem(String label, String iconName) {
        this.label = label;
        this.iconName = iconName;
        setText(label);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Theme.font(Font.PLAIN, 15f));
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
        return new Dimension(Theme.SIDEBAR_WIDTH - 24, HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (active || hover) {
                g2.setColor(active ? Theme.PURPLE_LIGHT : Theme.HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            Color foreground = active ? Theme.PURPLE : Theme.TEXT_SECONDARY;
            IconPainter.paint(g2, iconName, 12, (getHeight() - 18) / 2, 18, foreground, 1.9f);

            g2.setColor(active ? Theme.PURPLE : Theme.DARK);
            g2.setFont(Theme.font(active ? Font.BOLD : Font.PLAIN, 15f));
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(label, 42, y);

            if (isFocusOwner()) {
                g2.setColor(Theme.PURPLE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
            }
        } finally {
            g2.dispose();
        }
    }
}
