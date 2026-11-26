package com.perpustakaan.view;

/**
 * Controller yang membutuhkan konteks navigasi.
 */
public interface ViewAware {
    void setContext(NavigationContext context);
}
