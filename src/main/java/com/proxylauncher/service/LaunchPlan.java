package com.proxylauncher.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

record LaunchPlan(List<String> command,
                  Path workingDirectory,
                  Map<String, String> proxyEnvironment,
                  String launchStrategy,
                  String resolvedProxy) {
    LaunchPlan {
        command = List.copyOf(command);
        proxyEnvironment = Map.copyOf(proxyEnvironment);
    }
}
