package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Kolom teks membulat dengan placeholder, setara .form-control Bootstrap. */
public class RoundedTextField extends JTextField {

    private final String placeholder;

    public RoundedTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 12, 10, 12));
        setFont(Theme.font(Font.PLAIN, 16f));
        setForeground(Theme.DARK);
        setCaretColor(Theme.DARK);
        setSelectionColor(Theme.PURPLE_LIGHT);
        setSelectedTextColor(Theme.DARK);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(preferred.width, 44);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        } finally {
            g2.dispose();
        }

        super.paintComponent(g);

        Graphics2D g3 = (Graphics2D) g.create();
        try {
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (getText().isEmpty() && !isFocusOwner()) {
                g3.setColor(Theme.PLACEHOLDER);
                g3.setFont(getFont());
                int x = getInsets().left;
                int y = (getHeight() - g3.getFontMetrics().getHeight()) / 2 + g3.getFontMetrics().getAscent();
                g3.drawString(placeholder, x, y);
            }
            g3.setColor(isFocusOwner() ? Theme.PURPLE : Theme.BORDER_INPUT);
            g3.setStroke(new BasicStroke(isFocusOwner() ? 1.6f : 1f));
            g3.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        } finally {
            g3.dispose();
        }
    }
}
