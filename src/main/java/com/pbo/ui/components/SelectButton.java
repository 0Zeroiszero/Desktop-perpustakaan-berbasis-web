package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
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
import java.util.function.Consumer;

/** Tombol pilihan (pengganti select) dengan menu dropdown. */
public class SelectButton extends JButton {

    private final JPopupMenu menu;
    private String value;
    private final Consumer<String> onChange;
    private boolean hover;

    public SelectButton(String[] options, String initial, Consumer<String> onChange) {
        this.value = initial;
        this.onChange = onChange;
        setText(initial);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Theme.font(Font.PLAIN, 16f));

        menu = new JPopupMenu();
        menu.setBorder(new javax.swing.border.LineBorder(Theme.BORDER, 1));
        menu.setBackground(Theme.WHITE);
        for (String option : options) {
            JMenuItem item = new JMenuItem(option);
            item.setFont(Theme.font(Font.PLAIN, 14f));
            item.setBackground(Theme.WHITE);
            item.setForeground(Theme.DARK);
            item.setBorder(new EmptyBorder(8, 12, 8, 12));
            item.addActionListener(e -> {
                this.value = option;
                setText(option);
                repaint();
                this.onChange.accept(option);
            });
            menu.add(item);
        }
        addActionListener(e -> menu.show(this, 0, getHeight() + 2));
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
        return new Dimension(170, 44);
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
            g2.setColor(Theme.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(hover ? Theme.PURPLE : Theme.BORDER_INPUT);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g2.setColor(Theme.DARK);
            g2.setFont(Theme.font(Font.PLAIN, 16f));
            int textY = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(value, 12, textY);

            IconPainter.paint(g2, "chevron-down", getWidth() - 28, (getHeight() - 16) / 2, 16,
                    Theme.GRAY, 2f);

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
