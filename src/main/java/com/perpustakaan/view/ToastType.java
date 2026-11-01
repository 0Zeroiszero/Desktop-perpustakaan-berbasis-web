package com.perpustakaan.view;

/**
 * Jenis notifikasi toast.
 */
public enum ToastType {
    SUCCESS("toast-success", Icons.CHECK),
    ERROR("toast-error", Icons.ALERT),
    INFO("toast-info", Icons.INFO);

    private final String styleClass;
    private final String iconPath;

    ToastType(String styleClass, String iconPath) {
        this.styleClass = styleClass;
        this.iconPath = iconPath;
    }

    public String styleClass() {
        return styleClass;
    }

    public String iconPath() {
        return iconPath;
    }
}
