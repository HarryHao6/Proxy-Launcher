# AGENTS

## Project Goal

Build Proxy Launcher as a maintainable Windows desktop product repository that is suitable for long-term agent collaboration.

## Tech Stack

- Java 17+
- JavaFX
- Maven
- Jackson
- JUnit 5
- jpackage

## Layer Rules

- `model`: data structures and enums only
- `service`: business logic, persistence, launch behavior, validation orchestration
- `ui`: JavaFX controllers and UI coordination only
- `util`: pure helper logic such as path resolution and argument parsing

## Mandatory Constraints

- Do not place business logic directly in JavaFX controllers
- Route all config loading and saving through `ConfigService`
- Route all process launching through `LauncherService`
- Keep validation centralized in `ValidationService` or a dedicated validator
- Prefer the smallest working skeleton first, then fill in behavior and tests
- In test code, declare test classes and test methods with explicit `public` visibility for style consistency

## Validation Before Finishing

- Run the relevant tests when tests exist
- Otherwise run at least `mvn -q -DskipTests compile`
- Manually smoke test JavaFX UI changes when possible
- Keep `README.md` commands aligned with the actual project setup

## Logic Placement

- `AppEntry`: one launchable application entry
- `AppConfig`: default proxy and application list
- `ProxyMode`: launch mode enum
- `ConfigService`: config path, load, save, default config creation, corruption fallback
- `LauncherService`: `ProcessBuilder` launch and proxy environment injection
- `ValidationService`: executable path and proxy format validation
- `MainController`: UI event wiring, selection state, user-facing status messages
