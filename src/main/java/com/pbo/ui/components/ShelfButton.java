package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Tombol "Tambah ke rak" dengan status baca pengguna seperti dropdown di web. */
public class ShelfButton extends AppButton {

    public static final String[] STATUSES = {
            "Akan dibaca", "Sedang dibaca", "Telah dibaca", "Tidak selesai"
    };

    private final JPopupMenu menu = new JPopupMenu();
    private final Map<String, StatusItem> menuItems = new LinkedHashMap<>();

    public ShelfButton(String initialStatus, Predicate<String> onChoose, int buttonHeight) {
        super("Tambah ke rak", Style.SECONDARY_OUTLINE, null, buttonHeight);
        setBorder(new EmptyBorder(0, 18, 0, 44));

        menu.setBackground(Theme.WHITE);
        menu.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(4, 4, 4, 4)));
        for (String option : STATUSES) {
            StatusItem item = new StatusItem(option);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, item.getPreferredSize().height));
            item.addActionListener(e -> {
                if (onChoose.test(option)) {
                    setStatus(option);
                }
            });
            menuItems.put(option, item);
            menu.add(item);
        }
        setStatus(initialStatus);

        addActionListener(e -> {
            menu.setPreferredSize(null);
            Dimension preferred = menu.getPreferredSize();
            menu.setPreferredSize(new Dimension(Math.max(getWidth(), preferred.width + 24),
                    preferred.height));
            menu.show(this, 0, getHeight() + 4);
        });
    }

    private void setStatus(String status) {
        setText(status == null ? "Tambah ke rak" : status);
        menuItems.forEach((option, item) -> item.setActive(option.equals(status)));
        setToolTipText(status == null
                ? "Pilih status baca lalu tambahkan ke rak"
                : "Status di rak: " + status + ". Klik untuk mengubah.");
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            IconPainter.paint(g2, "chevron-down", getWidth() - 30, (getHeight() - 16) / 2, 16,
                    getForeground(), 2f);
        } finally {
            g2.dispose();
        }
    }

    /** Item menu digambar manual agar teks rata kiri dan warnanya sama di semua Look and Feel. */
    private static final class StatusItem extends JMenuItem {

        private boolean hover;
        private boolean active;

        StatusItem(String text) {
            super(text);
            setFont(Theme.font(Font.PLAIN, 15f));
            setBorder(new EmptyBorder(9, 14, 9, 14));
            setOpaque(false);
            setContentAreaFilled(false);
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

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(hover || isArmed() ? Theme.HOVER : Theme.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(getFont());
                g2.setColor(Theme.DARK);
                FontMetrics metrics = g2.getFontMetrics();
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g2.drawString(getText(), 14, y);
                if (active) {
                    IconPainter.paint(g2, "check", getWidth() - 28,
                            (getHeight() - 16) / 2, 16, Theme.PURPLE, 2.2f);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
