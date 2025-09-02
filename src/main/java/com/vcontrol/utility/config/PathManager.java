package com.vcontrol.utility.config;

import java.io.IOException;
import java.util.Properties;

import com.vcontrol.utility.ResourceUtils;

public final class PathManager {

    public static String propertiesFile = "paths.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    private PathManager() {}

    public static synchronized void load() throws IOException {
        load(propertiesFile);
    }

    public static synchronized void load(String propertiesFilePath) throws IOException {
        if (loaded) return;
        try {
            properties.load(ResourceUtils.getResourceAsStream(propertiesFilePath));
            propertiesFile = propertiesFilePath;
            loaded = true;
            LogManager.log(LogManager.LogLevel.INFO, "Properties file loaded: " + propertiesFilePath);
        } catch (IOException e) {
            LogManager.log(LogManager.LogLevel.ERROR, "Error loading properties file: " + propertiesFilePath, e);
            throw new IOException();
        }
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        if (!loaded) {
            LogManager.log(LogManager.LogLevel.ERROR, "PathManager not initialized. Call PathManager.load(...) before using get().");
            return defaultValue;
        }
        String value = properties.getProperty(key);
        if (value == null) {
            LogManager.log(LogManager.LogLevel.WARN, "Key not found in properties: " + key);
            return defaultValue;
        }
        return value;
    }
}