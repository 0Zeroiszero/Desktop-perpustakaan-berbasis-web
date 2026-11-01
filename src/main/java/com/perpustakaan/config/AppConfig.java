package com.perpustakaan.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Konfigurasi aplikasi. Nilai dapat dioverride lewat environment variable
 * (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, APP_STORAGE_DIR).
 */
public final class AppConfig {

    private static AppConfig instance;

    private final String appName;
    private final int windowWidth;
    private final int windowHeight;
    private final int minWidth;
    private final int minHeight;
    private final String storageDir;

    private final String dbHost;
    private final int dbPort;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;
    private final String dbUrl;

    private final int bcryptCost;
    private final int catalogPageSize;
    private final int adminPageSize;
    private final int shelfPageSize;
    private final int dashboardRecentLimit;
    private final int recommendationLimit;
    private final long maxCoverBytes;
    private final long maxEbookBytes;
    private final float readerDpi;

    private AppConfig() {
        Properties app = loadProperties("config/app.properties");
        Properties database = loadProperties("config/database.properties");

        appName = text(app, "app.name", "PustakaDigital");
        windowWidth = positiveInt(app, "window.width", 1366);
        windowHeight = positiveInt(app, "window.height", 768);
        minWidth = positiveInt(app, "window.min.width", 1280);
        minHeight = positiveInt(app, "window.min.height", 720);
        storageDir = env("APP_STORAGE_DIR", text(app, "storage.dir", "storage"));

        dbHost = env("DB_HOST", text(database, "db.host", "localhost"));
        dbPort = positiveInt(database, "db.port", 3306);
        dbName = env("DB_NAME", text(database, "db.name", "db_pustaka"));
        dbUser = env("DB_USER", text(database, "db.user", "root"));
        dbPassword = env("DB_PASSWORD", text(database, "db.password", ""));
        dbUrl = buildUrl(dbHost, dbPort, dbName);

        bcryptCost = between(app, "security.bcrypt.cost", 12, 4, 31);
        catalogPageSize = positiveInt(app, "page.catalog.size", 8);
        adminPageSize = positiveInt(app, "page.admin.size", 8);
        shelfPageSize = positiveInt(app, "page.shelf.size", 7);
        dashboardRecentLimit = positiveInt(app, "dashboard.recent.limit", 5);
        recommendationLimit = positiveInt(app, "dashboard.recommendation.limit", 4);
        maxCoverBytes = positiveLong(app, "upload.cover.max.bytes", 5L * 1024 * 1024);
        maxEbookBytes = positiveLong(app, "upload.ebook.max.bytes", 50L * 1024 * 1024);
        readerDpi = positiveFloat(app, "reader.dpi", 110f);
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private static Properties loadProperties(String resource) {
        Properties properties = new Properties();
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("File konfigurasi tidak ditemukan: " + resource);
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca konfigurasi: " + resource, e);
        }
    }

    private static String text(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : value.trim();
    }

    private static int positiveInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalStateException("Nilai konfigurasi harus > 0: " + key);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Nilai konfigurasi tidak valid: " + key, e);
        }
    }

    private static int between(Properties properties, String key, int defaultValue, int min, int max) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min || parsed > max) {
                throw new IllegalStateException("Nilai konfigurasi di luar rentang: " + key);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Nilai konfigurasi tidak valid: " + key, e);
        }
    }

    private static long positiveLong(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new IllegalStateException("Nilai konfigurasi harus > 0: " + key);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Nilai konfigurasi tidak valid: " + key, e);
        }
    }

    private static float positiveFloat(Properties properties, String key, float defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            float parsed = Float.parseFloat(value.trim());
            if (parsed <= 0) {
                throw new IllegalStateException("Nilai konfigurasi harus > 0: " + key);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Nilai konfigurasi tidak valid: " + key, e);
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    private static String buildUrl(String host, int port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Asia/Jakarta"
                + "&characterEncoding=UTF-8"
                + "&useUnicode=true"
                + "&connectionTimeZone=Asia/Jakarta";
    }

    public String getAppName() {
        return appName;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public String getDbHost() {
        return dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public int getBcryptCost() {
        return bcryptCost;
    }

    public int getCatalogPageSize() {
        return catalogPageSize;
    }

    public int getAdminPageSize() {
        return adminPageSize;
    }

    public int getShelfPageSize() {
        return shelfPageSize;
    }

    public int getDashboardRecentLimit() {
        return dashboardRecentLimit;
    }

    public int getRecommendationLimit() {
        return recommendationLimit;
    }

    public long getMaxCoverBytes() {
        return maxCoverBytes;
    }

    public long getMaxEbookBytes() {
        return maxEbookBytes;
    }

    public float getReaderDpi() {
        return readerDpi;
    }
}
