# Repository Boundaries

## Layer Ownership

- `model`: data structures and enums only.
- `service`: business logic, persistence, launch behavior, validation orchestration.
- `ui`: JavaFX controllers and UI coordination only.
- `util`: pure helper logic such as path resolution and argument parsing.

## Primary Types And Owners

- `AppConfig`: default proxy and saved application list.
- `AppEntry`: persisted application metadata.
- `LaunchRequest`: launch input assembled in the UI and consumed by `LauncherService`.
- `ProxyMode`: `DEFAULT`, `CUSTOM`, `NONE`.
- `ConfigService`: config path, load, save, default creation, corruption fallback.
- `LauncherService`: `ProcessBuilder` launch and proxy environment injection.
- `ValidationService`: executable path and proxy format validation.
- `MainController`: selection state, status messages, config sync, launch initiation.
- `AppEntryDialogController`: dialog-only app entry editing.

## Common Change Mapping

- Put persistence or corruption handling in `ConfigService`.
- Put launch environment changes in `LauncherService`.
- Put proxy and executable checks in `ValidationService`.
- Put new config or launch state shape in `model`.
- Put selection state, disable state, and status copy in `ui`.
- Put parsing and path helpers in `util`.

## Validation Expectations

- Add or update targeted tests for changed service and utility behavior.
- Declare new test classes and test methods with explicit `public` visibility.
- Run `.\mvnw.cmd test` when a change spans multiple layers or alters behavior.
- Run at least `.\mvnw.cmd -q -DskipTests compile` when no relevant tests exist.
- Report changed files, validation run, and residual risk at close-out.
