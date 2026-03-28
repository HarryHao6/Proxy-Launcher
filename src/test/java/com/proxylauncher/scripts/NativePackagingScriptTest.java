package com.proxylauncher.scripts;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        String output = evaluateNativeVersion("0.1.0-SNAPSHOT");
        assertTrue(
                SNAPSHOT_NATIVE_VERSION_PATTERN.matcher(output).matches(),
                "Snapshot native version should be a four-part numeric Windows installer version, but was: " + output
        );
    }

    @Test
    public void releaseNativeVersionSortsAboveSnapshotsOfTheSameBaseVersion() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name").toLowerCase().contains("win"));

        int[] snapshotVersion = parseVersion(evaluateNativeVersion("0.1.0-SNAPSHOT"));
        int[] releaseVersion = parseVersion(evaluateNativeVersion("0.1.0"));

        assertEquals(4, snapshotVersion.length, "Snapshot native version should have four numeric segments.");
        assertEquals(4, releaseVersion.length, "Release native version should have four numeric segments.");
        assertTrue(
                compareVersions(releaseVersion, snapshotVersion) > 0,
                "Release installer version must be greater than snapshot installer versions of the same base version."
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

    private String evaluateNativeVersion(String projectVersion) throws Exception {
        Path tempRoot = Files.createTempDirectory("native-version-test");
        Files.copy(Path.of("scripts", "common.ps1"), tempRoot.resolve("common.ps1"));
        Files.writeString(
                tempRoot.resolve("pom.xml"),
                """
                        <project>
                          <version>%s</version>
                        </project>
                        """.formatted(projectVersion).trim(),
                StandardCharsets.UTF_8
        );

        Process process = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                "& { Set-Location '" + tempRoot.toAbsolutePath() + "'; . .\\common.ps1; Get-NativeAppVersion }"
        ).redirectErrorStream(true).start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        int exitCode = process.waitFor();
        assertTrue(exitCode == 0, "PowerShell should return successfully when evaluating Get-NativeAppVersion.");
        return output;
    }

    private int[] parseVersion(String version) {
        return Arrays.stream(version.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private int compareVersions(int[] left, int[] right) {
        int maxLength = Math.max(left.length, right.length);
        for (int index = 0; index < maxLength; index++) {
            int leftPart = index < left.length ? left[index] : 0;
            int rightPart = index < right.length ? right[index] : 0;
            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }
        return 0;
    }
}
