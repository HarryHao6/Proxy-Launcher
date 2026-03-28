# UI Review Surfaces

## Files To Review Together

- `src/main/resources/com/proxylauncher/ui/main-view.fxml`
- `src/main/resources/com/proxylauncher/ui/app-entry-dialog.fxml`
- `src/main/resources/com/proxylauncher/ui/app.css`
- `src/main/java/com/proxylauncher/ui/MainController.java`
- `src/main/java/com/proxylauncher/ui/AppEntryDialogController.java`

## Flows To Recheck

- Add, edit, and delete application flow.
- List selection and empty-state behavior.
- Default proxy commit behavior.
- Custom proxy enable and disable behavior.
- Launch action and resulting status message flow.

## Ownership Checks

- Config load and save should stay on the `ConfigService` path.
- Launch requests should still flow through `LauncherService`.
- Validation should still flow through `ValidationService`.
- Controller code should stay focused on UI coordination.

## Launch Mode Expectations

- `DEFAULT`: use the saved default proxy.
- `CUSTOM`: use the custom proxy field for the current launch only.
- `NONE`: launch without proxy variables.

## Reporting Expectations

- List findings first, ordered by severity.
- Prioritize regressions in flow, status text, or ownership boundaries over cosmetic nits.
- State when a conclusion came from code review rather than a manual smoke test.
