package com.proxylauncher.scripts;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativePackagingScriptTest {
    private static final Pattern SNAPSHOT_NATIVE_VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$");

    @Test
    public void packageNativeScriptDefinesStableWindowsUpgradeUuid() throws IOException {
        String script = Files.readString(Path.of("scripts", "package-native.ps1"), StandardCharsets.UTF_8);

        assertTrue(
                script.contains("--win-upgrade-uuid"),
                "EXE packaging should define a stable win-upgrade-uuid so upgrades can replace existing installs."
        );
    }

    @Test
    public void snapshotBuildsProduceFourPartNativeVersionForWindowsInstallers() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name").toLowerCase().contains("win"));

        Process process = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                "& { Set-Location '" + Path.of("").toAbsolutePath() + "'; . .\\scripts\\common.ps1; Get-NativeAppVersion }"
        ).redirectErrorStream(true).start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        int exitCode = process.waitFor();

        assertTrue(exitCode == 0, "PowerShell should return successfully when evaluating Get-NativeAppVersion.");
        assertTrue(
                SNAPSHOT_NATIVE_VERSION_PATTERN.matcher(output).matches(),
                "Snapshot native version should be a four-part numeric Windows installer version, but was: " + output
        );
    }

    @Test
    public void exePackagingCleansPreviousInstallerArtifactsBeforeBuilding() throws IOException {
        String script = Files.readString(Path.of("scripts", "package-native.ps1"), StandardCharsets.UTF_8);

        assertTrue(
                script.contains("Get-ChildItem $nativeDir -Filter '*.exe'"),
                "EXE packaging should remove previous installer files before writing a new installer."
        );
    }
}
