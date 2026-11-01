package com.perpustakaan.model;

import java.util.List;

/**
 * Hasil paginasi generik.
 */
public record PageResult<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems
) {

    public PageResult {
        items = items == null ? List.of() : List.copyOf(items);
        page = Math.max(1, page);
        pageSize = Math.max(1, pageSize);
        totalItems = Math.max(0, totalItems);
    }

    public int totalPages() {
        if (totalItems == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages();
    }

    public long fromIndex() {
        if (totalItems == 0) {
            return 0;
        }
        return (long) (page - 1) * pageSize + 1;
    }

    public long toIndex() {
        return Math.min((long) page * pageSize, totalItems);
    }

    public static <T> PageResult<T> empty(int page, int pageSize) {
        return new PageResult<>(List.of(), page, pageSize, 0);
    }
}
