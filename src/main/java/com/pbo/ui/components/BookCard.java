package com.pbo.ui.components;

import com.pbo.model.Book;
import com.pbo.ui.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/** Kartu buku ringkas untuk grid katalog. */
public class BookCard extends JPanel {

    private static final int COVER_HEIGHT = 230;

    public BookCard(Book book, String shelfStatus, Consumer<Book> onOpenDetail,
                    BiPredicate<Book, String> onSaveBook) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 4, 8, 4));

        RoundedPanel card = new RoundedPanel(Theme.WHITE, 12, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setToolTipText("Klik dua kali untuk membuka detail");

        BookCover cover = new BookCover(book.cover(), coverColor(book.category()));
        cover.setPreferredSize(new Dimension(0, COVER_HEIGHT));
        card.add(cover, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badges.setOpaque(false);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);
        badges.add(Badges.category(book.category()));
        body.add(badges);

        JLabel title = new JLabel(book.title());
        title.setFont(Theme.displayFont(Font.BOLD, 17f));
        title.setForeground(Theme.DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setToolTipText(book.title());
        body.add(Box.createVerticalStrut(8));
        body.add(title);

        JLabel author = new JLabel(book.author());
        author.setFont(Theme.font(Font.PLAIN, 13f));
        author.setForeground(Theme.TEXT_SECONDARY);
        author.setAlignmentX(Component.LEFT_ALIGNMENT);
        author.setToolTipText(book.author());
        body.add(Box.createVerticalStrut(3));
        body.add(author);

        JLabel meta = new JLabel(book.yearText() + "  ·  " + book.format() + "  ·  " + book.pagesText());
        meta.setFont(Theme.font(Font.PLAIN, 12f));
        meta.setForeground(Theme.GRAY);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(8));
        body.add(meta);

        body.add(Box.createVerticalGlue());

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        AppButton detail = new AppButton("Lihat Detail", AppButton.Style.PRIMARY, null, 36);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);
        detail.addActionListener(e -> onOpenDetail.accept(book));
        actions.add(detail);

        ShelfButton save = new ShelfButton(shelfStatus,
                status -> onSaveBook.test(book, status), 36);
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(Box.createVerticalStrut(6));
        actions.add(save);

        body.add(Box.createVerticalStrut(12));
        body.add(actions);

        card.add(body, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        addOpenOnDoubleClick(card, () -> onOpenDetail.accept(book));
        cover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void addOpenOnDoubleClick(java.awt.Component component, Runnable action) {
        if (component instanceof javax.swing.AbstractButton) {
            return;
        }
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    action.run();
                }
            }
        });
        if (component instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                addOpenOnDoubleClick(child, action);
            }
        }
    }

    static Color coverColor(String category) {
        return switch (category) {
            case "Teknologi", "Sejarah", "Sains" -> Theme.BLUE_LIGHT;
            case "Novel", "Komik" -> Theme.RED_LIGHT;
            default -> Theme.PURPLE_LIGHT;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.alpha(Theme.DARK, 6));
            g2.fillRoundRect(5, 6, getWidth() - 10, getHeight() - 11, 12, 12);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
