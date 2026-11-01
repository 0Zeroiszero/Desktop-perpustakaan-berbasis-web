package com.perpustakaan.util;

import com.perpustakaan.config.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logger aplikasi ke konsol dan file logs/app.log.
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("com.perpustakaan");
    private static boolean initialized;

    private AppLogger() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            Path logsDir = AppPaths.getLogsDir();
            Files.createDirectories(logsDir);
            FileHandler handler = new FileHandler(logsDir.resolve("app.log").toString(), 2_000_000, 3, true);
            handler.setEncoding("UTF-8");
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
            LOGGER.setLevel(Level.INFO);
            initialized = true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Gagal membuat file log. Log hanya tampil di konsol.", e);
        }
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message, Throwable error) {
        LOGGER.log(Level.WARNING, message, error);
    }

    public static void error(String message, Throwable error) {
        LOGGER.log(Level.SEVERE, message, error);
    }
}
