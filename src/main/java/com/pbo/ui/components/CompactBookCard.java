package com.pbo.ui.components;

import com.pbo.model.Book;
import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Kartu ringkas untuk bagian "Buku Lainnya" di halaman detail. */
public class CompactBookCard extends JButton {

    public CompactBookCard(Book book, Consumer<Book> onOpen) {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setLayout(new BorderLayout());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName("Buka detail " + book.title());
        setToolTipText(book.title() + " — " + book.author());

        RoundedPanel card = new RoundedPanel(Theme.WHITE, 10, Theme.BORDER);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(9, 9, 9, 9));

        BookCover cover = new BookCover(book.cover(), BookCard.coverColor(book.category()), true);
        cover.setPreferredSize(new Dimension(64, 84));
        card.add(cover, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        WrappedLabel title = new WrappedLabel(book.title(), 145, 17);
        title.setFont(Theme.displayFont(Font.BOLD, 14f));
        title.setForeground(Theme.DARK);

        JLabel author = new JLabel(book.author());
        author.setFont(Theme.font(Font.PLAIN, 11f));
        author.setForeground(Theme.TEXT_SECONDARY);
        author.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(author);
        card.add(text, BorderLayout.CENTER);

        JLabel arrow = new JLabel(IconPainter.icon("chevron-right", 16, Theme.PLACEHOLDER));
        card.add(arrow, BorderLayout.EAST);

        add(card);
        addActionListener(e -> onOpen.accept(book));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                card.setBorderColor(Theme.PURPLE);
            }

            @Override
            public void focusLost(FocusEvent e) {
                card.setBorderColor(Theme.BORDER);
            }
        });

        forwardClicks(card);
    }

    private void forwardClicks(Component component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    doClick();
                }
            }
        });
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                forwardClicks(child);
            }
        }
    }
}
