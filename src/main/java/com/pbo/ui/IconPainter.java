package com.pbo.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Ikon garis sederhana (setara Bootstrap Icons) yang digambar dengan Graphics2D. */
public final class IconPainter {

    private IconPainter() {
    }

    public static void paint(Graphics2D graphics, String name, int x, int y, int size,
                             Color color, float strokeWidth) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(x, y);
            double scale = size / 24.0;
            g.scale(scale, scale);
            g.setColor(color);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setStroke(new BasicStroke((float) (strokeWidth / scale),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g, name);
        } finally {
            g.dispose();
        }
    }

    private static void draw(Graphics2D g, String name) {
        switch (name) {
            case "search" -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 12, 12));
                g.draw(new Line2D.Double(14.2, 14.2, 21, 21));
            }
            case "arrow-left" -> {
                g.draw(new Line2D.Double(20, 12, 5, 12));
                Path2D p = new Path2D.Double();
                p.moveTo(11, 6);
                p.lineTo(5, 12);
                p.lineTo(11, 18);
                g.draw(p);
            }
            case "chevron-down" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 9.5);
                p.lineTo(12, 15.5);
                p.lineTo(18, 9.5);
                g.draw(p);
            }
            case "chevron-right" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(9, 6);
                p.lineTo(15, 12);
                p.lineTo(9, 18);
                g.draw(p);
            }
            case "bookmark-plus" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 3.5);
                p.lineTo(18, 3.5);
                p.lineTo(18, 21);
                p.lineTo(12, 17);
                p.lineTo(6, 21);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(12, 7.5, 12, 13.5));
                g.draw(new Line2D.Double(9, 10.5, 15, 10.5));
            }
            case "book" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 6.2);
                p.curveTo(10.2, 4.8, 6.2, 4.4, 4, 5.2);
                p.lineTo(4, 19.2);
                p.curveTo(6.2, 18.4, 10.2, 18.8, 12, 20.2);
                p.curveTo(13.8, 18.8, 17.8, 18.4, 20, 19.2);
                p.lineTo(20, 5.2);
                p.curveTo(17.8, 4.4, 13.8, 4.8, 12, 6.2);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(12, 6.2, 12, 20.2));
            }
            case "hourglass" -> {
                g.draw(new Line2D.Double(6, 3, 18, 3));
                g.draw(new Line2D.Double(6, 21, 18, 21));
                Path2D p = new Path2D.Double();
                p.moveTo(7.5, 3.5);
                p.lineTo(7.5, 7);
                p.curveTo(7.5, 10, 12, 10.5, 12, 12);
                p.curveTo(12, 13.5, 7.5, 14, 7.5, 17);
                p.lineTo(7.5, 20.5);
                p.moveTo(16.5, 3.5);
                p.lineTo(16.5, 7);
                p.curveTo(16.5, 10, 12, 10.5, 12, 12);
                p.curveTo(12, 13.5, 16.5, 14, 16.5, 17);
                p.lineTo(16.5, 20.5);
                g.draw(p);
            }
            case "check" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(4.5, 12.5);
                p.lineTo(9.5, 17.5);
                p.lineTo(19.5, 6.5);
                g.draw(p);
            }
            case "info" -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Line2D.Double(12, 11, 12, 17));
                g.fill(new Ellipse2D.Double(11, 6.6, 2, 2));
            }
            case "grid" -> {
                g.draw(new RoundRectangle2D.Double(4, 4, 6.5, 6.5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(13.5, 4, 6.5, 6.5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(4, 13.5, 6.5, 6.5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(13.5, 13.5, 6.5, 6.5, 1.5, 1.5));
            }
            case "database" -> {
                g.draw(new Ellipse2D.Double(4, 3.5, 16, 5));
                g.draw(new Line2D.Double(4, 6, 4, 18));
                g.draw(new Line2D.Double(20, 6, 20, 18));
                g.draw(new java.awt.geom.Arc2D.Double(4, 15.5, 16, 5, 180, 180, java.awt.geom.Arc2D.OPEN));
                g.draw(new java.awt.geom.Arc2D.Double(4, 10.5, 16, 5, 180, 180, java.awt.geom.Arc2D.OPEN));
            }
            case "user" -> {
                g.draw(new Ellipse2D.Double(8.5, 3.5, 7, 7));
                Path2D p = new Path2D.Double();
                p.moveTo(4.5, 20.5);
                p.curveTo(4.5, 16, 8, 14, 12, 14);
                p.curveTo(16, 14, 19.5, 16, 19.5, 20.5);
                g.draw(p);
            }
            case "x" -> {
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
            }
            case "envelope" -> {
                g.draw(new RoundRectangle2D.Double(3, 5, 18, 14, 2, 2));
                Path2D p = new Path2D.Double();
                p.moveTo(3.5, 6.5);
                p.lineTo(12, 13);
                p.lineTo(20.5, 6.5);
                g.draw(p);
            }
            case "instagram" -> {
                g.draw(new RoundRectangle2D.Double(4, 4, 16, 16, 10, 10));
                g.draw(new Ellipse2D.Double(8.5, 8.5, 7, 7));
                g.fill(new Ellipse2D.Double(16.1, 6.1, 2.2, 2.2));
            }
            case "facebook" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(13.5, 21);
                p.lineTo(13.5, 14);
                p.lineTo(15.9, 14);
                p.lineTo(16.3, 11);
                p.lineTo(13.5, 11);
                p.lineTo(13.5, 9.2);
                p.curveTo(13.5, 8.3, 13.8, 7.7, 15.1, 7.7);
                p.lineTo(16.4, 7.7);
                p.lineTo(16.4, 5.1);
                p.curveTo(16.1, 5.1, 15.2, 5, 14.1, 5);
                p.curveTo(11.8, 5, 10.2, 6.4, 10.2, 9);
                p.lineTo(10.2, 11);
                p.lineTo(8, 11);
                p.lineTo(8, 14);
                p.lineTo(10.2, 14);
                p.lineTo(10.2, 21);
                p.closePath();
                g.fill(p);
            }
            case "youtube" -> {
                g.draw(new RoundRectangle2D.Double(3, 6, 18, 12, 8, 8));
                Path2D p = new Path2D.Double();
                p.moveTo(10, 9.5);
                p.lineTo(16, 12);
                p.lineTo(10, 14.5);
                p.closePath();
                g.fill(p);
            }
            default -> g.draw(new Ellipse2D.Double(4, 4, 16, 16));
        }
    }

    public static Icon icon(String name, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    paint(g2, name, x, y, size, color, 2f);
                } finally {
                    g2.dispose();
                }
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
}
