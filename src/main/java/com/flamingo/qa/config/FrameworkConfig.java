package com.flamingo.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public final class FrameworkConfig {

    private static final String DEFAULT_CONFIG = "config/default.properties";
    private static final Properties PROPERTIES = loadDefaults();

    private FrameworkConfig() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (hasText(systemValue)) {
            return systemValue.trim();
        }

        String envValue = System.getenv(toEnvKey(key));
        if (hasText(envValue)) {
            return envValue.trim();
        }

        return Objects.toString(PROPERTIES.getProperty(key), "").trim();
    }

    public static String getRequired(String key) {
        String value = get(key);
        if (!hasText(value)) {
            throw new IllegalStateException("Missing required configuration value: " + key);
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(getRequired(key));
    }

    public static long getLong(String key) {
        return Long.parseLong(getRequired(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getRequired(key));
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG)) {
            if (stream == null) {
                throw new IllegalStateException("Default config was not found on classpath: " + DEFAULT_CONFIG);
            }
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load default config: " + DEFAULT_CONFIG, exception);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String toEnvKey(String key) {
        return key.replace('.', '_')
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }
}
