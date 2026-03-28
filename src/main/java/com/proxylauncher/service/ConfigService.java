package com.proxylauncher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.proxylauncher.model.AppConfig;
import com.proxylauncher.util.ConfigPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigService {
    private final ObjectMapper objectMapper;
    private final Path configFile;
    private String lastLoadWarning = "";

    public ConfigService() {
        this(ConfigPaths.resolveConfigFile());
    }

    public ConfigService(Path configFile) {
        this.configFile = configFile;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AppConfig load() {
        lastLoadWarning = "";

        if (Files.notExists(configFile)) {
            AppConfig defaultConfig = createDefaultConfig();
            save(defaultConfig);
            return defaultConfig;
        }

        try {
            AppConfig config = objectMapper.readValue(configFile.toFile(), AppConfig.class);
            return sanitize(config);
        } catch (IOException exception) {
            lastLoadWarning = "Configuration file is unreadable. Falling back to a fresh default configuration.";
            AppConfig defaultConfig = createDefaultConfig();
            save(defaultConfig);
            return defaultConfig;
        }
    }

    public void save(AppConfig config) {
        try {
            Files.createDirectories(configFile.getParent());
            objectMapper.writeValue(configFile.toFile(), sanitize(config));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save configuration to " + configFile, exception);
        }
    }

    public AppConfig createDefaultConfig() {
        return new AppConfig();
    }

    public Path getConfigFile() {
        return configFile;
    }

    public String getLastLoadWarning() {
        return lastLoadWarning;
    }

    private AppConfig sanitize(AppConfig config) {
        AppConfig safeConfig = config == null ? new AppConfig() : config;
        if (safeConfig.getDefaultProxy() == null) {
            safeConfig.setDefaultProxy("");
        }
        if (safeConfig.getAppEntries() == null) {
            safeConfig.setAppEntries(null);
        }
        return safeConfig;
    }
}
