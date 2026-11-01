import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * PustakaDigital - versi aplikasi desktop Java Swing (tampilan mengikuti versi web).
 * Satu file, tanpa library tambahan, ukuran jendela tetap (tanpa scroll).
 *
 * Akun demo:
 *   Admin : admin@gmail.com   / admin123  (Masuk sebagai: Admin)
 *   User  : najwa@gmail.com   / user123   (Masuk sebagai: User)
 *   User  : jichang@gmail.com / user123   (Masuk sebagai: User)
 */
public class PustakaDigitalApp extends JFrame {

    // ======================= TEMA =======================
    static final Color PRIMER = new Color(124, 58, 237);
    static final Color UNGU_MUDA = new Color(237, 233, 254);
    static final Color GELAP = new Color(30, 27, 75);
    static final Color ABU = new Color(107, 114, 128);
    static final Color TEKS = new Color(31, 41, 55);
    static final Color GARIS = new Color(233, 229, 245);
    static final Color GARIS_INPUT = new Color(209, 213, 219);
    static final Color LATAR = new Color(250, 249, 255);
    static final Color HIJAU_BG = new Color(209, 250, 229);
    static final Color HIJAU_TX = new Color(6, 95, 70);
    static final Color MERAH_BG = new Color(254, 205, 211);
    static final Color MERAH_TX = new Color(159, 18, 57);
    static final Color HIJAU_BTN = new Color(21, 128, 61);
    static final Color MERAH = new Color(220, 38, 38);

    static final int MAKS_BUKU = 6, MAKS_USER = 6, MAKS_RAK = 6;

    static Font ft(int gaya, int ukuran) { return new Font("Segoe UI", gaya, ukuran); }

    // ======================= MODEL DATA =======================
    static class Buku {
        String judul, deskripsi, penulis, kategori, penerbit, format, status;
        int tahun, halaman;
        double rating;
        Buku(String judul, String deskripsi, String penulis, String kategori, String penerbit,
             int tahun, int halaman, String format, String status, double rating) {
            this.judul = judul; this.deskripsi = deskripsi; this.penulis = penulis; this.kategori = kategori;
            this.penerbit = penerbit; this.tahun = tahun; this.halaman = halaman; this.format = format;
            this.status = status; this.rating = rating;
        }
        boolean tersedia() { return "Tersedia".equals(status); }
        boolean cocok(String q) {
            q = q.toLowerCase().trim();
            return q.isEmpty() || judul.toLowerCase().contains(q) || penulis.toLowerCase().contains(q)
                    || kategori.toLowerCase().contains(q);
        }
    }

    static class Pengguna {
        String nama, email, role, status, sandi;
        int dibaca;
        Pengguna(String nama, String email, String role, String status, String sandi, int dibaca) {
            this.nama = nama; this.email = email; this.role = role; this.status = status;
            this.sandi = sandi; this.dibaca = dibaca;
        }
        boolean cocok(String q) {
            q = q.toLowerCase().trim();
            return q.isEmpty() || nama.toLowerCase().contains(q) || email.toLowerCase().contains(q);
        }
    }

    final List<Buku> buku = new ArrayList<>();
    final List<Pengguna> users = new ArrayList<>();
    final Pengguna admin = new Pengguna("Admin", "admin@gmail.com", "Admin", "Aktif", "admin123", 0);
    final Map<String, Integer> rak = new LinkedHashMap<>();
    final List<String[]> aktivitas = new ArrayList<>();
    int jamBaca = 4;
    Pengguna masuk = null;
    String menuAktif = "Dashboard";
    final JPanel holder = new JPanel(new BorderLayout());

    public PustakaDigitalApp() {
        super("PustakaDigital");
        isiData();
        setContentPane(holder);
        setSize(1200, 700);
        setMinimumSize(new Dimension(1200, 700));
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        bukaBeranda();
    }

    void isiData() {
        buku.add(new Buku("Atomic Habits", "Perubahan kecil yang dilakukan secara konsisten membawa hasil luar biasa.",
                "James Clear", "Pengembangan Diri", "Avery", 2018, 320, "PDF", "Tersedia", 4.8));
        buku.add(new Buku("Clean Code", "Panduan menulis kode yang bersih, rapi, dan mudah dipelihara.",
                "Robert C. Martin", "Teknologi", "Prentice Hall", 2008, 464, "PDF", "Tersedia", 4.7));
        buku.add(new Buku("Bumi Manusia", "Kisah Minke, pribumi terpelajar pada masa kolonial Hindia Belanda.",
                "Pramoedya Ananta Toer", "Novel", "Hasta Mitra", 1980, 535, "PDF", "Belum Tersedia", 4.9));
        buku.add(new Buku("Sejarah Dunia", "Rangkuman peristiwa besar dan peradaban dunia dari masa ke masa.",
                "Edisi Pembelajaran", "Sejarah", "Pustaka Ilmu", 2015, 410, "PDF", "Tersedia", 4.5));
        users.add(new Pengguna("Najwa Khairunnisa", "najwa@gmail.com", "User", "Aktif", "user123", 3));
        users.add(new Pengguna("Ji Chang-wook", "jichang@gmail.com", "User", "Aktif", "user123", 2));
        rak.put("Atomic Habits", 72);
        rak.put("Clean Code", 45);
        rak.put("Bumi Manusia", 100);
        aktivitas.add(new String[]{"Menambahkan buku", "Atomic Habits \u00B7 2 jam lalu"});
        aktivitas.add(new String[]{"Menyelesaikan bacaan", "Bumi Manusia \u00B7 Kemarin"});
        aktivitas.add(new String[]{"Membuka katalog", "Hari ini"});
    }

    Buku cariBuku(String judul) {
        for (Buku b : buku) if (b.judul.equals(judul)) return b;
        return null;
    }

    void ganti(JComponent c) {
        holder.removeAll();
        holder.add(c, BorderLayout.CENTER);
        holder.revalidate();
        holder.repaint();
    }

    void info(String teks) {
        JOptionPane.showMessageDialog(this, teks, "PustakaDigital", JOptionPane.INFORMATION_MESSAGE);
    }

