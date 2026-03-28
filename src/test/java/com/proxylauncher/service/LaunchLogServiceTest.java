package com.proxylauncher.service;

import com.proxylauncher.model.AppEntry;
import com.proxylauncher.model.LaunchRequest;
import com.proxylauncher.model.ProxyMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LaunchLogServiceTest {
    @TempDir
    public Path tempDir;

    @Test
    public void writesLaunchDiagnosticsToTheLogFile() throws IOException {
        Path logFile = tempDir.resolve("launcher.log");
        LaunchLogService logService = new LaunchLogService(logFile);
        LaunchRequest request = new LaunchRequest(
                new AppEntry("Codex", "C:\\Tools\\Codex.exe", "--inspect", ""),
                ProxyMode.CUSTOM,
                "http://127.0.0.1:7890",
                "socks5://127.0.0.1:1080"
        );
        LaunchPlan plan = new LaunchPlan(
                List.of("cmd.exe", "/c", "start \"\" \"C:\\Tools\\Codex.exe\" --inspect"),
                null,
                Map.of(
                        "HTTP_PROXY", "socks5://127.0.0.1:1080",
                        "http_proxy", "socks5://127.0.0.1:1080"
                ),
                "Windows shell start",
                "socks5://127.0.0.1:1080"
        );

        logService.logSuccess(request, plan, 4242L);

        String logContents = Files.readString(logFile, StandardCharsets.UTF_8);
        assertTrue(logContents.contains("App: Codex"));
        assertTrue(logContents.contains("Launch Strategy: Windows shell start"));
        assertTrue(logContents.contains("Proxy Mode: Use Custom Proxy"));
        assertTrue(logContents.contains("Working Directory: <none>"));
        assertTrue(logContents.contains("HTTP_PROXY=socks5://127.0.0.1:1080"));
        assertTrue(logContents.contains("PID: 4242"));
    }
}
