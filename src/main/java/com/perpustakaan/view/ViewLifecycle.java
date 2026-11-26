package com.perpustakaan.view;

/**
 * Lifecycle controller halaman.
 */
public interface ViewLifecycle {
    default void onShow() {
    }

    default void onHide() {
    }
}