    // ======================= KOMPONEN KUSTOM =======================
    static class Bulat extends JPanel {
        Color warna, garis;
        int arc;
        Bulat(Color warna, Color garis, int arc) {
            this.warna = warna; this.garis = garis; this.arc = arc;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(warna);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (garis != null) {
                g.setColor(garis);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static class Chip extends Bulat {
        final JLabel l = new JLabel();
        Chip(String teks, Color bg, Color fg) {
            super(bg, null, 999);
            setLayout(new BorderLayout());
            l.setText(teks);
            l.setFont(ft(Font.BOLD, 11));
            l.setForeground(fg);
            l.setBorder(new EmptyBorder(3, 10, 3, 10));
            add(l);
        }
        void set(String t) { l.setText(t); }
    }

    static class Tombol extends JButton {
        Color bg, garis;
        boolean hover;
        int rad = 12;
        Tombol(String teks, Color bg, Color fg, Color garis) {
            super(teks);
            this.bg = bg; this.garis = garis;
            setForeground(fg);
            setFont(ft(Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(0, 14, 0, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(Math.max(80, getPreferredSize().width), 38));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        Tombol kecil() { setFont(ft(Font.BOLD, 12)); setPreferredSize(new Dimension(80, 30)); rad = 10; return this; }
        Tombol tinggi(int h) { setPreferredSize(new Dimension(getPreferredSize().width, h)); return this; }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = bg;
            if (!isEnabled()) c = new Color(229, 231, 235);
            else if (hover) c = bg.equals(Color.WHITE) ? new Color(245, 243, 255) : bg.darker();
            g.setColor(c);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
            if (garis != null) {
                g.setColor(garis);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static Tombol primer(String t) { return new Tombol(t, PRIMER, Color.WHITE, null); }
    static Tombol hijau(String t) { return new Tombol(t, HIJAU_BTN, Color.WHITE, null); }
    static Tombol bahaya(String t) { return new Tombol(t, MERAH, Color.WHITE, null); }
    static Tombol garisTipis(String t) { return new Tombol(t, Color.WHITE, GELAP, GARIS_INPUT); }

    static void hint(Graphics g, javax.swing.text.JTextComponent c, String teks) {
        if (teks != null && c.getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(150, 150, 165));
            g2.setFont(c.getFont());
            Insets in = c.getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(teks, in.left, (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class Isian extends JTextField {
        String petunjuk;
        Isian(String petunjuk) {
            this.petunjuk = petunjuk;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(ft(Font.PLAIN, 14));
        }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); hint(g, this, petunjuk); }
    }

    static class IsianSandi extends JPasswordField {
        String petunjuk;
        IsianSandi(String petunjuk) {
            this.petunjuk = petunjuk;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(ft(Font.PLAIN, 14));
        }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); hint(g, this, petunjuk); }
    }

    static Bulat kolom(JComponent isi, int tinggi) {
        Bulat b = new Bulat(Color.WHITE, GARIS_INPUT, 14);
        b.setLayout(new BorderLayout());
        b.add(isi, BorderLayout.CENTER);
        b.setPreferredSize(new Dimension(100, tinggi));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, tinggi));
        return b;
    }

    static class Bar extends JComponent {
        int nilai; Color warna;
        Bar(int nilai, Color warna) {
            this.nilai = nilai; this.warna = warna;
            setPreferredSize(new Dimension(100, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight(), w = getWidth();
            g.setColor(new Color(229, 231, 235));
            g.fillRoundRect(0, 0, w, h, h, h);
            g.setColor(warna);
            g.fillRoundRect(0, 0, Math.max(h, w * nilai / 100), h, h, h);
            g.dispose();
        }
    }

    /** Ikon vektor sederhana, digambar dalam kotak 15x15 dengan warna aktif. */
    static void gambarIkon(Graphics2D g, String n) {
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (n) {
            case "Dashboard":
                g.drawRoundRect(0, 0, 6, 6, 2, 2); g.drawRoundRect(9, 0, 6, 6, 2, 2);
                g.drawRoundRect(0, 9, 6, 6, 2, 2); g.drawRoundRect(9, 9, 6, 6, 2, 2);
                break;
            case "Katalog":
                g.drawRoundRect(0, 1, 15, 13, 3, 3); g.drawLine(7, 1, 7, 14);
                break;
            case "Kelola Buku":
                g.drawRoundRect(0, 5, 15, 9, 3, 3); g.drawLine(2, 2, 13, 2);
                break;
            case "Kelola Pengguna":
                g.drawOval(4, 0, 7, 7); g.drawArc(1, 9, 13, 12, 0, 180);
                break;
            case "Beranda":
                g.drawPolyline(new int[]{0, 7, 15}, new int[]{7, 0, 7}, 3); g.drawRect(2, 7, 11, 7);
                break;
            case "Keluar":
                g.drawRect(0, 1, 7, 13); g.drawLine(6, 7, 15, 7); g.drawLine(12, 4, 15, 7); g.drawLine(12, 10, 15, 7);
                break;
            case "Rak Saya":
                g.drawPolyline(new int[]{2, 2, 13, 13, 7, 2}, new int[]{14, 0, 0, 14, 10, 14}, 6);
                break;
            case "Selesai":
                g.drawOval(0, 0, 15, 15); g.drawPolyline(new int[]{4, 7, 11}, new int[]{8, 11, 5}, 3);
                break;
            case "Jam":
                g.drawOval(0, 0, 15, 15); g.drawLine(7, 7, 7, 3); g.drawLine(7, 7, 11, 9);
                break;
            case "Tag":
                g.drawPolygon(new int[]{1, 9, 15, 7}, new int[]{1, 1, 7, 15}, 4); g.drawOval(4, 4, 2, 2);
                break;
            case "kode":
                g.drawPolyline(new int[]{5, 0, 5}, new int[]{3, 7, 12}, 3);
                g.drawPolyline(new int[]{10, 15, 10}, new int[]{3, 7, 12}, 3);
                g.drawLine(9, 2, 6, 13);
                break;
            default:
                break;
        }
    }

    static class IkonBox extends JComponent {
        String nama; Color bg, fg; int ukuran; double skala;
        IkonBox(String nama, Color bg, Color fg, int ukuran, double skala) {
            this.nama = nama; this.bg = bg; this.fg = fg; this.ukuran = ukuran; this.skala = skala;
            setPreferredSize(new Dimension(ukuran, ukuran));
            setMinimumSize(new Dimension(ukuran, ukuran));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (bg != null) { g.setColor(bg); g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ukuran / 3, ukuran / 3); }
            g.setColor(fg);
            g.translate(getWidth() / 2.0, getHeight() / 2.0);
            g.scale(skala, skala);
            g.translate(-7.5, -7.5);
            gambarIkon(g, nama);
            g.dispose();
        }
    }

    static class Avatar extends JComponent {
        Avatar() { setPreferredSize(new Dimension(34, 34)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(UNGU_MUDA);
            g.fillOval(0, 0, 33, 33);
            g.setColor(PRIMER);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(12, 8, 8, 8);
            g.drawArc(9, 19, 14, 12, 0, 180);
            g.dispose();
        }
    }

    static class LatarGradien extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setPaint(new GradientPaint(0, 0, new Color(240, 235, 255), getWidth(), getHeight(), new Color(255, 245, 245)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    // ---- warna per kategori ----
    static Color[] tema(String kat) {
        switch (kat) {
            case "Pengembangan Diri": return new Color[]{UNGU_MUDA, PRIMER};
            case "Teknologi": return new Color[]{new Color(224, 242, 254), new Color(14, 165, 233)};
            case "Novel": return new Color[]{new Color(254, 226, 226), new Color(239, 68, 68)};
            case "Sejarah": return new Color[]{new Color(254, 243, 199), new Color(217, 119, 6)};
            default: return new Color[]{new Color(243, 244, 246), PRIMER};
        }
    }

    static String ikonKategori(String kat) { return kat.equals("Teknologi") ? "kode" : "Katalog"; }

    static Chip chipKategori(String kat) {
        Color[] t = tema(kat);
        return new Chip(kat, t[0], t[1].darker());
    }

    static Chip chipStatus(String st) {
        return "Tersedia".equals(st) || "Aktif".equals(st)
                ? new Chip(st, HIJAU_BG, HIJAU_TX) : new Chip(st, MERAH_BG, MERAH_TX);
    }

    // ---- sampul buku (digambar) ----
    static Color[] warnaSampul(String kat) {
        switch (kat) {
            case "Pengembangan Diri": return new Color[]{new Color(252, 246, 225), new Color(245, 228, 160), new Color(176, 120, 0)};
            case "Teknologi": return new Color[]{new Color(10, 20, 60), new Color(70, 25, 120), Color.WHITE};
            case "Novel": return new Color[]{new Color(228, 208, 165), new Color(185, 155, 105), new Color(80, 50, 20)};
            case "Sejarah": return new Color[]{new Color(75, 45, 32), new Color(40, 25, 20), new Color(235, 210, 150)};
            default: return new Color[]{PRIMER, new Color(91, 33, 182), Color.WHITE};
        }
    }

    static List<String> bungkus(String s, FontMetrics fm, int lebar) {
        List<String> hasil = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : s.split(" ")) {
            String t = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(t) > lebar && cur.length() > 0) {
                hasil.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(t);
            }
        }
        if (cur.length() > 0) hasil.add(cur.toString());
        return hasil;
    }

    static class Sampul extends JPanel {
        Buku b;
        Sampul(Buku b) { this.b = b; setOpaque(false); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color[] c = warnaSampul(b.kategori);
            g.setPaint(new GradientPaint(0, 0, c[0], 0, h, c[1]));
            g.fill(new RoundRectangle2D.Float(0, 0, w, h + 24, 20, 20));
            g.setColor(new Color(255, 255, 255, 35));
            g.fillOval(-w / 4, h / 2, w, w);

            boolean tek = b.kategori.equals("Teknologi");
            int ukuran = Math.max(16, Math.min(34, w / 6));
            g.setFont(tek ? ft(Font.BOLD, ukuran) : new Font("Serif", Font.BOLD, ukuran));
            FontMetrics fm = g.getFontMetrics();
            String judul = tek ? b.judul : b.judul.toUpperCase();
            List<String> baris = bungkus(judul, fm, w - 36);
            int total = baris.size() * fm.getHeight();
            int y = (int) (h * 0.40) - total / 2 + fm.getAscent();
            g.setColor(c[2]);
            for (String s : baris) {
                g.drawString(s, (w - fm.stringWidth(s)) / 2, y);
                y += fm.getHeight();
            }
            g.fillRect(w / 2 - 22, y, 44, 3);
            g.setFont(ft(Font.PLAIN, 11));
            fm = g.getFontMetrics();
            String pen = b.penulis.toUpperCase();
            g.drawString(pen, Math.max(8, (w - fm.stringWidth(pen)) / 2), h - 18);
            g.dispose();
        }
    }

    // ======================= TABEL =======================
    static class RenHeader extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean fo, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setFont(ft(Font.BOLD, 12));
            setForeground(ABU);
            setBackground(Color.WHITE);
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GARIS), new EmptyBorder(0, 8, 0, 0)));
            return this;
        }
    }

    static void gayaTabel(JTable t) {
        t.setRowHeight(56);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(240, 238, 248));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setRowSelectionAllowed(false);
        t.setFocusable(false);
        t.setBackground(Color.WHITE);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new RenHeader());
    }

