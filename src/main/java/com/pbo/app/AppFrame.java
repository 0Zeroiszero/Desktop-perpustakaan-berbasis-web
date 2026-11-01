package com.pbo.app;

import com.pbo.data.BookRepository;
import com.pbo.data.ShelfRepository;
import com.pbo.model.Book;
import com.pbo.ui.Assets;
import com.pbo.ui.Theme;
import com.pbo.ui.components.AppSidebar;
import com.pbo.ui.components.PagePanel;
import com.pbo.ui.components.ToastLayer;
import com.pbo.ui.pages.BookDetailPage;
import com.pbo.ui.pages.CatalogPage;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/** Jendela utama: sidebar tetap di kiri, halaman isi dapat digulir. */
public class AppFrame extends JFrame {

    private final JScrollPane scroll = new JScrollPane();
    private final ToastLayer toastLayer = new ToastLayer();
    private final AppSidebar sidebar;

    private List<Book> books = List.of();
    private List<String> categories = List.of();
    private String userName = "Anggota";
    private boolean connected;
    private SQLException connectionError;
    private CatalogPage catalogPage;

    public AppFrame() {
        super("Katalog - PustakaDigital");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setIconImage(appIcon());

        loadDatabase();

        setLayout(new BorderLayout());
        sidebar = new AppSidebar(this::openCatalog, this::openSearch, userName, connected);
        add(sidebar, BorderLayout.WEST);

        scroll.setBorder(null);
        scroll.setBackground(Theme.BACKGROUND);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        setGlassPane(toastLayer);
        toastLayer.setVisible(false);

        installSearchShortcut();
        openCatalog();

        if (!connected) {
            SQLException error = connectionError;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "Tidak dapat terhubung ke database \"db_pustaka\" di localhost:3306.\n"
                            + "Pastikan MySQL Laragon sudah berjalan, lalu buka ulang aplikasi.\n\n"
                            + (error == null ? "" : error.getMessage()),
                    "Koneksi database gagal", JOptionPane.ERROR_MESSAGE));
        }
    }

    public void openCatalog() {
        if (catalogPage == null) {
            catalogPage = new CatalogPage(books, categories, this::openDetail, this::saveToShelf);
        }
        showPage(catalogPage, "Katalog Buku - PustakaDigital");
        sidebar.setActive("katalog");
    }

    public void openDetail(Book book) {
        List<Book> otherBooks = books.stream()
                .filter(candidate -> !candidate.id().equals(book.id()))
                .sorted(Comparator.comparing((Book candidate) -> !candidate.category().equals(book.category()))
                        .thenComparing(Book::title, String.CASE_INSENSITIVE_ORDER))
                .limit(3)
                .toList();
        BookDetailPage page = new BookDetailPage(book, otherBooks, this::openCatalog, this::openDetail,
                this::saveToShelf, this::readBook);
        showPage(page, "Detail Buku - PustakaDigital");
        sidebar.setActive("katalog");
    }

    private void openSearch() {
        if (catalogPage == null) {
            openCatalog();
        }
        catalogPage.openSearchDialog();
    }

    private void showPage(PagePanel page, String title) {
        scroll.setViewportView(page);
        setTitle(title);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            scroll.getViewport().setViewPosition(new java.awt.Point(0, 0));
            scroll.getVerticalScrollBar().setValue(0);
        });
    }

    private void saveToShelf(Book book) {
        try {
            ShelfRepository.Result result = ShelfRepository.save(book);
            toastLayer.showMessage(result == ShelfRepository.Result.SAVED
                    ? "Buku \"" + book.title() + "\" disimpan ke rak " + userName + "."
                    : "Buku \"" + book.title() + "\" sudah ada di rak " + userName + ".");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void readBook(Book book) {
        try {
            Assets.open(book.file());
            toastLayer.showMessage("Membuka \"" + book.title() + "\".");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Buku tidak dapat dibuka", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showDatabaseError(SQLException error) {
        JOptionPane.showMessageDialog(this,
                "Gagal menyimpan buku ke rak.\n\n" + error.getMessage(),
                "Kesalahan database", JOptionPane.ERROR_MESSAGE);
    }

    private void loadDatabase() {
        try {
            books = BookRepository.findAll();
            categories = BookRepository.findCategories();
            userName = ShelfRepository.currentUserName();
            connected = true;
        } catch (SQLException e) {
            connected = false;
            connectionError = e;
            books = List.of();
            categories = List.of();
        }
    }

    private void installSearchShortcut() {
        KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(shortcut, "cariBuku");
        getRootPane().getActionMap().put("cariBuku", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSearch();
            }
        });
    }

    private static BufferedImage appIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.PURPLE);
            g2.fillRoundRect(0, 0, 64, 64, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 40f));
            g2.drawString("P", 19, 47);
        } finally {
            g2.dispose();
        }
        return image;
    }
}
