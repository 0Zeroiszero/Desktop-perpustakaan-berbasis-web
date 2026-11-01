package com.pbo.ui.pages;

import com.pbo.model.Book;
import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;
import com.pbo.ui.components.AppButton;
import com.pbo.ui.components.Badges;
import com.pbo.ui.components.BookCard;
import com.pbo.ui.components.ContentColumn;
import com.pbo.ui.components.PagePanel;
import com.pbo.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

/** Halaman katalog: header ringkas, filter multi-kategori, dan grid kartu buku. */
public class CatalogPage extends PagePanel {

    private static final int COLUMNS = 4;

    private final List<Book> books;
    private final List<String> categories;
    private final Consumer<Book> onOpenDetail;
    private final Function<String, String> shelfStatusOf;
    private final BiPredicate<Book, String> onSaveBook;

    private final Set<String> selectedCategories = new LinkedHashSet<>();
    private final JPanel grid = new JPanel(new GridLayout(0, COLUMNS, 20, 20));
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardArea = new JPanel(cardLayout);
    private final JLabel countBadge = Badges.count("0 Buku");
    private final JLabel filterInfo = new JLabel();
    private final AppButton resetFilter = new AppButton("Reset", AppButton.Style.SECONDARY_OUTLINE, "x", 30);

    private RoundedPanel headerCard;
    private JPanel filterRow;
    private String keyword = "";
    private String sort = "Judul A-Z";

