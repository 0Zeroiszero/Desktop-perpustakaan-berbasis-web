package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Badge kecil dengan latar membulat. */
public class BadgeLabel extends JLabel {

    private final int radius;

    public BadgeLabel(String text, java.awt.Color background, java.awt.Color foreground, int radius) {
        super(text);
        this.radius = radius;
        setOpaque(false);
        setFont(Theme.font(Font.BOLD, 12f));
        setForeground(foreground);
        setBackground(background);
        setBorder(new EmptyBorder(4, 10, 4, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
