package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Sidebar tetap di kiri, pengganti navbar horizontal gaya web. */
public class AppSidebar extends JPanel {

    private final Map<String, SidebarItem> items = new LinkedHashMap<>();

    public AppSidebar(Runnable onCatalog, Runnable onSearch, String userName, boolean connected) {
        setLayout(new BorderLayout());
        setBackground(Theme.WHITE);
        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
        setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 12, 16, 12));

        body.add(brand(onCatalog));
        body.add(Box.createVerticalStrut(24));
        body.add(sectionLabel());
        body.add(Box.createVerticalStrut(8));

        SidebarItem catalog = item("katalog", "Katalog Buku", "grid", onCatalog);
        catalog.setToolTipText("Katalog buku");
        SidebarItem search = item("search", "Cari Buku", "search", onSearch);
        search.setToolTipText("Cari & filter buku (Ctrl+K)");
        body.add(catalog);
        body.add(Box.createVerticalStrut(2));
        body.add(search);

        body.add(Box.createVerticalGlue());
        body.add(separator());
        body.add(Box.createVerticalStrut(12));
        body.add(databaseStatus(connected));
        body.add(Box.createVerticalStrut(10));
        body.add(account(userName));

        add(body, BorderLayout.CENTER);
        setActive("katalog");
    }

    public void setActive(String key) {
        items.forEach((name, item) -> item.setActive(name.equals(key)));
    }

    private SidebarItem item(String key, String label, String icon, Runnable action) {
        SidebarItem item = new SidebarItem(label, icon);
        item.addActionListener(e -> action.run());
        items.put(key, item);
        return item;
    }

    private static JPanel brand(Runnable onCatalog) {
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        brand.add(new LogoBadge(36));
        brand.add(Box.createHorizontalStrut(10));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel name = new JLabel("PustakaDigital");
        name.setFont(Theme.displayFont(Font.BOLD, 17f));
        name.setForeground(Theme.DARK);
        name.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Perpustakaan digital");
        tagline.setFont(Theme.font(Font.PLAIN, 11f));
        tagline.setForeground(Theme.GRAY);
        tagline.setAlignmentX(LEFT_ALIGNMENT);

        text.add(name);
        text.add(tagline);
        brand.add(text);
        brand.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        brand.setToolTipText("Kembali ke katalog");
        brand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onCatalog.run();
            }
        });
        return brand;
    }

    private static JLabel sectionLabel() {
        JLabel label = new JLabel("MENU");
        label.setFont(Theme.font(Font.BOLD, 11f));
        label.setForeground(Theme.PLACEHOLDER);
        label.setBorder(new EmptyBorder(0, 12, 0, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JComponent separator() {
        JPanel line = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1, 1);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 1);
            }
        };
        line.setOpaque(false);
        line.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        line.setAlignmentX(LEFT_ALIGNMENT);
        return line;
    }

    private static JPanel databaseStatus(boolean connected) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        Color color = connected ? Theme.GREEN_DARK : Theme.RED_DARK;
        JComponent dot = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(8, 8);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillOval(0, 0, 8, 8);
                } finally {
                    g2.dispose();
                }
            }
        };
        row.add(dot);

        JLabel text = new JLabel(connected ? "MySQL · db_pustaka" : "Database terputus");
        text.setFont(Theme.font(Font.PLAIN, 12f));
        text.setForeground(connected ? Theme.GRAY : Theme.RED_DARK);
        row.add(text);
        return row;
    }

    private static JPanel account(String userName) {
        RoundedPanel card = new RoundedPanel(Theme.BACKGROUND, 10, Theme.BORDER_SUBTLE) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 52);
            }
        };
        card.setLayout(new BorderLayout(9, 0));
        card.setBorder(new EmptyBorder(8, 9, 8, 9));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setToolTipText("Rak akun demo " + userName);

        card.add(avatar(userName), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel name = new JLabel(userName);
        name.setFont(Theme.font(Font.BOLD, 13f));
        name.setForeground(Theme.DARK);
        name.setAlignmentX(LEFT_ALIGNMENT);

        JLabel role = new JLabel("Anggota");
        role.setFont(Theme.font(Font.PLAIN, 11f));
        role.setForeground(Theme.GRAY);
        role.setAlignmentX(LEFT_ALIGNMENT);

        text.add(name);
        text.add(role);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private static JComponent avatar(String userName) {
        String initial = userName == null || userName.isBlank()
                ? "A" : userName.substring(0, 1).toUpperCase();
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
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
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(Theme.PURPLE_LIGHT);
                    g2.fillOval(0, 0, 32, 32);
                    g2.setColor(Theme.PURPLE);
                    g2.setFont(Theme.font(Font.BOLD, 15f));
                    int x = (32 - g2.getFontMetrics().stringWidth(initial)) / 2;
                    int y = (32 - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                    g2.drawString(initial, x, y);
                } finally {
                    g2.dispose();
                }
            }
        };
    }
}
