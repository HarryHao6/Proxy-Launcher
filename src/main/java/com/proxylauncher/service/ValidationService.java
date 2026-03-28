package com.proxylauncher.service;

import com.proxylauncher.model.AppEntry;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ValidationService {
    private static final Set<String> SUPPORTED_PROXY_SCHEMES = Set.of("http", "https", "socks5");

    public List<String> validateAppEntry(AppEntry appEntry) {
        List<String> errors = new ArrayList<>();

        if (isBlank(appEntry.getName())) {
            errors.add("Application name cannot be empty.");
        }

        validateExecutablePath(appEntry.getExecutablePath()).ifPresent(errors::add);
        return errors;
    }

    public Optional<String> validateExecutablePath(String executablePath) {
        if (isBlank(executablePath)) {
            return Optional.of("Executable path cannot be empty.");
        }

        Path path = Path.of(executablePath.trim());
        if (Files.notExists(path)) {
            return Optional.of("Executable path does not exist.");
        }
        if (Files.isDirectory(path)) {
            return Optional.of("Executable path must point to a file, not a directory.");
        }

        return Optional.empty();
    }

    public Optional<String> validateProxy(String proxyValue) {
        if (isBlank(proxyValue)) {
            return Optional.empty();
        }

        try {
            URI uri = new URI(proxyValue.trim());
            if (uri.getScheme() == null || !SUPPORTED_PROXY_SCHEMES.contains(uri.getScheme().toLowerCase())) {
                return Optional.of("Proxy must start with http://, https://, or socks5://.");
            }
            if (isBlank(uri.getHost())) {
                return Optional.of("Proxy must include a host.");
            }
            if (uri.getPort() < 1 || uri.getPort() > 65535) {
                return Optional.of("Proxy must include a valid port number.");
            }
            if ((uri.getPath() != null && !uri.getPath().isBlank() && !"/".equals(uri.getPath()))
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || uri.getUserInfo() != null) {
                return Optional.of("Proxy format must look like scheme://host:port.");
            }
            return Optional.empty();
        } catch (URISyntaxException exception) {
            return Optional.of("Proxy format must look like scheme://host:port.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
