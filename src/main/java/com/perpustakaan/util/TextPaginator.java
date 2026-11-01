package com.perpustakaan.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Memecah teks panjang menjadi beberapa halaman tanpa scrollbar.
 */
public final class TextPaginator {

    private TextPaginator() {
    }

    public static List<String> paginate(String text, int maxCharacters) {
        if (text == null || text.isBlank()) {
            return List.of("");
        }
        int limit = Math.max(80, maxCharacters);
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        List<String> pages = new ArrayList<>();
        int index = 0;
        while (index < normalized.length()) {
            int end = Math.min(index + limit, normalized.length());
            if (end < normalized.length()) {
                int newline = normalized.lastIndexOf('\n', end - 1);
                int space = normalized.lastIndexOf(' ', end - 1);
                int breakAt = Math.max(newline, space);
                if (breakAt > index + limit / 2) {
                    end = breakAt + 1;
                }
            }
            String page = normalized.substring(index, end).trim();
            if (!page.isEmpty()) {
                pages.add(page);
            }
            index = end;
            while (index < normalized.length() && Character.isWhitespace(normalized.charAt(index))) {
                index++;
            }
        }
        return pages.isEmpty() ? List.of("") : pages;
    }
}