    public CatalogPage(List<Book> books, List<String> categories,
                       Function<String, String> shelfStatusOf, Consumer<Book> onOpenDetail,
                       BiPredicate<Book, String> onSaveBook) {
        this.books = books;
        this.categories = categories;
        this.shelfStatusOf = shelfStatusOf;
        this.onOpenDetail = onOpenDetail;
        this.onSaveBook = onSaveBook;

        add(Box.createVerticalStrut(28));

        ContentColumn column = new ContentColumn();
        column.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));

        column.add(buildHeader());
        column.add(Box.createVerticalStrut(18));

        grid.setOpaque(false);
        cardArea.setOpaque(false);
        cardArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardArea.add(grid, "grid");
        cardArea.add(emptyState(), "empty");
        column.add(cardArea);
        column.add(Box.createVerticalStrut(40));
        add(column);

        applyFilter();
    }

    public void openSearchDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        SearchDialog dialog = new SearchDialog(owner, categories, keyword, selectedCategories, sort,
                (kata, kategori, urutan) -> {
                    keyword = kata;
                    selectedCategories.clear();
                    selectedCategories.addAll(kategori);
                    sort = urutan;
                    applyFilter();
                });
        dialog.setVisible(true);
    }

    private JPanel buildHeader() {
        headerCard = new RoundedPanel(Theme.WHITE, 14, Theme.BORDER) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        headerCard.setLayout(new BoxLayout(headerCard, BoxLayout.Y_AXIS));
        headerCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Katalog Buku");
        title.setFont(Theme.displayFont(Font.BOLD, 30f));
        title.setForeground(Theme.DARK);
        titleRow.add(title);
        titleRow.add(countBadge);

        AppButton search = new AppButton("Cari & Filter", AppButton.Style.SECONDARY_OUTLINE, "search", 38);
        search.addActionListener(e -> openSearchDialog());

        top.add(titleRow, BorderLayout.WEST);
        top.add(search, BorderLayout.EAST);
        headerCard.add(top);
        headerCard.add(Box.createVerticalStrut(3));

        JLabel subtitle = new JLabel("Jelajahi koleksi e-book perpustakaan digital.");
        subtitle.setFont(Theme.font(Font.PLAIN, 14f));
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerCard.add(subtitle);

        filterRow = new JPanel(new BorderLayout(12, 0));
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        filterRow.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(11, 0, 0, 0)
        ));
        filterRow.setVisible(false);

        filterInfo.setFont(Theme.font(Font.PLAIN, 13f));
        filterInfo.setForeground(Theme.GRAY);
        filterRow.add(filterInfo, BorderLayout.CENTER);

        resetFilter.setToolTipText("Kembalikan pencarian dan filter ke awal");
        resetFilter.addActionListener(e -> resetFilters());
        JPanel resetWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        resetWrapper.setOpaque(false);
        resetWrapper.add(resetFilter);
        filterRow.add(resetWrapper, BorderLayout.EAST);

        headerCard.add(filterRow);
        return headerCard;
    }

    private JPanel emptyState() {
        RoundedPanel card = new RoundedPanel(Theme.WHITE, 12, Theme.BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(44, 40, 44, 40));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel icon = new JLabel(IconPainter.icon("search", 44, Theme.GRAY));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Buku tidak ditemukan");
        title.setFont(Theme.displayFont(Font.BOLD, 20f));
        title.setForeground(Theme.DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Coba kata kunci lain atau pilih kategori lain.");
        hint.setFont(Theme.font(Font.PLAIN, 14f));
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        AppButton reset = new AppButton("Reset Filter", AppButton.Style.PRIMARY, null, 36);
        reset.addActionListener(e -> resetFilters());
        JPanel resetWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        resetWrapper.setOpaque(false);
        resetWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetWrapper.add(reset);

        content.add(icon);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(hint);
        content.add(Box.createVerticalStrut(14));
        content.add(resetWrapper);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        card.add(content, constraints);
        return card;
    }

    /** Bangun ulang kartu agar status rak terbaru ikut tampil. */
    public void refresh() {
        applyFilter();
    }

    private void resetFilters() {
        keyword = "";
        selectedCategories.clear();
        sort = "Judul A-Z";
        applyFilter();
    }

    private void applyFilter() {
        List<Book> result = new ArrayList<>(books);

        if (!selectedCategories.isEmpty()) {
            result.removeIf(book -> !selectedCategories.contains(book.category()));
        }

        if (!keyword.isBlank()) {
            String query = keyword.toLowerCase();
            result.removeIf(book -> !(book.title() + " " + book.author() + " " + book.category())
                    .toLowerCase().contains(query));
        }

        switch (sort) {
            case "Tahun Terbaru" -> result.sort(
                    Comparator.comparingInt((Book b) -> b.year() == null ? 0 : b.year()).reversed()
                            .thenComparing(Book::title, String.CASE_INSENSITIVE_ORDER));
            case "Tahun Terlama" -> result.sort(
                    Comparator.comparingInt((Book b) -> b.year() == null ? Integer.MAX_VALUE : b.year())
                            .thenComparing(Book::title, String.CASE_INSENSITIVE_ORDER));
            default -> result.sort(Comparator.comparing(Book::title, String.CASE_INSENSITIVE_ORDER));
        }

        grid.removeAll();
        for (Book book : result) {
            grid.add(new BookCard(book, shelfStatusOf.apply(book.id()), onOpenDetail, onSaveBook));
        }
        for (int i = result.size(); i < Math.max(COLUMNS, ((result.size() + COLUMNS - 1) / COLUMNS) * COLUMNS); i++) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            grid.add(filler);
        }

        countBadge.setText(result.size() + " Buku");
        updateFilterInfo();
        cardLayout.show(cardArea, result.isEmpty() ? "empty" : "grid");
        grid.revalidate();
        grid.repaint();
        cardArea.revalidate();
        cardArea.repaint();
    }

    private void updateFilterInfo() {
        boolean hasCategories = !selectedCategories.isEmpty();
        boolean hasKeyword = !keyword.isBlank();
        boolean active = hasCategories || hasKeyword;

        filterRow.setVisible(active);
        if (!active) {
            filterInfo.setText("");
            filterInfo.setToolTipText(null);
            headerCard.revalidate();
            return;
        }

        StringBuilder text = new StringBuilder("Filter aktif: ");
        if (!selectedCategories.isEmpty()) {
            if (selectedCategories.size() <= 3) {
                text.append(String.join(", ", selectedCategories));
            } else {
                int shown = 0;
                for (String name : selectedCategories) {
                    if (shown == 3) {
                        break;
                    }
                    if (shown > 0) {
                        text.append(", ");
                    }
                    text.append(name);
                    shown++;
                }
                text.append(" +").append(selectedCategories.size() - 3);
            }
        }
        if (hasKeyword) {
            if (hasCategories) {
                text.append(" · ");
            }
            text.append('"').append(keyword).append('"');
        }

        filterInfo.setText(text.toString());
        filterInfo.setToolTipText(hasCategories ? String.join(", ", selectedCategories) : null);
        headerCard.revalidate();
    }
}
