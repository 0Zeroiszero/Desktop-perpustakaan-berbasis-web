package com.pbo.ui.pages;

import com.pbo.model.Book;
import com.pbo.ui.Theme;
import com.pbo.ui.components.WrappedLabel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

/** Dialog informasi lengkap; tombol tutup memakai title bar jendela. */
public class BookInfoDialog extends JDialog {

    public BookInfoDialog(Window owner, Book book) {
        super(owner, "Informasi Buku - " + book.title(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Theme.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("Informasi Buku");
        title.setFont(Theme.displayFont(Font.BOLD, 22f));
        title.setForeground(Theme.DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3 * 44 + 2 * 14));
        grid.add(item("Penulis", book.author()));
        grid.add(item("Kategori", book.category()));
        grid.add(item("Tahun", book.yearText()));
        grid.add(item("Halaman", book.pagesText()));
        grid.add(item("ISBN", book.isbnText()));
        grid.add(item("Format", book.format()));
        root.add(grid);
        root.add(Box.createVerticalStrut(20));

        WrappedLabel note = new WrappedLabel(
                "Buku tersedia dalam koleksi digital PustakaDigital dan dapat dibaca langsung "
                        + "melalui aplikasi ini.",
                660, 24);
        note.setFont(Theme.font(Font.PLAIN, 14f));
        note.setForeground(Theme.TEXT_SECONDARY);
        root.add(note);
        root.add(Box.createVerticalGlue());

        setContentPane(root);
        setSize(760, 330);
        setLocationRelativeTo(owner);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private static JPanel item(String label, String value) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);

        JLabel name = new JLabel(label);
        name.setFont(Theme.font(Font.PLAIN, 13f));
        name.setForeground(Theme.GRAY);
        name.setAlignmentX(LEFT_ALIGNMENT);

        JLabel text = new JLabel(value);
        text.setFont(Theme.font(Font.BOLD, 15f));
        text.setForeground(Theme.DARK);
        text.setAlignmentX(LEFT_ALIGNMENT);

        item.add(name);
        item.add(Box.createVerticalStrut(4));
        item.add(text);
        item.setPreferredSize(new Dimension(310, 44));
        return item;
    }
}
