package com.perpustakaan;

/**
 * Entry point terpisah dari kelas Application.
 * Kelas ini diperlukan agar aplikasi dapat dijalankan dari shaded JAR
 * tanpa error "JavaFX runtime components are missing".
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
