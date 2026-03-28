package com.proxylauncher.service;

import com.proxylauncher.model.AppEntry;
import com.proxylauncher.model.LaunchRequest;
import com.proxylauncher.model.ProxyMode;
import com.proxylauncher.util.ArgumentTokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LauncherService {
    private static final List<String> PROXY_ENV_KEYS = List.of(
            "HTTP_PROXY",
            "HTTPS_PROXY",
            "ALL_PROXY",
            "http_proxy",
            "https_proxy",
            "all_proxy"
    );

    private final LaunchLogService launchLogService;

    public LauncherService() {
        this(new LaunchLogService());
    }

    LauncherService(LaunchLogService launchLogService) {
        this.launchLogService = launchLogService;
    }

    public Process launch(LaunchRequest request) throws IOException {
        LaunchPlan plan = buildLaunchPlan(request);
        ProcessBuilder builder = new ProcessBuilder(plan.command());
        if (plan.workingDirectory() != null) {
            builder.directory(plan.workingDirectory().toFile());
        }

        configureProxyEnvironment(builder.environment(), plan.proxyEnvironment());

        try {
            Process process = builder.start();
            launchLogService.logSuccess(request, plan, process.pid());
            return process;
        } catch (IOException exception) {
            launchLogService.logFailure(request, plan, exception);
            throw exception;
        }
    }

    List<String> buildCommand(AppEntry appEntry) {
        return buildStartCommand(appEntry);
    }

    LaunchPlan buildLaunchPlan(LaunchRequest request) {
        String resolvedProxy = resolveProxy(request);

        return new LaunchPlan(
                buildCommand(request.getAppEntry()),
                null,
                buildProxyEnvironment(resolvedProxy),
                "Windows shell start",
                resolvedProxy
        );
    }

    public Path getLogDirectory() {
        return launchLogService.getLogDirectory();
    }

    public Path getLogFile() {
        return launchLogService.getLogFile();
    }

    private List<String> buildTargetCommand(AppEntry appEntry) {
        List<String> command = new ArrayList<>();
        command.add(appEntry.getExecutablePath());
        command.addAll(ArgumentTokenizer.tokenize(appEntry.getArguments()));
        return command;
    }

    private List<String> buildStartCommand(AppEntry appEntry) {
        return List.of("cmd.exe", "/c", "start \"\" " + renderCommandForCmd(buildTargetCommand(appEntry)));
    }

    void applyProxyEnvironment(Map<String, String> environment, LaunchRequest request) {
        configureProxyEnvironment(environment, buildProxyEnvironment(resolveProxy(request)));
    }

    String resolveProxy(LaunchRequest request) {
        return switch (request.getProxyMode()) {
            case NONE -> "";
            case CUSTOM -> request.getCustomProxy();
            case DEFAULT -> request.getDefaultProxy();
        };
    }

    void configureProxyEnvironment(Map<String, String> environment, Map<String, String> proxyEnvironment) {
        for (String key : PROXY_ENV_KEYS) {
            environment.remove(key);
        }

        environment.putAll(proxyEnvironment);
    }

    Map<String, String> buildProxyEnvironment(String proxy) {
        Map<String, String> proxyEnvironment = new LinkedHashMap<>();
        if (proxy == null || proxy.isBlank()) {
            return proxyEnvironment;
        }

        for (String key : PROXY_ENV_KEYS) {
            proxyEnvironment.put(key, proxy);
        }
        return proxyEnvironment;
    }

    private String renderCommandForCmd(List<String> directCommand) {
        List<String> renderedTokens = new ArrayList<>();
        for (int index = 0; index < directCommand.size(); index++) {
            renderedTokens.add(quoteForCmd(directCommand.get(index), index == 0));
        }
        return renderedTokens.stream().collect(Collectors.joining(" "));
    }

    private String quoteForCmd(String token) {
        return quoteForCmd(token, false);
    }

    private String quoteForCmd(String token, boolean forceQuotes) {
        String safeToken = token == null ? "" : token;
        if (safeToken.isBlank()) {
            return "\"\"";
        }

        String escaped = safeToken.replace("\"", "\"\"");
        boolean requiresQuotes = forceQuotes || escaped.chars().anyMatch(character ->
                Character.isWhitespace(character)
                        || "&()[]{}^=;!'+,`~".indexOf(character) >= 0
        );

        return requiresQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
