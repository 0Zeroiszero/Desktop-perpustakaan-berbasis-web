package com.pbo.ui.components;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/** Teks multi-baris dengan lebar tetap; pengganti atribut width HTML yang diabaikan Swing. */
public class WrappedLabel extends JComponent {

    private final String text;
    private final int wrapWidth;
    private final int lineHeight;

    public WrappedLabel(String text, int wrapWidth, int lineHeight) {
        this.text = text;
        this.wrapWidth = wrapWidth;
        this.lineHeight = lineHeight;
        setAlignmentX(LEFT_ALIGNMENT);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(wrapWidth, lines(getFontMetrics(getFont()), wrapWidth).size() * lineHeight);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int width = getWidth() > 0 ? Math.min(wrapWidth, getWidth()) : wrapWidth;
            int y = (lineHeight - metrics.getHeight()) / 2 + metrics.getAscent();
            for (String line : lines(metrics, width)) {
                g2.drawString(line, 0, y);
                y += lineHeight;
            }
        } finally {
            g2.dispose();
        }
    }

    private List<String> lines(FontMetrics metrics, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && metrics.stringWidth(candidate) > width) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
}
