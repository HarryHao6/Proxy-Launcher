# Release Surfaces

## Files To Review

- `README.md`
- `pom.xml`
- `CHANGELOG.md`
- `.github/workflows/ci.yml`
- `scripts/test.ps1`
- `scripts/package.ps1`
- `scripts/package-native.ps1`
- `scripts/package-app-image.ps1`
- `scripts/package-exe.ps1`

## Expected Commands

- Baseline verification: `.\mvnw.cmd -q test`
- Distributable jars: `.\scripts\package.ps1`
- Native app-image: `.\scripts\package-app-image.ps1`
- Native installer: `.\scripts\package-exe.ps1`

## Output And Packaging Expectations

- `scripts/package.ps1` should populate `dist/`.
- Native packaging scripts should write to `native-dist/` subdirectories.
- `pom.xml` should remain aligned with artifact names, version, and `com.proxylauncher.LauncherMain`.
- CI should exercise the same run or packaging path the docs describe.

## Native Packaging Constraints

- Use JDK 17+.
- `jpackage` must be available for native packaging.
- WiX Toolset 3.x is required for Windows `exe` installers.
- Treat missing prerequisites as explicit release blockers or skipped checks.

## MVP Claims To Recheck

- Application add, edit, and delete flow.
- Global default proxy storage.
- `DEFAULT`, `CUSTOM`, and `NONE` launch modes.
- Proxy environment injection into the child process.
- JSON persistence.
- Executable path and proxy validation.
