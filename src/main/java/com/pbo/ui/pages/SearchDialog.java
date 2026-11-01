package com.pbo.ui.pages;

import com.pbo.ui.Theme;
import com.pbo.ui.components.CategoryButton;
import com.pbo.ui.components.RoundedTextField;
import com.pbo.ui.components.SelectButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Dialog "Cari & filter buku"; kategori bisa dipilih lebih dari satu. */
public class SearchDialog extends JDialog {

    public interface FilterListener {
        void onFilter(String keyword, Set<String> categories, String sort);
    }

    private static final String SORT_DEFAULT = "Judul A-Z";

    private final RoundedTextField searchField;
    private final FilterListener listener;
    private final List<CategoryButton> categoryButtons = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final Set<String> selectedCategories = new LinkedHashSet<>();
    private String sort;

    public SearchDialog(Window owner, List<String> categoryNames, String keyword,
                        Set<String> selectedCategories, String sort, FilterListener listener) {
        super(owner, "Cari & filter buku", ModalityType.APPLICATION_MODAL);
        this.sort = sort == null ? SORT_DEFAULT : sort;
        this.listener = listener;
        this.selectedCategories.addAll(selectedCategories);

        categories.add("Semua");
        categories.addAll(categoryNames);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Theme.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Cari & filter buku");
        title.setFont(Theme.displayFont(Font.BOLD, 22f));
        title.setForeground(Theme.DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);
        root.add(title);

        JLabel subtitle = new JLabel("Cari berdasarkan judul, penulis, atau kategori.");
        subtitle.setFont(Theme.font(Font.PLAIN, 13f));
        subtitle.setForeground(Theme.GRAY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        root.add(Box.createVerticalStrut(3));
        root.add(subtitle);
        root.add(Box.createVerticalStrut(16));

        searchField = new RoundedTextField("Cari judul, penulis...");
        searchField.setText(keyword);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(new SelectButton(
                new String[]{SORT_DEFAULT, "Tahun Terbaru", "Tahun Terlama"},
                this.sort,
                value -> {
                    this.sort = value;
                    emit();
                }
        ), BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        searchRow.setAlignmentX(LEFT_ALIGNMENT);
        root.add(searchRow);

        JLabel hint = new JLabel("Pilih satu atau lebih kategori. Hasil diperbarui otomatis.");
        hint.setFont(Theme.font(Font.PLAIN, 12f));
        hint.setForeground(Theme.GRAY);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        root.add(Box.createVerticalStrut(8));
        root.add(hint);

        JPanel pills = new JPanel(new GridLayout(0, 4, 8, 8));
        pills.setOpaque(false);
        pills.setAlignmentX(LEFT_ALIGNMENT);
        int rows = (categories.size() + 3) / 4;
        pills.setMaximumSize(new Dimension(Integer.MAX_VALUE, rows * 40 + (rows - 1) * 8));
        for (String name : categories) {
            CategoryButton button = new CategoryButton(name);
            button.addActionListener(e -> selectCategory(name));
            categoryButtons.add(button);
            pills.add(button);
        }
        refreshCategoryButtons();
        root.add(Box.createVerticalStrut(14));
        root.add(pills);
        root.add(Box.createVerticalGlue());

        setContentPane(root);
        setSize(840, 350);
        setLocationRelativeTo(owner);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                emit();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                emit();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                emit();
            }
        });

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                searchField.requestFocusInWindow();
            }
        });
    }

    private void selectCategory(String name) {
        if ("Semua".equals(name)) {
            selectedCategories.clear();
        } else if (!selectedCategories.remove(name)) {
            selectedCategories.add(name);
        }
        refreshCategoryButtons();
        emit();
    }

    private void refreshCategoryButtons() {
        for (int i = 0; i < categoryButtons.size(); i++) {
            String name = categories.get(i);
            boolean active = "Semua".equals(name)
                    ? selectedCategories.isEmpty()
                    : selectedCategories.contains(name);
            categoryButtons.get(i).setActive(active);
        }
    }

    private void emit() {
        if (listener == null || searchField == null) {
            return;
        }
        listener.onFilter(searchField.getText().trim(), new LinkedHashSet<>(selectedCategories), sort);
    }
}
