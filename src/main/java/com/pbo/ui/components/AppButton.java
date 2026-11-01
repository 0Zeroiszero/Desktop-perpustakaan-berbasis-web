package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Tombol dengan warna, sudut, dan ikon sesuai gaya tombol pada web. */
public class AppButton extends JButton {

    public enum Style {
        PRIMARY,
        SECONDARY_OUTLINE,
        BLUE,
        GREEN
    }

    private final Style style;
    private final int buttonHeight;
    private boolean hover;

    public AppButton(String text, Style style, String iconName, int buttonHeight) {
        super(text);
        this.style = style;
        this.buttonHeight = buttonHeight;
        setFont(Theme.font(Font.BOLD, 16f));
        setBorder(new EmptyBorder(0, 18, 0, 18));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        setIconTextGap(7);

        if (iconName != null) {
            setIcon(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    try {
                        IconPainter.paint(g2, iconName, x, y, 16, getForeground(), 2.1f);
                    } finally {
                        g2.dispose();
                    }
                }

                @Override
                public int getIconWidth() {
                    return 16;
                }

                @Override
                public int getIconHeight() {
                    return 16;
                }
            });
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                refreshForeground();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                refreshForeground();
                repaint();
            }
        });
        refreshForeground();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(preferred.width, buttonHeight);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, buttonHeight);
    }

    private void refreshForeground() {
        setForeground(foregroundColor());
    }

    private Color backgroundColor() {
        if (!isEnabled()) {
            return Theme.WHITE;
        }
        boolean pressed = getModel().isPressed();
        return switch (style) {
            case PRIMARY -> pressed ? Theme.PURPLE_ACTIVE : hover ? Theme.PURPLE_HOVER : Theme.PURPLE;
            case SECONDARY_OUTLINE -> hover ? Theme.TEXT_SECONDARY : Theme.WHITE;
            case BLUE -> hover ? Theme.BLUE_DARKER : Theme.BLUE_DARK;
            case GREEN -> hover ? Theme.GREEN_DARKER : Theme.GREEN_DARK;
        };
    }

    private Color foregroundColor() {
        if (!isEnabled()) {
            return Theme.PLACEHOLDER;
        }
        return switch (style) {
            case PRIMARY, BLUE, GREEN -> Theme.WHITE;
            case SECONDARY_OUTLINE -> hover ? Theme.WHITE : Theme.TEXT_SECONDARY;
        };
    }

    private Color borderColor() {
        if (!isEnabled()) {
            return Theme.BORDER_SUBTLE;
        }
        return style == Style.SECONDARY_OUTLINE ? Theme.TEXT_SECONDARY : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        setForeground(foregroundColor());
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            Color border = borderColor();
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            if (isFocusOwner()) {
                g2.setColor(Theme.PURPLE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
