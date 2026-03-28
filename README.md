# Proxy Launcher

Proxy Launcher is a Windows desktop launcher built with Java, JavaFX, and Maven.
It starts a target application with optional proxy environment variables injected into the child process.

This repository is primarily used to experience Vibe Coding through a small but real feature.
The scope is intentionally compact, while the repository is still structured for long-term iteration, maintenance, testing, packaging, release preparation, and reusable skills.

## What It Does

- Manage a saved list of launchable applications
- Reorder saved applications with drag and drop
- Store a global default proxy
- Launch an application in `DEFAULT`, `CUSTOM`, or `NONE` proxy mode
- Start applications through `cmd.exe /c start` for better Windows app compatibility
- Inject `HTTP_PROXY`, `HTTPS_PROXY`, `ALL_PROXY` and lowercase variants into the child process environment
- Persist application data and default proxy settings to a local JSON config file
- Write launch diagnostics to a local log file for troubleshooting
- Validate executable paths and basic proxy formats before launch

## Important Product Boundary

Proxy Launcher only provides proxy values through environment variables for the launched child process.
It does not force all applications to use the proxy.
Whether the proxy is actually used depends on whether the target application reads those environment variables.

## Tech Stack

- Java 17+
- JavaFX
- Maven
- Jackson
- JUnit 5
- jpackage

## Local Development

Prerequisites:

- JDK 17 or above
- Maven 3.9+ or the included Maven Wrapper

Use a JDK, not a JRE.

Run the desktop app:

```powershell
.\mvnw.cmd javafx:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

Useful helper scripts:

- `scripts/run-dev.ps1`
- `scripts/test.ps1`
- `scripts/package.ps1`
- `scripts/package-native.ps1`
- `scripts/package-app-image.ps1`
- `scripts/package-exe.ps1`

Build distributable jars:

```powershell
.\scripts\package.ps1
```

This writes the generated jars and a copy of `README.md` into `dist/`.

## Release Channels

Proxy Launcher keeps two Windows release channels in parallel.

### 1. Portable Channel

Use this when you want a no-install delivery.
Users extract a packaged directory and run `ProxyLauncher.exe` directly.

Build it with:

```powershell
.\scripts\package-app-image.ps1
```

Output:

- `native-dist/app-image/ProxyLauncher/`

Typical release flow:

1. Run the packaging script
2. Zip `native-dist/app-image/ProxyLauncher/`
3. Publish the zip as the portable download

### 2. Installer Channel

Use this when you want a standard Windows installer experience.
Users run an installer `exe`, then launch Proxy Launcher from the installed location.

Build it with:

```powershell
.\scripts\package-exe.ps1
```

Output:

- `native-dist/exe/ProxyLauncher-<version>.exe`

Additional requirement:

- WiX Toolset 3.x must be installed so `jpackage` can produce the Windows installer
- Windows installers use an internal four-part version so stable releases sort above prior snapshot installers of the same release line
- The published installer filename still follows the project semver from `pom.xml`

### Generic Native Wrapper

If you prefer one generic command, the repository also keeps:

```powershell
.\scripts\package-native.ps1
```

It defaults to `app-image` and also accepts `-Type exe`.

## Repository Notes

- Maven Wrapper is included via `mvnw` and `mvnw.cmd`
- GitHub Actions CI is defined in `.github/workflows/ci.yml`
- The project ships with `AGENTS.md` and reusable skills under `.codex/skills/`
- `CHANGELOG.md` tracks release-facing repository changes

## Roadmap

- Add manual UI smoke verification notes and screenshots
- Add app icon and polish release assets
- Add GitHub release workflow automation
