package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Panel berlatar putih, sudut membulat, dan garis tepi tipis. */
public class RoundedPanel extends JPanel {

    private Color fill;
    private Color borderColor;
    private int radius;

    public RoundedPanel(Color fill, int radius, Color borderColor) {
        this.fill = fill;
        this.radius = radius;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (fill != null) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, radius, radius);
            }
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
