package com.pbo.ui.components;

import com.pbo.ui.Theme;

/** Warna badge kategori dan jumlah, mengikuti kelas utility di root.css. */
public final class Badges {

    private Badges() {
    }

    public static BadgeLabel category(String category) {
        return switch (category) {
            case "Teknologi", "Sejarah", "Sains" ->
                    new BadgeLabel(category, Theme.BLUE_LIGHT, Theme.BLUE_DARK, 10);
            case "Novel", "Komik" ->
                    new BadgeLabel(category, Theme.RED_LIGHT, Theme.RED_TEXT, 10);
            default ->
                    new BadgeLabel(category, Theme.PURPLE_LIGHT, Theme.PURPLE, 10);
        };
    }

    public static BadgeLabel count(String text) {
        BadgeLabel badge = new BadgeLabel(text, Theme.PURPLE_LIGHT, Theme.PURPLE, 999);
        badge.setFont(Theme.font(java.awt.Font.BOLD, 12f));
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 12, 5, 12));
        return badge;
    }

    /** Chip metadata ringan di halaman detail (tahun, halaman, format, ISBN). */
    public static BadgeLabel meta(String text) {
        BadgeLabel badge = new BadgeLabel(text, Theme.HOVER, Theme.TEXT_SECONDARY, 8);
        badge.setFont(Theme.font(java.awt.Font.PLAIN, 12f));
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 9, 4, 9));
        return badge;
    }
}
