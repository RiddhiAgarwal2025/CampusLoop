package com.campusloop.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Properties;

/**
 * Immutable application configuration.
 *
 * Values come from classpath:/campusloop.properties and can be overridden by a
 * JVM system property ("campusloop." + key) or an environment variable
 * ("CAMPUSLOOP_" + key upper-cased with dots replaced by underscores).
 */
public final class AppConfig {

    private static final String RESOURCE = "/campusloop.properties";
    private static final long DEFAULT_MAX_UPLOAD_BYTES = 5L * 1024L * 1024L;

    private static volatile AppConfig instance;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final ZoneId zoneId;
    private final Path uploadDir;
    private final long maxUploadBytes;

    private AppConfig(Properties file) {
        this.dbUrl = required(file, "db.url");
        this.dbUser = required(file, "db.user");
        this.dbPassword = value(file, "db.password", "");

        String tz = value(file, "app.timezone", "Asia/Kolkata");
        ZoneId zone;
        try {
            zone = ZoneId.of(tz);
        } catch (Exception e) {
            zone = ZoneId.of("Asia/Kolkata");
        }
        this.zoneId = zone;

        String dir = value(file, "upload.dir", "");
        if (dir.isEmpty()) {
            this.uploadDir = Paths.get(System.getProperty("user.home"), "campusloop-uploads")
                    .toAbsolutePath().normalize();
        } else {
            this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        }

        long max;
        try {
            max = Long.parseLong(value(file, "upload.maxBytes", String.valueOf(DEFAULT_MAX_UPLOAD_BYTES)));
        } catch (NumberFormatException e) {
            max = DEFAULT_MAX_UPLOAD_BYTES;
        }
        this.maxUploadBytes = max > 0 ? max : DEFAULT_MAX_UPLOAD_BYTES;
    }

    /** Loads configuration once. Safe to call repeatedly. */
    public static AppConfig init() {
        AppConfig local = instance;
        if (local == null) {
            synchronized (AppConfig.class) {
                local = instance;
                if (local == null) {
                    local = new AppConfig(loadFile());
                    instance = local;
                }
            }
        }
        return local;
    }

    /** Returns the configuration, loading it lazily if the listener has not run yet. */
    public static AppConfig get() {
        AppConfig local = instance;
        return local != null ? local : init();
    }

    private static Properties loadFile() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing configuration file " + RESOURCE);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + RESOURCE, e);
        }
        return props;
    }

    private static String value(Properties file, String key, String fallback) {
        String sys = System.getProperty("campusloop." + key);
        if (sys != null && !sys.trim().isEmpty()) {
            return sys.trim();
        }
        String envName = "CAMPUSLOOP_" + key.toUpperCase(Locale.ROOT).replace('.', '_');
        String env = System.getenv(envName);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String fromFile = file.getProperty(key);
        if (fromFile != null) {
            return fromFile.trim();
        }
        return fallback;
    }

    private static String required(Properties file, String key) {
        String v = value(file, key, "");
        if (v.isEmpty()) {
            throw new IllegalStateException("Configuration property '" + key + "' is required");
        }
        return v;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public long getMaxUploadBytes() {
        return maxUploadBytes;
    }
}
