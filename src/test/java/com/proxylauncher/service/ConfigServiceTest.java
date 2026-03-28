package com.proxylauncher.service;

import com.proxylauncher.model.AppConfig;
import com.proxylauncher.model.AppEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigServiceTest {
    @TempDir
    Path tempDir;

    @Test
    public void createsDefaultConfigAndFileWhenMissing() {
        Path configFile = tempDir.resolve("ProxyLauncher").resolve("config.json");
        ConfigService configService = new ConfigService(configFile);

        AppConfig config = configService.load();

        assertNotNull(config);
        assertEquals("", config.getDefaultProxy());
        assertTrue(config.getAppEntries().isEmpty());
        assertTrue(Files.exists(configFile));
    }

    @Test
    public void savesAndLoadsConfigRoundTrip() {
        Path configFile = tempDir.resolve("config.json");
        ConfigService configService = new ConfigService(configFile);

        AppConfig config = new AppConfig();
        config.setDefaultProxy("http://127.0.0.1:7890");
        config.setAppEntries(List.of(
                new AppEntry("Notepad", "C:\\Windows\\System32\\notepad.exe", "--test", "demo"),
                new AppEntry("Codex", "C:\\Tools\\Codex.exe", "", "second")
        ));

        configService.save(config);

        ConfigService reloadedService = new ConfigService(configFile);
        AppConfig reloaded = reloadedService.load();

        assertEquals("http://127.0.0.1:7890", reloaded.getDefaultProxy());
        assertEquals(2, reloaded.getAppEntries().size());
        assertEquals("Notepad", reloaded.getAppEntries().get(0).getName());
        assertEquals("C:\\Windows\\System32\\notepad.exe", reloaded.getAppEntries().get(0).getExecutablePath());
        assertEquals("--test", reloaded.getAppEntries().get(0).getArguments());
        assertEquals("demo", reloaded.getAppEntries().get(0).getNotes());
        assertEquals("Codex", reloaded.getAppEntries().get(1).getName());
    }

    @Test
    public void fallsBackToDefaultConfigWhenFileIsCorrupted() throws IOException {
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, "{invalid json}", StandardCharsets.UTF_8);

        ConfigService configService = new ConfigService(configFile);
        AppConfig config = configService.load();

        assertEquals("", config.getDefaultProxy());
        assertTrue(config.getAppEntries().isEmpty());
        assertFalse(configService.getLastLoadWarning().isBlank());
        assertNotEquals("{invalid json}", Files.readString(configFile, StandardCharsets.UTF_8).trim());
    }
}
