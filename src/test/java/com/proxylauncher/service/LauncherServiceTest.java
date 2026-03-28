package com.proxylauncher.service;

import com.proxylauncher.model.AppEntry;
import com.proxylauncher.model.LaunchRequest;
import com.proxylauncher.model.ProxyMode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LauncherServiceTest {
    private final LauncherService launcherService = new LauncherService();

    @Test
    public void buildsCommandWithTokenizedArguments() {
        AppEntry appEntry = new AppEntry("Demo", "C:\\Tools\\demo.exe", "--profile \"Proxy Test\" --verbose", "");

        List<String> command = launcherService.buildCommand(appEntry);

        assertEquals(
                List.of("C:\\Tools\\demo.exe", "--profile", "Proxy Test", "--verbose"),
                command
        );
    }

    @Test
    public void appliesDefaultProxyToAllExpectedEnvironmentVariables() {
        Map<String, String> environment = new HashMap<String, String>();
        LaunchRequest request = new LaunchRequest(
                new AppEntry("Demo", "C:\\Tools\\demo.exe", "", ""),
                ProxyMode.DEFAULT,
                "http://127.0.0.1:7890",
                ""
        );

        launcherService.applyProxyEnvironment(environment, request);

        assertEquals("http://127.0.0.1:7890", environment.get("HTTP_PROXY"));
        assertEquals("http://127.0.0.1:7890", environment.get("HTTPS_PROXY"));
        assertEquals("http://127.0.0.1:7890", environment.get("ALL_PROXY"));
        assertEquals("http://127.0.0.1:7890", environment.get("http_proxy"));
        assertEquals("http://127.0.0.1:7890", environment.get("https_proxy"));
        assertEquals("http://127.0.0.1:7890", environment.get("all_proxy"));
    }

    @Test
    public void appliesCustomProxyWhenRequested() {
        Map<String, String> environment = new HashMap<String, String>();
        LaunchRequest request = new LaunchRequest(
                new AppEntry("Demo", "C:\\Tools\\demo.exe", "", ""),
                ProxyMode.CUSTOM,
                "http://127.0.0.1:7890",
                "socks5://127.0.0.1:1080"
        );

        launcherService.applyProxyEnvironment(environment, request);

        assertEquals("socks5://127.0.0.1:1080", environment.get("HTTP_PROXY"));
        assertEquals("socks5://127.0.0.1:1080", environment.get("all_proxy"));
    }

    @Test
    public void removesProxyEnvironmentVariablesWhenModeIsNone() {
        Map<String, String> environment = new HashMap<String, String>();
        environment.put("HTTP_PROXY", "http://old:1");
        environment.put("HTTPS_PROXY", "http://old:1");
        environment.put("ALL_PROXY", "http://old:1");
        environment.put("http_proxy", "http://old:1");
        environment.put("https_proxy", "http://old:1");
        environment.put("all_proxy", "http://old:1");

        LaunchRequest request = new LaunchRequest(
                new AppEntry("Demo", "C:\\Tools\\demo.exe", "", ""),
                ProxyMode.NONE,
                "http://127.0.0.1:7890",
                "socks5://127.0.0.1:1080"
        );

        launcherService.applyProxyEnvironment(environment, request);

        assertFalse(environment.containsKey("HTTP_PROXY"));
        assertFalse(environment.containsKey("HTTPS_PROXY"));
        assertFalse(environment.containsKey("ALL_PROXY"));
        assertFalse(environment.containsKey("http_proxy"));
        assertFalse(environment.containsKey("https_proxy"));
        assertFalse(environment.containsKey("all_proxy"));
    }
}
