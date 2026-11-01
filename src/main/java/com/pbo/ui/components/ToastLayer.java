package com.pbo.ui.components;

import com.pbo.ui.IconPainter;
import com.pbo.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Notifikasi kecil di kanan bawah, sama seperti toast "Buku disimpan ke rak". */
public class ToastLayer extends JPanel {

    private final RoundedPanel toast;
    private final JLabel message = new JLabel();
    private final Timer timer;

    public ToastLayer() {
        setOpaque(false);
        setLayout(null);

        toast = new RoundedPanel(Theme.DARK, 12, null);
        toast.setLayout(new BorderLayout(0, 0));
        toast.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 8));

        RoundedPanel check = new RoundedPanel(Theme.PURPLE, 8, null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    IconPainter.paint(g2, "check", 8, 8, 16, Theme.WHITE, 2.4f);
                } finally {
                    g2.dispose();
                }
            }
        };
        check.setPreferredSize(new Dimension(32, 32));
        toast.add(check, BorderLayout.WEST);

        message.setFont(Theme.font(Font.PLAIN, 14f));
        message.setForeground(Theme.WHITE);
        message.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        toast.add(message, BorderLayout.CENTER);

        IconButton close = new IconButton("x", IconButton.Style.DARK, 14, 28, 28,
                "Tutup notifikasi");
        close.addActionListener(e -> dismiss());
        toast.add(close, BorderLayout.EAST);

        add(toast);
        timer = new Timer(3000, e -> dismiss());
        timer.setRepeats(false);
    }

    public void showMessage(String text) {
        message.setText(text);
        toast.invalidate();
        setVisible(true);
        doLayout();
        repaint();
        timer.restart();
    }

    private void dismiss() {
        timer.stop();
        setVisible(false);
    }

    @Override
    public boolean contains(int x, int y) {
        return toast != null && toast.getBounds().contains(x, y);
    }

    @Override
    public void doLayout() {
        if (toast == null) {
            return;
        }
        Dimension preferred = toast.getPreferredSize();
        int x = Math.max(16, getWidth() - preferred.width - 24);
        int y = Math.max(16, getHeight() - preferred.height - 24);
        toast.setBounds(x, y, preferred.width, preferred.height);
    }
}
