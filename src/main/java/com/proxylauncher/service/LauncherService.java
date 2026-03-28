package com.proxylauncher.service;

import com.proxylauncher.model.AppEntry;
import com.proxylauncher.model.LaunchRequest;
import com.proxylauncher.model.ProxyMode;
import com.proxylauncher.util.ArgumentTokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class LauncherService {
    private static final List<String> PROXY_ENV_KEYS = List.of(
            "HTTP_PROXY",
            "HTTPS_PROXY",
            "ALL_PROXY",
            "http_proxy",
            "https_proxy",
            "all_proxy"
    );

    public Process launch(LaunchRequest request) throws IOException {
        AppEntry appEntry = request.getAppEntry();
        Path executable = Path.of(appEntry.getExecutablePath());

        ProcessBuilder builder = new ProcessBuilder(buildCommand(appEntry));
        if (executable.getParent() != null) {
            builder.directory(executable.getParent().toFile());
        }

        applyProxyEnvironment(builder.environment(), request);
        return builder.start();
    }

    List<String> buildCommand(AppEntry appEntry) {
        List<String> command = new ArrayList<>();
        command.add(appEntry.getExecutablePath());
        command.addAll(ArgumentTokenizer.tokenize(appEntry.getArguments()));
        return command;
    }

    void applyProxyEnvironment(Map<String, String> environment, LaunchRequest request) {
        configureProxyEnvironment(environment, resolveProxy(request));
    }

    String resolveProxy(LaunchRequest request) {
        return switch (request.getProxyMode()) {
            case NONE -> "";
            case CUSTOM -> request.getCustomProxy();
            case DEFAULT -> request.getDefaultProxy();
        };
    }

    void configureProxyEnvironment(Map<String, String> environment, String proxy) {
        for (String key : PROXY_ENV_KEYS) {
            environment.remove(key);
        }

        if (proxy == null || proxy.isBlank()) {
            return;
        }

        for (String key : PROXY_ENV_KEYS) {
            environment.put(key, proxy);
        }
    }
}
