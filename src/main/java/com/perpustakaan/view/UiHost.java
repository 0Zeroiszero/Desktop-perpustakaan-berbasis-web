package com.perpustakaan.view;

/**
 * Kontrak host navigasi yang diimplementasikan RootNavigator.
 */
public interface UiHost {
    void navigate(Route route, NavigationContext context);

    void toast(String message, ToastType type);

    void setBusy(boolean busy, String message);

    void setImmersive(boolean immersive);

    boolean confirm(String title, String message, String confirmText, boolean danger);

    void refreshUser();
}
