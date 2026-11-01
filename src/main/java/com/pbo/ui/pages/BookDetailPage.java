package com.pbo.ui.pages;

import com.pbo.model.Book;
import com.pbo.ui.Theme;
import com.pbo.ui.IconPainter;
import com.pbo.ui.components.AppButton;
import com.pbo.ui.components.Badges;
import com.pbo.ui.components.CompactBookCard;
import com.pbo.ui.components.ContentColumn;
import com.pbo.ui.components.DetailCover;
import com.pbo.ui.components.PagePanel;
import com.pbo.ui.components.RoundedPanel;
import com.pbo.ui.components.WrappedLabel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

/** Halaman detail buku: kartu utama berisi sampul, metadata, sinopsis, dan buku lain. */
public class BookDetailPage extends PagePanel {

    private final Book book;

    public BookDetailPage(Book book, List<Book> otherBooks, Runnable onBack,
                          Consumer<Book> onOpenBook, Consumer<Book> onSaveBook,
                          Consumer<Book> onReadBook) {
        this.book = book;

        add(Box.createVerticalStrut(20));

        ContentColumn column = new ContentColumn();
        column.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));

        column.add(navigation(onBack));
        column.add(Box.createVerticalStrut(16));
        column.add(detailCard(onSaveBook, onReadBook));

        if (!otherBooks.isEmpty()) {
            column.add(Box.createVerticalStrut(24));
            column.add(sectionTitle());
            column.add(Box.createVerticalStrut(12));
            column.add(otherBooks(otherBooks, onOpenBook));
            column.add(Box.createVerticalStrut(12));
        }

        column.add(Box.createVerticalStrut(36));
        add(column);
    }

    private JPanel navigation(Runnable onBack) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        AppButton back = new AppButton("Kembali ke Katalog", AppButton.Style.SECONDARY_OUTLINE,
                "arrow-left", 36);
        back.addActionListener(e -> onBack.run());
        row.add(back);

        JLabel path = new JLabel("Katalog Buku  /  " + book.category());
        path.setFont(Theme.font(Font.PLAIN, 13f));
        path.setForeground(Theme.GRAY);
        row.add(path);
        return row;
    }

    private JPanel detailCard(Consumer<Book> onSaveBook, Consumer<Book> onReadBook) {
        RoundedPanel card = new RoundedPanel(Theme.WHITE, 16, Theme.BORDER);
        card.setLayout(new BorderLayout(28, 0));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel coverWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        coverWrapper.setOpaque(false);
        coverWrapper.add(new DetailCover(book.cover()));
        card.add(coverWrapper, BorderLayout.WEST);
        card.add(infoPanel(onSaveBook, onReadBook), BorderLayout.CENTER);
        return card;
    }

    private JPanel infoPanel(Consumer<Book> onSaveBook, Consumer<Book> onReadBook) {
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel category = Badges.category(book.category());
        category.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel categoryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        categoryRow.setOpaque(false);
        categoryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryRow.add(category);
        fixedHeight(categoryRow);
        info.add(categoryRow);
        info.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel(book.title());
        float titleSize = book.title().length() > 24 ? 28f : 34f;
        title.setFont(Theme.displayFont(Font.BOLD, titleSize));
        title.setForeground(Theme.DARK);

        AppButton infoButton = new AppButton("Informasi Buku", AppButton.Style.SECONDARY_OUTLINE,
                "info", 34);
        infoButton.setToolTipText("Lihat informasi lengkap buku");
        infoButton.addActionListener(e -> showInformation());

        JPanel titleRow = new JPanel(new GridBagLayout());
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridy = 0;
        titleConstraints.anchor = GridBagConstraints.WEST;
        titleConstraints.gridx = 0;
        titleConstraints.weightx = 0;
        titleRow.add(title, titleConstraints);
        titleConstraints.gridx = 1;
        titleConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        titleRow.add(infoButton, titleConstraints);
        titleConstraints.gridx = 2;
        titleConstraints.weightx = 1;
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleRow.add(Box.createHorizontalGlue(), titleConstraints);
        fixedHeight(titleRow);
        info.add(titleRow);

        JLabel author = new JLabel(book.author());
        author.setFont(Theme.font(Font.PLAIN, 17f));
        author.setForeground(Theme.TEXT_SECONDARY);
        author.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedHeight(author);
        info.add(Box.createVerticalStrut(2));
        info.add(author);

        JPanel meta = metaBadges();
        if (meta.getComponentCount() > 0) {
            fixedHeight(meta);
            info.add(Box.createVerticalStrut(10));
            info.add(meta);
        }

        info.add(Box.createVerticalStrut(18));
        info.add(divider());
        info.add(Box.createVerticalStrut(14));

        JLabel synopsisTitle = new JLabel("Sinopsis");
        synopsisTitle.setFont(Theme.displayFont(Font.BOLD, 17f));
        synopsisTitle.setForeground(Theme.DARK);
        synopsisTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedHeight(synopsisTitle);
        info.add(synopsisTitle);

        String synopsis = book.synopsis().isBlank()
                ? "Sinopsis belum tersedia untuk buku ini."
                : book.synopsis();
        WrappedLabel description = new WrappedLabel(synopsis, 420, 26);
        description.setFont(Theme.displayFont(Font.PLAIN, 15f));
        description.setForeground(Theme.TEXT_SECONDARY);
        info.add(Box.createVerticalStrut(6));
        info.add(description);

        AppButton read = new AppButton("Baca Buku", AppButton.Style.GREEN, "book", 42);
        read.addActionListener(e -> onReadBook.accept(book));

        AppButton save = new AppButton("Simpan ke Rak", AppButton.Style.BLUE, "bookmark-plus", 42);
        save.addActionListener(e -> onSaveBook.accept(book));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(read);
        actions.add(save);
        fixedHeight(actions);
        info.add(Box.createVerticalStrut(20));
        info.add(actions);

        info.add(Box.createVerticalStrut(14));
        info.add(readingHint());
        info.add(Box.createVerticalGlue());
        return info;
    }

    private JPanel readingHint() {
        RoundedPanel hint = new RoundedPanel(Theme.BACKGROUND, 10, Theme.BORDER_SUBTLE);
        hint.setLayout(new BorderLayout(10, 0));
        hint.setBorder(new EmptyBorder(10, 12, 10, 12));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(IconPainter.icon("book", 20, Theme.PURPLE));
        hint.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("Cara membaca");
        title.setFont(Theme.font(Font.BOLD, 12f));
        title.setForeground(Theme.DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        WrappedLabel body = new WrappedLabel(
                "Gunakan Baca Buku untuk membuka berkas, atau Simpan ke Rak untuk membaca nanti.",
                430, 17);
        body.setFont(Theme.font(Font.PLAIN, 12f));
        body.setForeground(Theme.GRAY);

        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(body);
        hint.add(text, BorderLayout.CENTER);
        fixedHeight(hint);
        return hint;
    }

    private JPanel metaBadges() {
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (book.year() != null) {
            meta.add(Badges.meta("Tahun: " + book.year()));
        }
        if (book.pages() != null) {
            meta.add(Badges.meta(book.pagesText()));
        }
        if (!"-".equals(book.format())) {
            meta.add(Badges.meta(book.format()));
        }
        if (!"-".equals(book.isbnText())) {
            meta.add(Badges.meta("ISBN: " + book.isbnText()));
        }
        return meta;
    }

    private static void fixedHeight(javax.swing.JComponent component) {
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    private JPanel otherBooks(List<Book> otherBooks, Consumer<Book> onOpenBook) {
        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < 3; i++) {
            if (i < otherBooks.size()) {
                grid.add(new CompactBookCard(otherBooks.get(i), onOpenBook));
            } else {
                JPanel filler = new JPanel();
                filler.setOpaque(false);
                grid.add(filler);
            }
        }
        return grid;
    }

    private static JLabel sectionTitle() {
        JLabel label = new JLabel("Buku Lainnya");
        label.setFont(Theme.displayFont(Font.BOLD, 20f));
        label.setForeground(Theme.DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JPanel divider() {
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
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private void showInformation() {
        BookInfoDialog dialog = new BookInfoDialog(SwingUtilities.getWindowAncestor(this), book);
        dialog.setVisible(true);
    }
}
