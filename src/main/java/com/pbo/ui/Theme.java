package com.pbo.ui;

import java.awt.Color;
import java.awt.Font;

/** Palet, font, dan ukuran. Warna kertas hangat + serif untuk konsep perpustakaan. */
public final class Theme {

    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color PURPLE_HOVER = new Color(0x6D28D9);
    public static final Color PURPLE_ACTIVE = new Color(0x5B21B6);
    public static final Color PURPLE_LIGHT = new Color(0xEDE9FE);

    public static final Color DARK = new Color(0x1E1B4B);
    public static final Color TEXT_SECONDARY = new Color(0x5D5A54);
    public static final Color GRAY = new Color(0x6F6A62);
    public static final Color PLACEHOLDER = new Color(0x7A746A);

    public static final Color BACKGROUND = new Color(0xFAF7F1);
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER = new Color(0xE8E2D8);
    public static final Color BORDER_INPUT = new Color(0xD8D1C5);
    public static final Color BORDER_SUBTLE = new Color(0xE2DCD1);
    public static final Color HOVER = new Color(0xF4EFE7);

    public static final Color BLUE_LIGHT = new Color(0xE0F2FE);
    public static final Color BLUE_DARK = new Color(0x0369A1);
    public static final Color BLUE_DARKER = new Color(0x075985);

    public static final Color RED_LIGHT = new Color(0xFEE2E2);
    public static final Color RED_DARK = new Color(0xDC2626);
    public static final Color RED_TEXT = new Color(0x58151C);

    public static final Color GREEN_DARK = new Color(0x15803D);
    public static final Color GREEN_DARKER = new Color(0x166534);

    public static final int SIDEBAR_WIDTH = 236;
    public static final int CONTENT_WIDTH = 1080;

    private static final String BODY_FAMILY = "Segoe UI";
    private static final String DISPLAY_FAMILY = "Georgia";

    private Theme() {
    }

    public static Font font(int style, float size) {
        // Ukuran yang diminta dalam px CSS; Java Font memakai poin (96 dpi: 1 px = 0.75 pt).
        return build(BODY_FAMILY, style, size);
    }

    /** Georgia untuk judul halaman, judul buku, dan heading — memberi nuansa pustaka. */
    public static Font displayFont(int style, float size) {
        return build(DISPLAY_FAMILY, style, size);
    }

    @SuppressWarnings("MagicConstant")
    private static Font build(String family, int style, float size) {
        return new Font(family, style, 12).deriveFont(style, size * 0.75f);
    }

    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
