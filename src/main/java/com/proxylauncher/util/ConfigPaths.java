package com.proxylauncher.util;

import java.nio.file.Path;

public final class ConfigPaths {
    private static final String APP_NAME = "ProxyLauncher";
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String LOG_DIRECTORY_NAME = "logs";
    private static final String LAUNCH_LOG_FILE_NAME = "launcher.log";

    private ConfigPaths() {
    }

    public static Path resolveConfigDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, APP_NAME);
        }
        return Path.of(System.getProperty("user.home"), "." + APP_NAME.toLowerCase());
    }

    public static Path resolveConfigFile() {
        return resolveConfigDirectory().resolve(CONFIG_FILE_NAME);
    }

    public static Path resolveLogsDirectory() {
        return resolveConfigDirectory().resolve(LOG_DIRECTORY_NAME);
    }

    public static Path resolveLaunchLogFile() {
        return resolveLogsDirectory().resolve(LAUNCH_LOG_FILE_NAME);
    }
}
