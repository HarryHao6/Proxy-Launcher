package com.proxylauncher.service;

import com.proxylauncher.model.AppEntry;
import com.proxylauncher.model.LaunchRequest;
import com.proxylauncher.util.ConfigPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LaunchLogService {
    private static final String DIVIDER = "--------------------------------------------------------------------------------";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Path logFile;

    public LaunchLogService() {
        this(ConfigPaths.resolveLaunchLogFile());
    }

    public LaunchLogService(Path logFile) {
        this.logFile = logFile;
    }

    public Path getLogFile() {
        return logFile;
    }

    public Path getLogDirectory() {
        return logFile.getParent();
    }

    public void logSuccess(LaunchRequest request, LaunchPlan plan, long processId) {
        writeEntry(request, plan, "SUCCESS", "PID: " + processId, null);
    }

    public void logFailure(LaunchRequest request, LaunchPlan plan, Exception exception) {
        writeEntry(request, plan, "FAILURE", null, exception);
    }

    private void writeEntry(LaunchRequest request, LaunchPlan plan, String result, String extraDetail, Exception exception) {
        try {
            Files.createDirectories(getLogDirectory());
            Files.writeString(
                    logFile,
                    formatEntry(request, plan, result, extraDetail, exception),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // Launch logging should never block the actual application launch flow.
        }
    }

    private String formatEntry(LaunchRequest request,
                               LaunchPlan plan,
                               String result,
                               String extraDetail,
                               Exception exception) {
        AppEntry appEntry = request.getAppEntry();
        String lineSeparator = System.lineSeparator();
        StringBuilder entry = new StringBuilder();
        entry.append(DIVIDER).append(lineSeparator);
        entry.append("Timestamp: ").append(TIMESTAMP_FORMATTER.format(OffsetDateTime.now())).append(lineSeparator);
        entry.append("Result: ").append(result).append(lineSeparator);
        entry.append("App: ").append(appEntry).append(lineSeparator);
        entry.append("Executable: ").append(appEntry.getExecutablePath()).append(lineSeparator);
        entry.append("Launch Strategy: ").append(plan.launchStrategy()).append(lineSeparator);
        entry.append("Proxy Mode: ").append(request.getProxyMode()).append(lineSeparator);
        entry.append("Resolved Proxy: ")
                .append(plan.resolvedProxy().isBlank() ? "<none>" : plan.resolvedProxy())
                .append(lineSeparator);
        entry.append("Working Directory: ")
                .append(plan.workingDirectory() == null ? "<none>" : plan.workingDirectory())
                .append(lineSeparator);
        entry.append("Command: ")
                .append(plan.command().stream().collect(Collectors.joining(" | ")))
                .append(lineSeparator);
        entry.append("Injected Proxy Environment:").append(lineSeparator);

        if (plan.proxyEnvironment().isEmpty()) {
            entry.append("  <none>").append(lineSeparator);
        } else {
            for (Map.Entry<String, String> proxyEntry : new TreeMap<>(plan.proxyEnvironment()).entrySet()) {
                entry.append("  ")
                        .append(proxyEntry.getKey())
                        .append("=")
                        .append(proxyEntry.getValue())
                        .append(lineSeparator);
            }
        }

        if (extraDetail != null && !extraDetail.isBlank()) {
            entry.append(extraDetail).append(lineSeparator);
        }

        if (exception != null) {
            entry.append("Exception: ")
                    .append(exception.getClass().getSimpleName())
                    .append(": ")
                    .append(exception.getMessage())
                    .append(lineSeparator);
        }

        entry.append(lineSeparator);
        return entry.toString();
    }
}
