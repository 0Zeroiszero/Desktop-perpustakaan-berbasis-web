package com.perpustakaan.view;

import com.perpustakaan.util.AppLogger;

/**
 * Akses statis ke navigasi dan notifikasi dari controller.
 */
public final class Ui {

    private static UiHost host;

    private Ui() {
    }

    public static void setHost(UiHost uiHost) {
        host = uiHost;
    }

    public static void navigate(Route route, NavigationContext context) {
        if (host == null) {
            AppLogger.warn("Navigasi dipanggil sebelum host siap.", null);
            return;
        }
        host.navigate(route, context);
    }

    public static void toast(String message, ToastType type) {
        if (host != null) {
            host.toast(message, type);
        } else {
            AppLogger.info("Notifikasi: " + message);
        }
    }

    public static void success(String message) {
        toast(message, ToastType.SUCCESS);
    }

    public static void error(String message) {
        toast(message, ToastType.ERROR);
    }

    public static void info(String message) {
        toast(message, ToastType.INFO);
    }

    public static void busy(boolean busy, String message) {
        if (host != null) {
            host.setBusy(busy, message);
        }
    }

    public static void immersive(boolean immersive) {
        if (host != null) {
            host.setImmersive(immersive);
        }
    }

    public static boolean confirm(String title, String message, String confirmText, boolean danger) {
        return host != null && host.confirm(title, message, confirmText, danger);
    }

    public static void refreshUser() {
        if (host != null) {
            host.refreshUser();
        }
    }
}