    static JPanel bungkusSel(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 8, 0, 0);
        p.add(c, g);
        return p;
    }

    static JLabel teksSel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(ft(Font.PLAIN, 13));
        l.setForeground(TEKS);
        l.setOpaque(true);
        l.setBackground(Color.WHITE);
        l.setBorder(new EmptyBorder(0, 8, 0, 4));
        return l;
    }

    static JPanel selAksi() {
        JPanel kotak = new JPanel(new GridLayout(1, 2, 6, 0));
        kotak.setOpaque(false);
        kotak.setPreferredSize(new Dimension(84, 28));
        Bulat ubah = new Bulat(new Color(243, 244, 246), null, 10);
        ubah.setLayout(new GridBagLayout());
        JLabel a = new JLabel("Ubah");
        a.setFont(ft(Font.BOLD, 11)); a.setForeground(TEKS);
        ubah.add(a);
        Bulat hapus = new Bulat(new Color(254, 226, 226), null, 10);
        hapus.setLayout(new GridBagLayout());
        JLabel b = new JLabel("Hapus");
        b.setFont(ft(Font.BOLD, 11)); b.setForeground(MERAH);
        hapus.add(b);
        kotak.add(ubah);
        kotak.add(hapus);
        return bungkusSel(kotak);
    }

    static class RenBuku implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean fo, int r, int c) {
            Buku b = (Buku) v;
            switch (c) {
                case 0: {
                    JPanel y = new JPanel();
                    y.setOpaque(false);
                    y.setLayout(new BoxLayout(y, BoxLayout.Y_AXIS));
                    JLabel j = new JLabel(b.judul);
                    j.setFont(ft(Font.BOLD, 14)); j.setForeground(GELAP);
                    String d = b.deskripsi.length() > 30 ? b.deskripsi.substring(0, 30) + "..." : b.deskripsi;
                    JLabel k = new JLabel(d);
                    k.setFont(ft(Font.PLAIN, 11)); k.setForeground(ABU);
                    y.add(j); y.add(k);
                    return bungkusSel(y);
                }
                case 1: return teksSel(b.penulis);
                case 2: return teksSel(b.kategori);
                case 3: return teksSel(b.penerbit);
                case 4: return teksSel(String.valueOf(b.tahun));
                case 5: return teksSel(String.valueOf(b.halaman));
                case 6: return teksSel(b.format);
                case 7: return bungkusSel(chipStatus(b.status));
                default: return selAksi();
            }
        }
    }

    static class RenPengguna implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean fo, int r, int c) {
            Pengguna p = (Pengguna) v;
            switch (c) {
                case 0: {
                    JPanel y = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 11));
                    y.setBackground(Color.WHITE);
                    JLabel n = new JLabel(p.nama);
                    n.setFont(ft(Font.BOLD, 14)); n.setForeground(GELAP);
                    y.add(new Avatar());
                    y.add(n);
                    return y;
                }
                case 1: return teksSel(p.email);
                case 2: return bungkusSel(new Chip(p.role, UNGU_MUDA, PRIMER));
                case 3: return bungkusSel(chipStatus(p.status));
                case 4: return teksSel(p.dibaca + " Buku");
                default: return selAksi();
            }
        }
    }

    static DocumentListener dl(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    static final int AKSI_LEBAR = 100;

    // ======================= NAVBAR PUBLIK =======================
    JComponent logoMerek() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        Bulat kotak = new Bulat(PRIMER, null, 14);
        kotak.setLayout(new GridBagLayout());
        kotak.setPreferredSize(new Dimension(40, 40));
        JLabel pl = new JLabel("P");
        pl.setFont(ft(Font.BOLD, 20)); pl.setForeground(Color.WHITE);
        kotak.add(pl);
        JLabel nama = new JLabel("PustakaDigital");
        nama.setFont(ft(Font.BOLD, 20)); nama.setForeground(GELAP);
        p.add(kotak); p.add(nama);
        return p;
    }

    JComponent navbar(String aktif) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GARIS), new EmptyBorder(14, 60, 14, 60)));
        p.setPreferredSize(new Dimension(0, 72));
        p.add(logoMerek(), BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        kanan.setOpaque(false);
        String[] menu = {"Beranda", "Katalog", "Dashboard"};
        for (String m : menu) {
            boolean on = m.equals(aktif);
            Tombol b = new Tombol(m, on ? UNGU_MUDA : Color.WHITE, on ? PRIMER : GELAP, null);
            b.setFont(ft(Font.BOLD, 14));
            b.addActionListener(e -> {
                if (m.equals("Beranda")) bukaBeranda();
                else if (m.equals("Katalog")) bukaKatalogPublik("");
                else if (masuk == null) { info("Silakan masuk terlebih dahulu."); bukaLogin(); }
                else bukaShell("Dashboard");
            });
            kanan.add(b);
        }
        Tombol btn = primer(masuk == null ? "Masuk" : "Keluar");
        btn.setPreferredSize(new Dimension(96, 42));
        btn.addActionListener(e -> {
            if (masuk == null) bukaLogin();
            else keluarAkun();
        });
        kanan.add(btn);
        p.add(kanan, BorderLayout.EAST);
        return p;
    }

    JComponent publik(String aktif, JComponent isi) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(navbar(aktif), BorderLayout.NORTH);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }

    void keluarAkun() {
        if (JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?", "Keluar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            masuk = null;
            bukaBeranda();
        }
    }

    // ======================= BERANDA =======================
    void bukaBeranda() { ganti(publik("Beranda", isiBeranda())); }

    static void rata(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }

    JComponent isiBeranda() {
        JPanel p = new JPanel(new GridLayout(1, 2, 40, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(40, 60, 20, 60));

        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
        Chip badge = new Chip("\u2726 Gratis untuk pelajar Indonesia", UNGU_MUDA, PRIMER);
        badge.l.setFont(ft(Font.BOLD, 13));
        JPanel barisBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barisBadge.setOpaque(false);
        barisBadge.add(badge);
        rata(barisBadge);
        kiri.add(barisBadge);
        kiri.add(Box.createVerticalStrut(18));

        JPanel b1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        b1.setOpaque(false);
        JLabel a1 = new JLabel("Baca Lebih ");
        a1.setFont(ft(Font.BOLD, 44)); a1.setForeground(GELAP);
        JLabel a2 = new JLabel("Pintar,");
        a2.setFont(ft(Font.BOLD, 44)); a2.setForeground(PRIMER);
        b1.add(a1); b1.add(a2);
        rata(b1);
        kiri.add(b1);
        JLabel a3 = new JLabel("Bukan Lebih Lama.");
        a3.setFont(ft(Font.BOLD, 44)); a3.setForeground(GELAP);
        rata(a3);
        kiri.add(a3);
        kiri.add(Box.createVerticalStrut(16));

        JLabel desk = new JLabel("<html><body style='width:440px'>Akses seluruh koleksi buku digital dalam satu perpustakaan yang cepat dan mudah digunakan.</body></html>");
        desk.setFont(ft(Font.PLAIN, 16)); desk.setForeground(ABU);
        rata(desk);
        kiri.add(desk);
        kiri.add(Box.createVerticalStrut(22));

        Isian cari = new Isian("Cari buku atau penulis...");
        Bulat kotakCari = new Bulat(Color.WHITE, GARIS_INPUT, 18);
        kotakCari.setLayout(new BorderLayout(8, 0));
        kotakCari.setBorder(new EmptyBorder(6, 6, 6, 8));
        kotakCari.setPreferredSize(new Dimension(100, 58));
        kotakCari.add(cari, BorderLayout.CENTER);
        Tombol bCari = primer("Cari");
        bCari.setPreferredSize(new Dimension(80, 42));
        kotakCari.add(bCari, BorderLayout.EAST);
        Runnable cariAksi = () -> bukaKatalogPublik(cari.getText());
        bCari.addActionListener(e -> cariAksi.run());
        cari.addActionListener(e -> cariAksi.run());
        rata(kotakCari);
        kiri.add(kotakCari);
        kiri.add(Box.createVerticalStrut(14));

        JPanel pop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pop.setOpaque(false);
        JLabel pl = new JLabel("Populer:");
        pl.setFont(ft(Font.PLAIN, 15)); pl.setForeground(ABU);
        pop.add(pl);
        String[] populer = {"Teknologi", "Novel", "Sejarah"};
        for (String s : populer) {
            Tombol t = new Tombol(s, Color.WHITE, PRIMER, null);
            t.setBackground(new Color(250, 249, 255, 0));
            t.setFont(ft(Font.BOLD, 15));
            t.setPreferredSize(new Dimension(t.getPreferredSize().width, 28));
            t.addActionListener(e -> bukaKatalogPublik(s));
            pop.add(t);
        }
        pop.setBackground(Color.WHITE);
        rata(pop);
        kiri.add(pop);

        // kartu rak di kanan
        Bulat kartu = new Bulat(Color.WHITE, GARIS, 30);
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBorder(new EmptyBorder(20, 24, 24, 24));
        JLabel hd = new JLabel("PustakaDigital / Rak Saya");
        hd.setFont(ft(Font.PLAIN, 15)); hd.setForeground(ABU);
        hd.setAlignmentX(Component.RIGHT_ALIGNMENT);
        kartu.add(hd);
        kartu.add(Box.createVerticalStrut(18));
        Color[] bgs = {UNGU_MUDA, new Color(224, 242, 254), new Color(248, 247, 252)};
        for (int i = 0; i < 3; i++) {
            Buku b = buku.get(i);
            Bulat baris = new Bulat(bgs[i], null, 22);
            baris.setLayout(new BoxLayout(baris, BoxLayout.Y_AXIS));
            baris.setBorder(new EmptyBorder(20, 20, 16, 20));
            JLabel j = new JLabel(b.judul);
            j.setFont(ft(Font.BOLD, 18)); j.setForeground(GELAP);
            JLabel pn = new JLabel(b.penulis);
            pn.setFont(ft(Font.PLAIN, 14)); pn.setForeground(ABU);
            baris.add(j); baris.add(Box.createVerticalStrut(4)); baris.add(pn);
            baris.setPreferredSize(new Dimension(100, 92));
            baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
            baris.setAlignmentX(Component.LEFT_ALIGNMENT);
            kartu.add(baris);
            kartu.add(Box.createVerticalStrut(16));
        }
        JPanel kanan = new JPanel(new BorderLayout());
        kanan.setOpaque(false);
        kanan.setBorder(new EmptyBorder(0, 0, 0, 0));
        kanan.add(kartu, BorderLayout.NORTH);

        p.add(kiri);
        p.add(kanan);
        return p;
    }

    // ======================= KATALOG =======================
    void bukaKatalogPublik(String cari) {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(LATAR);
        w.setBorder(new EmptyBorder(26, 60, 10, 60));
        w.add(isiKatalog(cari), BorderLayout.CENTER);
        ganti(publik("Katalog", w));
    }

    JComponent isiKatalog(String cariAwal) {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        JLabel jd = new JLabel("Koleksi Buku");
        jd.setFont(ft(Font.BOLD, 26)); jd.setForeground(GELAP);
        Isian cari = new Isian("Cari judul, penulis, kategori...");
        cari.setText(cariAwal == null ? "" : cariAwal);
        Chip badge = new Chip("0 Buku", UNGU_MUDA, PRIMER);
        JPanel kananAtas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        kananAtas.setOpaque(false);
        Bulat kc = kolom(cari, 40);
        kc.setPreferredSize(new Dimension(320, 40));
        kananAtas.add(kc);
        kananAtas.add(badge);
        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(jd, BorderLayout.WEST);
        atas.add(kananAtas, BorderLayout.EAST);

        JPanel grid = new JPanel();
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 440));
        Runnable isi = () -> {
            grid.removeAll();
            List<Buku> hasil = new ArrayList<>();
            for (Buku b : buku) if (b.cocok(cari.getText())) hasil.add(b);
            int kol = Math.max(4, hasil.size());
            grid.setLayout(new GridLayout(1, kol, 16, 0));
            for (Buku b : hasil) grid.add(kartuBuku(b));
            for (int i = hasil.size(); i < kol; i++) {
                JPanel k = new JPanel();
                k.setOpaque(false);
                grid.add(k);
            }
            badge.set(hasil.size() + " Buku");
            grid.revalidate();
            grid.repaint();
        };
        cari.getDocument().addDocumentListener(dl(isi));
        isi.run();

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        tengah.add(grid, BorderLayout.CENTER);
        p.add(atas, BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    JComponent kartuBuku(Buku b) {
        Bulat k = new Bulat(b.tersedia() ? Color.WHITE : new Color(238, 236, 244), GARIS, 20);
        k.setLayout(new BorderLayout());
        k.setBorder(new EmptyBorder(1, 1, 1, 1));
        k.add(new Sampul(b), BorderLayout.CENTER);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(10, 14, 14, 14));
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(chipKategori(b.kategori));
        chips.add(chipStatus(b.status));
        rata(chips);
        info.add(chips);
        info.add(Box.createVerticalStrut(8));
        JLabel j = new JLabel(b.judul);
        j.setFont(ft(Font.BOLD, 19)); j.setForeground(GELAP);
        rata(j);
        info.add(j);
        JLabel pn = new JLabel(b.penulis);
        pn.setFont(ft(Font.PLAIN, 13)); pn.setForeground(ABU);
        rata(pn);
        info.add(pn);
        info.add(Box.createVerticalStrut(4));
        int penuh = (int) Math.round(b.rating);
        StringBuilder bintang = new StringBuilder();
        for (int i = 0; i < 5; i++) bintang.append(i < penuh ? "\u2605" : "\u2606");
        JLabel rt = new JLabel(bintang + "  " + b.rating);
        rt.setFont(ft(Font.PLAIN, 13)); rt.setForeground(new Color(180, 83, 9));
        rata(rt);
        info.add(rt);
        info.add(Box.createVerticalStrut(10));
        Tombol det = b.tersedia() ? primer("Lihat Detail") : garisTipis("Lihat Detail");
        det.setPreferredSize(new Dimension(100, 38));
        det.addActionListener(e -> dialogDetail(b));
        rata(det);
        info.add(det);
        k.add(info, BorderLayout.SOUTH);
        return k;
    }

    // ======================= DIALOG =======================
    JDialog dlg(String judul, int w, int h) {
        JDialog d = new JDialog(this, judul, true);
        d.setSize(w, h);
        d.setResizable(false);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Color.WHITE);
        return d;
    }

    static JTextField isianDialog(String teks) {
        JTextField t = new JTextField(teks);
        t.setFont(ft(Font.PLAIN, 13));
        t.setBorder(new CompoundBorder(new LineBorder(GARIS_INPUT, 1, true), new EmptyBorder(6, 8, 6, 8)));
        return t;
    }

    static JPanel berlabel(String label, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(ft(Font.BOLD, 12)); l.setForeground(TEKS);
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        return p;
    }

    static JPanel dua(JComponent a, JComponent b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setOpaque(false);
        p.add(a); p.add(b);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        return p;
    }

    void dialogBuku(Buku edit) {
        boolean baru = edit == null;
        if (baru && buku.size() >= MAKS_BUKU) {
            info("Maksimal " + MAKS_BUKU + " buku agar tampilan tetap muat tanpa scroll.");
            return;
        }
        JDialog d = dlg(baru ? "Tambah Buku" : "Ubah Buku", 460, 600);
        JTextField tJ = isianDialog(baru ? "" : edit.judul), tP = isianDialog(baru ? "" : edit.penulis);
        JTextField tPb = isianDialog(baru ? "" : edit.penerbit);
        JTextField tTh = isianDialog(baru ? "" : String.valueOf(edit.tahun));
        JTextField tHal = isianDialog(baru ? "" : String.valueOf(edit.halaman));
        JTextField tDesk = isianDialog(baru ? "" : edit.deskripsi);
        JComboBox<String> cK = new JComboBox<>(new String[]{"Pengembangan Diri", "Teknologi", "Novel", "Sejarah", "Lainnya"});
        JComboBox<String> cF = new JComboBox<>(new String[]{"PDF", "EPUB"});
        JComboBox<String> cS = new JComboBox<>(new String[]{"Tersedia", "Belum Tersedia"});
        if (!baru) { cK.setSelectedItem(edit.kategori); cF.setSelectedItem(edit.format); cS.setSelectedItem(edit.status); }

        JPanel f = new JPanel();
        f.setOpaque(false);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBorder(new EmptyBorder(16, 22, 8, 22));
        JLabel jd = new JLabel(baru ? "Tambah Buku" : "Ubah Buku");
        jd.setFont(ft(Font.BOLD, 22)); jd.setForeground(GELAP);
        jd.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.add(jd);
        f.add(Box.createVerticalStrut(10));
        f.add(berlabel("Judul", tJ)); f.add(Box.createVerticalStrut(6));
        f.add(berlabel("Penulis", tP)); f.add(Box.createVerticalStrut(6));
        f.add(berlabel("Kategori", cK)); f.add(Box.createVerticalStrut(6));
        f.add(berlabel("Penerbit", tPb)); f.add(Box.createVerticalStrut(6));
        f.add(dua(berlabel("Tahun", tTh), berlabel("Halaman", tHal))); f.add(Box.createVerticalStrut(6));
        f.add(dua(berlabel("Format", cF), berlabel("Status", cS))); f.add(Box.createVerticalStrut(6));
        f.add(berlabel("Deskripsi singkat", tDesk));

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        tb.setOpaque(false);
        tb.setBorder(new EmptyBorder(0, 0, 8, 14));
        Tombol batal = garisTipis("Batal"), simpan = primer("Simpan");
        batal.addActionListener(e -> d.dispose());
        simpan.addActionListener(e -> {
            if (tJ.getText().isBlank() || tP.getText().isBlank() || tPb.getText().isBlank()) {
                JOptionPane.showMessageDialog(d, "Judul, penulis, dan penerbit wajib diisi.");
                return;
            }
            int th, hl;
            try {
                th = Integer.parseInt(tTh.getText().trim());
                hl = Integer.parseInt(tHal.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Tahun dan halaman harus berupa angka.");
                return;
            }
            String desk = tDesk.getText().isBlank() ? "Belum ada deskripsi." : tDesk.getText().trim();
            if (baru) {
                buku.add(new Buku(tJ.getText().trim(), desk, tP.getText().trim(), (String) cK.getSelectedItem(),
                        tPb.getText().trim(), th, hl, (String) cF.getSelectedItem(), (String) cS.getSelectedItem(), 4.5));
            } else {
                String lama = edit.judul;
                edit.judul = tJ.getText().trim(); edit.penulis = tP.getText().trim();
                edit.kategori = (String) cK.getSelectedItem(); edit.penerbit = tPb.getText().trim();
                edit.tahun = th; edit.halaman = hl; edit.format = (String) cF.getSelectedItem();
                edit.status = (String) cS.getSelectedItem(); edit.deskripsi = desk;
                if (rak.containsKey(lama) && !lama.equals(edit.judul)) {
                    Map<String, Integer> salin = new LinkedHashMap<>();
                    for (Map.Entry<String, Integer> en : rak.entrySet())
                        salin.put(en.getKey().equals(lama) ? edit.judul : en.getKey(), en.getValue());
                    rak.clear();
                    rak.putAll(salin);
                }
            }
            d.dispose();
            bukaShell(menuAktif);
        });
        tb.add(batal); tb.add(simpan);
        d.add(f, BorderLayout.CENTER);
        d.add(tb, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void hapusBuku(Buku b) {
        if (JOptionPane.showConfirmDialog(this, "Hapus buku \"" + b.judul + "\"?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            buku.remove(b);
            rak.remove(b.judul);
            bukaShell(menuAktif);
        }
    }

    void dialogPengguna(Pengguna edit, boolean daftarMandiri) {
        boolean baru = edit == null;
        if (baru && users.size() >= MAKS_USER) {
            info("Maksimal " + MAKS_USER + " pengguna agar tampilan tetap muat tanpa scroll.");
            return;
        }
        String judul = daftarMandiri ? "Daftar Akun" : (baru ? "Tambah Pengguna" : "Ubah Pengguna");
        JDialog d = dlg(judul, 420, daftarMandiri ? 400 : 500);
        JTextField tN = isianDialog(baru ? "" : edit.nama), tE = isianDialog(baru ? "" : edit.email);
        JPasswordField tS = new JPasswordField();
        tS.setBorder(new CompoundBorder(new LineBorder(GARIS_INPUT, 1, true), new EmptyBorder(6, 8, 6, 8)));
        JComboBox<String> cR = new JComboBox<>(new String[]{"User", "Admin"});
        JComboBox<String> cS = new JComboBox<>(new String[]{"Aktif", "Nonaktif"});
        if (!baru) { cR.setSelectedItem(edit.role); cS.setSelectedItem(edit.status); }

        JPanel f = new JPanel();
        f.setOpaque(false);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBorder(new EmptyBorder(16, 22, 8, 22));
        JLabel jd = new JLabel(judul);
        jd.setFont(ft(Font.BOLD, 22)); jd.setForeground(GELAP);
        jd.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.add(jd);
        f.add(Box.createVerticalStrut(10));
        f.add(berlabel("Nama lengkap", tN)); f.add(Box.createVerticalStrut(6));
        f.add(berlabel("Email", tE)); f.add(Box.createVerticalStrut(6));
        f.add(berlabel(baru ? "Password" : "Password (kosongkan jika tidak diubah)", tS)); f.add(Box.createVerticalStrut(6));
        if (!daftarMandiri) {
            f.add(dua(berlabel("Role", cR), berlabel("Status", cS)));
        }

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        tb.setOpaque(false);
        tb.setBorder(new EmptyBorder(0, 0, 8, 14));
        Tombol batal = garisTipis("Batal"), simpan = primer(daftarMandiri ? "Daftar" : "Simpan");
        batal.addActionListener(e -> d.dispose());
        simpan.addActionListener(e -> {
            String em = tE.getText().trim();
            String sandi = new String(tS.getPassword());
            if (tN.getText().isBlank() || !em.contains("@")) {
                JOptionPane.showMessageDialog(d, "Nama wajib diisi dan email harus valid.");
                return;
            }
            if (baru && sandi.isBlank()) {
                JOptionPane.showMessageDialog(d, "Password wajib diisi.");
                return;
            }
            for (Pengguna u : users) {
                if (u != edit && u.email.equalsIgnoreCase(em)) {
                    JOptionPane.showMessageDialog(d, "Email sudah terdaftar.");
                    return;
                }
            }
            if (baru) {
                users.add(new Pengguna(tN.getText().trim(), em, daftarMandiri ? "User" : (String) cR.getSelectedItem(),
                        daftarMandiri ? "Aktif" : (String) cS.getSelectedItem(), sandi, 0));
            } else {
                edit.nama = tN.getText().trim(); edit.email = em;
                edit.role = (String) cR.getSelectedItem(); edit.status = (String) cS.getSelectedItem();
                if (!sandi.isBlank()) edit.sandi = sandi;
            }
            d.dispose();
            if (daftarMandiri) info("Pendaftaran berhasil. Silakan masuk.");
            else bukaShell(menuAktif);
        });
        tb.add(batal); tb.add(simpan);
        d.add(f, BorderLayout.CENTER);
        d.add(tb, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void hapusPengguna(Pengguna u) {
        if (u == masuk) { info("Akun yang sedang dipakai tidak bisa dihapus."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus pengguna \"" + u.nama + "\"?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            users.remove(u);
            bukaShell(menuAktif);
        }
    }

    void dialogDetail(Buku b) {
        JDialog d = dlg("Detail Buku", 680, 420);
        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(18, 18, 8, 18));
        Sampul s = new Sampul(b);
        s.setPreferredSize(new Dimension(190, 0));
        p.add(s, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(chipKategori(b.kategori));
        chips.add(chipStatus(b.status));
        rata(chips);
        info.add(chips);
        info.add(Box.createVerticalStrut(8));
        JLabel j = new JLabel(b.judul);
        j.setFont(ft(Font.BOLD, 24)); j.setForeground(GELAP);
        rata(j);
        info.add(j);
        JLabel pn = new JLabel("oleh " + b.penulis);
        pn.setFont(ft(Font.PLAIN, 14)); pn.setForeground(ABU);
        rata(pn);
        info.add(pn);
        info.add(Box.createVerticalStrut(12));
        JLabel ds = new JLabel("<html><body style='width:370px'>" + b.deskripsi + "</body></html>");
        ds.setFont(ft(Font.PLAIN, 14)); ds.setForeground(TEKS);
        rata(ds);
        info.add(ds);
        info.add(Box.createVerticalStrut(14));
        JPanel tabel = new JPanel(new GridLayout(0, 2, 8, 6));
        tabel.setOpaque(false);
        String[][] baris = {{"Penerbit", b.penerbit}, {"Tahun terbit", String.valueOf(b.tahun)},
                {"Jumlah halaman", String.valueOf(b.halaman)}, {"Format", b.format}, {"Rating", b.rating + " / 5"}};
        for (String[] r : baris) {
            JLabel a = new JLabel(r[0]);
            a.setForeground(ABU); a.setFont(ft(Font.PLAIN, 13));
            JLabel v = new JLabel(r[1]);
            v.setForeground(GELAP); v.setFont(ft(Font.BOLD, 13));
            tabel.add(a); tabel.add(v);
        }
        rata(tabel);
        info.add(tabel);
        p.add(info, BorderLayout.CENTER);

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        tb.setOpaque(false);
        tb.setBorder(new EmptyBorder(0, 0, 8, 10));
        if (masuk == null) {
            JLabel l = new JLabel("Masuk sebagai User untuk menyimpan buku ke rak.");
            l.setForeground(ABU);
            tb.add(l);
        } else if ("User".equals(masuk.role)) {
            Tombol tambah = hijau("Tambah ke Rak Saya");
            tambah.setPreferredSize(new Dimension(170, 38));
            if (rak.containsKey(b.judul)) { tambah.setText("Sudah di Rak Saya"); tambah.setEnabled(false); }
            else if (!b.tersedia()) { tambah.setText("Belum Tersedia"); tambah.setEnabled(false); }
            tambah.addActionListener(e -> {
                if (rak.size() >= MAKS_RAK) { JOptionPane.showMessageDialog(d, "Rak penuh. Maksimal " + MAKS_RAK + " buku."); return; }
                rak.put(b.judul, 0);
                aktivitas.add(0, new String[]{"Menambahkan buku", b.judul + " \u00B7 Baru saja"});
                d.dispose();
                info("\"" + b.judul + "\" ditambahkan ke Rak Saya.");
                if (menuAktif.equals("Katalog") && masuk != null) bukaShell("Katalog");
            });
            tb.add(tambah);
        }
        Tombol tutup = garisTipis("Tutup");
        tutup.addActionListener(e -> d.dispose());
        tb.add(tutup);
        d.add(p, BorderLayout.CENTER);
        d.add(tb, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ======================= LOGIN =======================
    void bukaLogin() { ganti(publik("Masuk", isiLogin())); }

    Pengguna cariAkun(String email, String sandi, String role) {
        if (role.equals("Admin") && email.equalsIgnoreCase(admin.email) && sandi.equals(admin.sandi)) return admin;
        for (Pengguna u : users) {
            if (u.email.equalsIgnoreCase(email) && u.sandi.equals(sandi) && u.role.equals(role) && u.status.equals("Aktif"))
                return u;
        }
        return null;
    }

    JComponent isiLogin() {
        JPanel latar = new LatarGradien();
        latar.setLayout(new GridBagLayout());

        JPanel kolomUtama = new JPanel();
        kolomUtama.setOpaque(false);
        kolomUtama.setLayout(new BoxLayout(kolomUtama, BoxLayout.Y_AXIS));
        JLabel jd = new JLabel("Selamat Datang");
        jd.setFont(ft(Font.BOLD, 38)); jd.setForeground(GELAP);
        jd.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sb = new JLabel("Masuk ke akun perpustakaanmu.");
        sb.setFont(ft(Font.PLAIN, 16)); sb.setForeground(ABU);
        sb.setAlignmentX(Component.CENTER_ALIGNMENT);
        kolomUtama.add(jd);
        kolomUtama.add(Box.createVerticalStrut(6));
        kolomUtama.add(sb);
        kolomUtama.add(Box.createVerticalStrut(22));

        Bulat card = new Bulat(Color.WHITE, GARIS, 24);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 38, 20, 38));
        card.setPreferredSize(new Dimension(520, 400));
        card.setMaximumSize(new Dimension(520, 400));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        Isian tEmail = new Isian("nama@email.com");
        IsianSandi tSandi = new IsianSandi("Masukkan password");
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"User", "Admin"});
        cbRole.setFont(ft(Font.PLAIN, 14));
        cbRole.setBackground(Color.WHITE);

        card.add(labelForm("Email"));
        card.add(Box.createVerticalStrut(4));
        card.add(kolomRata(kolom(tEmail, 42)));
        card.add(Box.createVerticalStrut(10));
        card.add(labelForm("Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(kolomRata(kolom(tSandi, 42)));
        card.add(Box.createVerticalStrut(10));
        card.add(labelForm("Masuk sebagai"));
        card.add(Box.createVerticalStrut(4));
        card.add(kolomRata(kolom(cbRole, 42)));
        card.add(Box.createVerticalStrut(10));

        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);
        JCheckBox ingat = new JCheckBox("Ingat saya");
        ingat.setOpaque(false); ingat.setForeground(ABU); ingat.setFont(ft(Font.PLAIN, 14));
        Tombol lupa = new Tombol("Lupa password?", Color.WHITE, PRIMER, null);
        lupa.setBackground(new Color(255, 255, 255, 0));
        lupa.setFont(ft(Font.PLAIN, 14));
        lupa.addActionListener(e -> info("Hubungi admin PustakaDigital untuk mengatur ulang password."));
        baris.add(ingat, BorderLayout.WEST);
        baris.add(lupa, BorderLayout.EAST);
        baris.setPreferredSize(new Dimension(100, 32));
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(baris);
        card.add(Box.createVerticalStrut(10));

        Tombol bMasuk = primer("Masuk");
        bMasuk.setPreferredSize(new Dimension(100, 46));
        bMasuk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        bMasuk.setAlignmentX(Component.LEFT_ALIGNMENT);
        bMasuk.setFont(ft(Font.BOLD, 15));
        Runnable proses = () -> {
            Pengguna u = cariAkun(tEmail.getText().trim(), new String(tSandi.getPassword()), (String) cbRole.getSelectedItem());
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Email, password, atau peran salah / akun tidak aktif.",
                        "Login Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            masuk = u;
            bukaShell("Dashboard");
        };
        bMasuk.addActionListener(e -> proses.run());
        tSandi.addActionListener(e -> proses.run());
        card.add(bMasuk);
        card.add(Box.createVerticalStrut(12));

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        foot.setOpaque(false);
        JLabel fl = new JLabel("Belum punya akun?");
        fl.setForeground(ABU); fl.setFont(ft(Font.PLAIN, 14));
        Tombol daftar = new Tombol("Daftar", Color.WHITE, PRIMER, null);
        daftar.setBackground(new Color(255, 255, 255, 0));
        daftar.setFont(ft(Font.BOLD, 14));
        daftar.setPreferredSize(new Dimension(60, 26));
        daftar.addActionListener(e -> dialogPengguna(null, true));
        foot.add(fl); foot.add(daftar);
        foot.setPreferredSize(new Dimension(100, 28));
        foot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(foot);

        kolomUtama.add(card);
        latar.add(kolomUtama);
        return latar;
    }

    static JLabel labelForm(String t) {
        JLabel l = new JLabel(t);
        l.setFont(ft(Font.BOLD, 14)); l.setForeground(GELAP);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static JComponent kolomRata(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // ======================= KERANGKA APLIKASI (SIDEBAR) =======================
    static class ItemMenu extends JButton {
        String ikon; boolean aktif, hover;
        ItemMenu(String teks, boolean aktif) {
            super(teks);
            this.ikon = teks; this.aktif = aktif;
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 46, 0, 0));
            setFont(ft(aktif ? Font.BOLD : Font.PLAIN, 15));
            setForeground(aktif ? PRIMER : new Color(55, 65, 81));
            setPreferredSize(new Dimension(190, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (aktif) {
                g.setColor(UNGU_MUDA);
                g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g.setColor(PRIMER);
                g.fillRoundRect(0, 9, 4, h - 18, 4, 4);
            } else if (hover) {
                g.setColor(new Color(246, 244, 255));
                g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            }
            g.setColor(aktif ? PRIMER : new Color(75, 85, 99));
            g.translate(18, (h - 15) / 2);
            gambarIkon(g, ikon);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    JComponent sidebar(String menu) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1, GARIS), new EmptyBorder(16, 14, 14, 14)));
        p.setPreferredSize(new Dimension(230, 0));

        JComponent logo = logoMerek();
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.add(logo);
        p.add(Box.createVerticalStrut(28));

        String[] daftar = "Admin".equals(masuk.role)
                ? new String[]{"Dashboard", "Katalog", "Kelola Buku", "Kelola Pengguna", "Beranda", "Keluar"}
                : new String[]{"Dashboard", "Katalog", "Rak Saya", "Beranda", "Keluar"};
        for (String m : daftar) {
            if (m.equals("Beranda") || m.equals("Keluar")) {
                if (m.equals("Beranda")) p.add(Box.createVerticalStrut(0));
            }
            ItemMenu it = new ItemMenu(m, m.equals(menu));
            it.addActionListener(e -> {
                if (m.equals("Beranda")) bukaBeranda();
                else if (m.equals("Keluar")) keluarAkun();
                else bukaShell(m);
            });
            p.add(it);
            p.add(Box.createVerticalStrut(8));
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    JComponent headerShell(String menu) {
        String judul, sub;
        boolean adm = "Admin".equals(masuk.role);
        switch (menu) {
            case "Dashboard":
                judul = adm ? "Dashboard Admin" : "Dashboard User";
                sub = adm ? "Kelola sistem PustakaDigital." : "Selamat datang kembali, " + masuk.nama.split(" ")[0] + ".";
                break;
            case "Katalog": judul = "Katalog"; sub = "Jelajahi seluruh koleksi PustakaDigital."; break;
            case "Kelola Buku": judul = "Kelola Buku"; sub = "Tambah, ubah, dan hapus data buku."; break;
            case "Kelola Pengguna": judul = "Kelola Pengguna"; sub = "Daftar pengguna PustakaDigital."; break;
            default: judul = "Rak Saya"; sub = "Buku yang sudah kamu simpan."; break;
        }
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GARIS), new EmptyBorder(10, 26, 10, 26)));
        p.setPreferredSize(new Dimension(0, 66));
        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(judul);
        a.setFont(ft(Font.BOLD, 17)); a.setForeground(GELAP);
        JLabel b = new JLabel(sub);
        b.setFont(ft(Font.PLAIN, 13)); b.setForeground(ABU);
        kiri.add(a); kiri.add(b);
        p.add(kiri, BorderLayout.WEST);
        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        kanan.add(new Chip(masuk.role, UNGU_MUDA, PRIMER));
        p.add(kanan, BorderLayout.EAST);
        return p;
    }

    void bukaShell(String menu) {
        if (masuk == null) { bukaLogin(); return; }
        menuAktif = menu;
        JPanel p = new JPanel(new BorderLayout());
        p.add(sidebar(menu), BorderLayout.WEST);
        JPanel kanan = new JPanel(new BorderLayout());
        kanan.add(headerShell(menu), BorderLayout.NORTH);
        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(LATAR);
        isi.setBorder(new EmptyBorder(16, 26, 12, 26));
        boolean adm = "Admin".equals(masuk.role);
        JComponent konten;
        switch (menu) {
            case "Dashboard": konten = adm ? dashAdmin() : dashUser(); break;
            case "Katalog": konten = isiKatalog(""); break;
            case "Kelola Buku": konten = halKelolaBuku(); break;
            case "Kelola Pengguna": konten = halKelolaPengguna(); break;
            default: konten = halRakSaya(); break;
        }
        isi.add(konten, BorderLayout.CENTER);
        kanan.add(isi, BorderLayout.CENTER);
        p.add(kanan, BorderLayout.CENTER);
        ganti(p);
    }

    // ======================= KOMPONEN HALAMAN =======================
    JComponent judulHalaman(String t, String s, JComponent kanan) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(t);
        a.setFont(ft(Font.BOLD, 30)); a.setForeground(GELAP);
        JLabel b = new JLabel(s);
        b.setFont(ft(Font.PLAIN, 14)); b.setForeground(ABU);
        kiri.add(a); kiri.add(Box.createVerticalStrut(2)); kiri.add(b);
        p.add(kiri, BorderLayout.WEST);
        if (kanan != null) {
            JPanel w = new JPanel(new GridBagLayout());
            w.setOpaque(false);
            w.add(kanan);
            p.add(w, BorderLayout.EAST);
        }
        return p;
    }

    JComponent statKartu(String label, String nilai, String sub, Color warnaSub, String ikon) {
        Bulat p = new Bulat(Color.WHITE, GARIS, 22);
        p.setLayout(new BorderLayout(0, 2));
        p.setBorder(new EmptyBorder(12, 18, 10, 14));
        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(ft(Font.PLAIN, 14)); l.setForeground(new Color(55, 65, 81));
        atas.add(l, BorderLayout.WEST);
        atas.add(new IkonBox(ikon, UNGU_MUDA, PRIMER, 36, 1.0), BorderLayout.EAST);
        JLabel n = new JLabel(nilai);
        n.setFont(ft(Font.BOLD, 30)); n.setForeground(GELAP);
        p.add(atas, BorderLayout.NORTH);
        p.add(n, BorderLayout.CENTER);
        if (sub != null) {
            JLabel s = new JLabel(sub);
            s.setFont(ft(Font.PLAIN, 13)); s.setForeground(warnaSub);
            p.add(s, BorderLayout.SOUTH);
        }
        p.setPreferredSize(new Dimension(100, 112));
        return p;
    }

    JComponent barisStat(JComponent... kartu) {
        JPanel p = new JPanel(new GridLayout(1, kartu.length, 16, 0));
        p.setOpaque(false);
        for (JComponent k : kartu) p.add(k);
        return p;
    }

    // ======================= DASHBOARD ADMIN =======================
    JComponent dashAdmin() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        Tombol tambah = primer("+  Tambah Buku");
        tambah.setPreferredSize(new Dimension(150, 42));
        tambah.addActionListener(e -> dialogBuku(null));
        JPanel atas = new JPanel(new BorderLayout(0, 14));
        atas.setOpaque(false);
        atas.add(judulHalaman("Dashboard Admin", "Pantau seluruh aktivitas PustakaDigital.", tambah), BorderLayout.NORTH);

        Set<String> kat = new HashSet<>();
        int dibaca = 0;
        for (Buku b : buku) kat.add(b.kategori);
        for (Pengguna u : users) dibaca += u.dibaca;
        atas.add(barisStat(
                statKartu("Total Buku", String.valueOf(buku.size()), "buku tersimpan", HIJAU_TX, "Katalog"),
                statKartu("Total Pengguna", String.valueOf(users.size()), "pengguna terdaftar", HIJAU_TX, "Kelola Pengguna"),
                statKartu("Sedang Dibaca", String.valueOf(dibaca), "Aktif dibaca", ABU, "Kelola Buku"),
                statKartu("Kategori", String.valueOf(kat.size()), "Genre tersedia", ABU, "Tag")), BorderLayout.CENTER);

        int dari = Math.max(0, buku.size() - 3);
        List<Buku> terbaru = new ArrayList<>(buku.subList(dari, buku.size()));
        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        tengah.add(panelTabelBuku(terbaru, "Data Buku Terbaru", "Buku yang baru ditambahkan ke sistem.", true), BorderLayout.NORTH);
        p.add(atas, BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    JComponent panelTabelBuku(List<Buku> sumber, String judulKartu, String subKartu, boolean lihatKatalog) {
        Bulat kartu = new Bulat(Color.WHITE, GARIS, 22);
        kartu.setLayout(new BorderLayout(0, 8));
        kartu.setBorder(new EmptyBorder(16, 22, 14, 22));

        JPanel atas = new JPanel();
        atas.setOpaque(false);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        if (judulKartu != null) {
            JPanel jb = new JPanel(new BorderLayout());
            jb.setOpaque(false);
            JPanel kiri = new JPanel();
            kiri.setOpaque(false);
            kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
            JLabel a = new JLabel(judulKartu);
            a.setFont(ft(Font.BOLD, 19)); a.setForeground(GELAP);
            JLabel b = new JLabel(subKartu);
            b.setFont(ft(Font.PLAIN, 13)); b.setForeground(ABU);
            kiri.add(a); kiri.add(b);
            jb.add(kiri, BorderLayout.WEST);
            if (lihatKatalog) {
                Tombol lk = garisTipis("Lihat Katalog");
                lk.setPreferredSize(new Dimension(130, 38));
                lk.addActionListener(e -> bukaShell("Katalog"));
                JPanel w = new JPanel(new GridBagLayout());
                w.setOpaque(false);
                w.add(lk);
                jb.add(w, BorderLayout.EAST);
            }
            jb.setAlignmentX(Component.LEFT_ALIGNMENT);
            atas.add(jb);
            atas.add(Box.createVerticalStrut(10));
        }
        Isian cari = new Isian("Cari judul atau penulis...");
        Chip badge = new Chip(sumber.size() + " Buku", UNGU_MUDA, PRIMER);
        JPanel barisCari = new JPanel(new BorderLayout());
        barisCari.setOpaque(false);
        Bulat kc = kolom(cari, 42);
        kc.setPreferredSize(new Dimension(380, 42));
        JPanel kcw = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        kcw.setOpaque(false);
        kcw.add(kc);
        barisCari.add(kcw, BorderLayout.WEST);
        JPanel bw = new JPanel(new GridBagLayout());
        bw.setOpaque(false);
        bw.add(badge);
        barisCari.add(bw, BorderLayout.EAST);
        barisCari.setAlignmentX(Component.LEFT_ALIGNMENT);
        barisCari.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        atas.add(barisCari);
        kartu.add(atas, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Judul", "Penulis", "Kategori", "Penerbit", "Tahun", "Halaman", "Format", "Status", "Aksi"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Buku b : sumber) m.addRow(new Object[]{b, b, b, b, b, b, b, b, b});
        JTable t = new JTable(m);
        gayaTabel(t);
        t.setDefaultRenderer(Object.class, new RenBuku());
        int[] lebar = {190, 130, 115, 95, 50, 65, 50, 105, AKSI_LEBAR};
        for (int i = 0; i < lebar.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        TableColumn ka = t.getColumnModel().getColumn(8);
        ka.setMinWidth(AKSI_LEBAR); ka.setMaxWidth(AKSI_LEBAR);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        for (int i = 0; i < 9; i++) sorter.setSortable(i, false);
        t.setRowSorter(sorter);
        cari.getDocument().addDocumentListener(dl(() -> {
            final String q = cari.getText();
            sorter.setRowFilter(q.isBlank() ? null : new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> en) {
                    return ((Buku) en.getValue(0)).cocok(q);
                }
            });
            badge.set(t.getRowCount() + " Buku");
            kartu.revalidate();
            kartu.repaint();
        }));

        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = t.rowAtPoint(e.getPoint()), col = t.columnAtPoint(e.getPoint());
                if (row < 0 || col != 8) return;
                Rectangle r = t.getCellRect(row, col, false);
                Buku b = (Buku) m.getValueAt(t.convertRowIndexToModel(row), 0);
                if (e.getX() - r.x < 8 + 42) dialogBuku(b); else hapusBuku(b);
            }
        });
        t.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                t.setCursor(t.columnAtPoint(e.getPoint()) == 8 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });

        JPanel tabel = new JPanel(new BorderLayout());
        tabel.setOpaque(false);
        tabel.add(t.getTableHeader(), BorderLayout.NORTH);
        tabel.add(t, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(tabel, BorderLayout.NORTH);
        kartu.add(wrap, BorderLayout.CENTER);
        return kartu;
    }

    // ======================= KELOLA BUKU =======================
    JComponent halKelolaBuku() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        Tombol tambah = primer("+  Tambah Buku");
        tambah.setPreferredSize(new Dimension(150, 42));
        tambah.addActionListener(e -> dialogBuku(null));
        p.add(judulHalaman("Kelola Buku", "Semua data buku yang tersimpan di PustakaDigital.", tambah), BorderLayout.NORTH);
        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        tengah.add(panelTabelBuku(new ArrayList<>(buku), null, null, false), BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    // ======================= KELOLA PENGGUNA =======================
    JComponent halKelolaPengguna() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        Tombol tambah = primer("+  Tambah Pengguna");
        tambah.setPreferredSize(new Dimension(180, 42));
        tambah.addActionListener(e -> dialogPengguna(null, false));
        p.add(judulHalaman("Kelola Pengguna", "Kelola data pengguna sistem.", tambah), BorderLayout.NORTH);

        Bulat kartu = new Bulat(Color.WHITE, GARIS, 22);
        kartu.setLayout(new BorderLayout(0, 8));
        kartu.setBorder(new EmptyBorder(16, 22, 14, 22));

        Isian cari = new Isian("Cari nama atau email...");
        Chip badge = new Chip(users.size() + " Pengguna", UNGU_MUDA, PRIMER);
        JPanel barisCari = new JPanel(new BorderLayout());
        barisCari.setOpaque(false);
        Bulat kc = kolom(cari, 42);
        kc.setPreferredSize(new Dimension(380, 42));
        JPanel kcw = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        kcw.setOpaque(false);
        kcw.add(kc);
        barisCari.add(kcw, BorderLayout.WEST);
        JPanel bw = new JPanel(new GridBagLayout());
        bw.setOpaque(false);
        bw.add(badge);
        barisCari.add(bw, BorderLayout.EAST);
        kartu.add(barisCari, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new String[]{"Nama", "Email", "Role", "Status", "Sedang Dibaca", "Aksi"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Pengguna u : users) m.addRow(new Object[]{u, u, u, u, u, u});
        JTable t = new JTable(m);
        gayaTabel(t);
        t.setDefaultRenderer(Object.class, new RenPengguna());
        int[] lebar = {260, 220, 90, 100, 120, AKSI_LEBAR};
        for (int i = 0; i < lebar.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        TableColumn ka = t.getColumnModel().getColumn(5);
        ka.setMinWidth(AKSI_LEBAR); ka.setMaxWidth(AKSI_LEBAR);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        for (int i = 0; i < 6; i++) sorter.setSortable(i, false);
        t.setRowSorter(sorter);
        cari.getDocument().addDocumentListener(dl(() -> {
            final String q = cari.getText();
            sorter.setRowFilter(q.isBlank() ? null : new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> en) {
                    return ((Pengguna) en.getValue(0)).cocok(q);
                }
            });
            badge.set(t.getRowCount() + " Pengguna");
            kartu.revalidate();
            kartu.repaint();
        }));
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = t.rowAtPoint(e.getPoint()), col = t.columnAtPoint(e.getPoint());
                if (row < 0 || col != 5) return;
                Rectangle r = t.getCellRect(row, col, false);
                Pengguna u = (Pengguna) m.getValueAt(t.convertRowIndexToModel(row), 0);
                if (e.getX() - r.x < 8 + 42) dialogPengguna(u, false); else hapusPengguna(u);
            }
        });
        t.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                t.setCursor(t.columnAtPoint(e.getPoint()) == 5 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });

        JPanel tabel = new JPanel(new BorderLayout());
        tabel.setOpaque(false);
        tabel.add(t.getTableHeader(), BorderLayout.NORTH);
        tabel.add(t, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(tabel, BorderLayout.NORTH);
        kartu.add(wrap, BorderLayout.CENTER);

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        tengah.add(kartu, BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    // ======================= DASHBOARD USER =======================
    JComponent dashUser() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        JPanel atas = new JPanel(new BorderLayout(0, 14));
        atas.setOpaque(false);
        atas.add(judulHalaman("Ringkasan Aktivitas", "Kelola bacaan dan koleksi pribadi kamu.", null), BorderLayout.NORTH);
        int baca = 0, selesai = 0;
        for (int v : rak.values()) { if (v >= 100) selesai++; else baca++; }
        atas.add(barisStat(
                statKartu("Rak Saya", String.valueOf(rak.size()), null, ABU, "Rak Saya"),
                statKartu("Sedang Dibaca", String.valueOf(baca), null, ABU, "Katalog"),
                statKartu("Selesai Dibaca", String.valueOf(selesai), null, ABU, "Selesai"),
                statKartu("Jam Membaca", String.valueOf(jamBaca), null, ABU, "Jam")), BorderLayout.CENTER);
        for (Component c : ((JPanel) atas.getComponent(1)).getComponents()) c.setPreferredSize(new Dimension(100, 98));

        // bagian bawah
        JPanel bawah = new JPanel(new GridBagLayout());
        bawah.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.gridy = 0;

        Bulat kiri = new Bulat(Color.WHITE, GARIS, 22);
        kiri.setLayout(new BorderLayout(0, 6));
        kiri.setBorder(new EmptyBorder(16, 22, 12, 22));
        JPanel hk = new JPanel(new BorderLayout());
        hk.setOpaque(false);
        JLabel jk = new JLabel("Sedang Dibaca");
        jk.setFont(ft(Font.BOLD, 19)); jk.setForeground(GELAP);
        Tombol lk = new Tombol("Lihat Katalog", Color.WHITE, PRIMER, null);
        lk.setBackground(new Color(255, 255, 255, 0));
        lk.setFont(ft(Font.PLAIN, 14));
        lk.addActionListener(e -> bukaShell("Katalog"));
        hk.add(jk, BorderLayout.WEST);
        hk.add(lk, BorderLayout.EAST);
        kiri.add(hk, BorderLayout.NORTH);

        JPanel daftar = new JPanel(new GridLayout(3, 1, 0, 0));
        daftar.setOpaque(false);
        int n = 0;
        for (Map.Entry<String, Integer> en : rak.entrySet()) {
            if (en.getValue() >= 100 || n >= 3) continue;
            Buku b = cariBuku(en.getKey());
            String kt = b == null ? "Lainnya" : b.kategori;
            Color[] t = tema(kt);
            JPanel baris = new JPanel(new BorderLayout(14, 0));
            baris.setOpaque(false);
            baris.setBorder(new MatteBorder(0, 0, 1, 0, new Color(240, 238, 248)));
            JPanel ik = new JPanel(new GridBagLayout());
            ik.setOpaque(false);
            ik.add(new IkonBox(ikonKategori(kt), t[0], t[1], 52, 1.2));
            JPanel tx = new JPanel();
            tx.setOpaque(false);
            tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS));
            tx.setBorder(new EmptyBorder(10, 0, 8, 0));
            JLabel jd = new JLabel(en.getKey());
            jd.setFont(ft(Font.BOLD, 15)); jd.setForeground(GELAP);
            JLabel pn = new JLabel(b == null ? "-" : b.penulis);
            pn.setFont(ft(Font.PLAIN, 13)); pn.setForeground(ABU);
            Bar bar = new Bar(en.getValue(), t[1]);
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            jd.setAlignmentX(Component.LEFT_ALIGNMENT);
            pn.setAlignmentX(Component.LEFT_ALIGNMENT);
            tx.add(jd); tx.add(pn); tx.add(Box.createVerticalStrut(6)); tx.add(bar);
            JLabel pr = new JLabel(en.getValue() + "%");
            pr.setFont(ft(Font.BOLD, 13)); pr.setForeground(TEKS);
            baris.add(ik, BorderLayout.WEST);
            baris.add(tx, BorderLayout.CENTER);
            baris.add(pr, BorderLayout.EAST);
            daftar.add(baris);
            n++;
        }
        if (n == 0) {
            JLabel kosong = new JLabel("Belum ada bacaan yang sedang berjalan.");
            kosong.setForeground(ABU);
            daftar.add(kosong);
            n = 1;
        }
        for (; n < 3; n++) daftar.add(new JLabel());
        kiri.add(daftar, BorderLayout.CENTER);
        g.gridx = 0; g.weightx = 0.64; g.insets = new Insets(0, 0, 0, 8);
        bawah.add(kiri, g);

        Bulat kanan = new Bulat(Color.WHITE, GARIS, 22);
        kanan.setLayout(new BorderLayout(0, 6));
        kanan.setBorder(new EmptyBorder(16, 22, 12, 22));
        JLabel ja = new JLabel("Aktivitas Terbaru");
        ja.setFont(ft(Font.BOLD, 19)); ja.setForeground(GELAP);
        kanan.add(ja, BorderLayout.NORTH);
        JPanel la = new JPanel(new GridLayout(3, 1));
        la.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            JPanel it = new JPanel();
            it.setOpaque(false);
            it.setLayout(new BoxLayout(it, BoxLayout.Y_AXIS));
            it.setBorder(new CompoundBorder(new MatteBorder(0, 0, i < 2 ? 1 : 0, 0, new Color(240, 238, 248)), new EmptyBorder(10, 0, 8, 0)));
            if (i < aktivitas.size()) {
                JLabel a = new JLabel(aktivitas.get(i)[0]);
                a.setFont(ft(Font.BOLD, 15)); a.setForeground(GELAP);
                JLabel b = new JLabel(aktivitas.get(i)[1]);
                b.setFont(ft(Font.PLAIN, 13)); b.setForeground(ABU);
                it.add(a); it.add(b);
            }
            la.add(it);
        }
        kanan.add(la, BorderLayout.CENTER);
        g.gridx = 1; g.weightx = 0.36; g.insets = new Insets(0, 8, 0, 0);
        bawah.add(kanan, g);

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        bawah.setPreferredSize(new Dimension(0, 300));
        tengah.add(bawah, BorderLayout.CENTER);
        p.add(atas, BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    // ======================= RAK SAYA =======================
    void lanjutBaca(String judul) {
        int cur = rak.get(judul);
        JSlider s = new JSlider(0, 100, cur);
        s.setMajorTickSpacing(25);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        int ok = JOptionPane.showConfirmDialog(this, new Object[]{"Progress membaca \"" + judul + "\":", s},
                "Lanjut Baca", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            int baru = s.getValue();
            if (baru > cur) jamBaca += 1;
            rak.put(judul, baru);
            if (baru >= 100 && cur < 100) aktivitas.add(0, new String[]{"Menyelesaikan bacaan", judul + " \u00B7 Baru saja"});
            bukaShell("Rak Saya");
        }
    }

    JComponent kartuRak(String judul, int prog) {
        Buku b = cariBuku(judul);
        String kt = b == null ? "Lainnya" : b.kategori;
        Color[] t = tema(kt);
        Bulat k = new Bulat(Color.WHITE, GARIS, 18);
        k.setLayout(new BorderLayout());
        k.setBorder(new EmptyBorder(1, 1, 1, 1));

        Bulat cover = new Bulat(t[0], null, 18);
        cover.setLayout(new GridBagLayout());
        cover.setPreferredSize(new Dimension(0, 52));
        cover.add(new IkonBox(ikonKategori(kt), null, t[1], 40, 1.5));
        k.add(cover, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(8, 14, 10, 14));
        JPanel cp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cp.setOpaque(false);
        cp.add(chipKategori(kt));
        rata(cp);
        info.add(cp);
        info.add(Box.createVerticalStrut(5));
        JLabel jd = new JLabel(judul);
        jd.setFont(ft(Font.BOLD, 17)); jd.setForeground(GELAP);
        rata(jd);
        info.add(jd);
        JLabel pn = new JLabel(b == null ? "-" : b.penulis);
        pn.setFont(ft(Font.PLAIN, 12)); pn.setForeground(ABU);
        rata(pn);
        info.add(pn);
        info.add(Box.createVerticalStrut(6));
        Bar bar = new Bar(prog, prog >= 100 ? t[1] : t[1]);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(bar);
        info.add(Box.createVerticalStrut(4));
        if (prog >= 100) {
            JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            sp.setOpaque(false);
            sp.add(new Chip("Selesai Dibaca", HIJAU_BG, HIJAU_TX));
            rata(sp);
            info.add(sp);
        } else {
            JLabel pt = new JLabel(prog + "% selesai dibaca");
            pt.setFont(ft(Font.PLAIN, 12)); pt.setForeground(ABU);
            rata(pt);
            info.add(pt);
        }
        info.add(Box.createVerticalGlue());

        JPanel tb = new JPanel(new GridLayout(2, 1, 0, 6));
        tb.setOpaque(false);
        if (prog >= 100) {
            JPanel r1 = new JPanel(new GridLayout(1, 2, 6, 0));
            r1.setOpaque(false);
            Tombol sel = primer("Buku Selanjutnya").kecil();
            Tombol ulang = garisTipis("Baca Ulang").kecil();
            sel.addActionListener(e -> bukaShell("Katalog"));
            ulang.addActionListener(e -> { rak.put(judul, 0); bukaShell("Rak Saya"); });
            r1.add(sel); r1.add(ulang);
            tb.add(r1);
        } else {
            Tombol lanjut = hijau("Lanjut Baca").kecil();
            lanjut.addActionListener(e -> lanjutBaca(judul));
            tb.add(lanjut);
        }
        Tombol hapus = garisTipis("Hapus dari Rak").kecil();
        hapus.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Hapus \"" + judul + "\" dari rak?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                rak.remove(judul);
                bukaShell("Rak Saya");
            }
        });
        tb.add(hapus);
        tb.setPreferredSize(new Dimension(100, 62));
        tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        tb.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(tb);
        k.add(info, BorderLayout.CENTER);
        return k;
    }

    JComponent halRakSaya() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        Isian cari = new Isian("Cari di rak");
        JComboBox<String> urut = new JComboBox<>(new String[]{"Terbaru", "Judul A-Z", "Progress"});
        urut.setFont(ft(Font.PLAIN, 14));
        urut.setBackground(Color.WHITE);
        boolean[] sembunyi = {false};
        Tombol filter = garisTipis("Filter: Semua");
        filter.setPreferredSize(new Dimension(130, 42));

        JPanel atas = new JPanel(new BorderLayout(10, 0));
        atas.setOpaque(false);
        atas.add(kolom(cari, 42), BorderLayout.CENTER);
        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kanan.setOpaque(false);
        Bulat ku = kolom(urut, 42);
        ku.setPreferredSize(new Dimension(150, 42));
        kanan.add(ku);
        kanan.add(filter);
        atas.add(kanan, BorderLayout.EAST);
        atas.setPreferredSize(new Dimension(0, 42));

        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 500));
        Runnable isi = () -> {
            grid.removeAll();
            List<Map.Entry<String, Integer>> daftar = new ArrayList<>();
            for (Map.Entry<String, Integer> en : rak.entrySet()) {
                Buku b = cariBuku(en.getKey());
                String q = cari.getText().toLowerCase().trim();
                boolean cocok = q.isEmpty() || en.getKey().toLowerCase().contains(q)
                        || (b != null && b.penulis.toLowerCase().contains(q));
                if (cocok && !(sembunyi[0] && en.getValue() >= 100)) daftar.add(en);
            }
            String o = (String) urut.getSelectedItem();
            if ("Terbaru".equals(o)) Collections.reverse(daftar);
            else if ("Judul A-Z".equals(o)) daftar.sort(Comparator.comparing(Map.Entry::getKey));
            else daftar.sort((a, c) -> c.getValue() - a.getValue());
            int n = 0;
            for (Map.Entry<String, Integer> en : daftar) {
                if (n++ >= 6) break;
                grid.add(kartuRak(en.getKey(), en.getValue()));
            }
            for (; n < 6; n++) {
                JPanel k = new JPanel();
                k.setOpaque(false);
                grid.add(k);
            }
            grid.revalidate();
            grid.repaint();
        };
        cari.getDocument().addDocumentListener(dl(isi));
        urut.addActionListener(e -> isi.run());
        filter.addActionListener(e -> {
            sembunyi[0] = !sembunyi[0];
            filter.setText(sembunyi[0] ? "Filter: Belum selesai" : "Filter: Semua");
            isi.run();
        });
        isi.run();

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        tengah.add(grid, BorderLayout.CENTER);
        p.add(atas, BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    // ======================= MAIN =======================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new PustakaDigitalApp().setVisible(true);
        });
    }
}
