# UI Surfaces

## Shared Files

- `src/main/resources/com/proxylauncher/ui/main-view.fxml`
- `src/main/resources/com/proxylauncher/ui/app-entry-dialog.fxml`
- `src/main/resources/com/proxylauncher/ui/app.css`
- `src/main/java/com/proxylauncher/ui/MainController.java`
- `src/main/java/com/proxylauncher/ui/AppEntryDialogController.java`

## Reuse Existing Style Language

- Containers: `panel-card`, `launch-card`, `status-panel`
- Titles and copy: `page-title`, `section-title`, `section-subtitle`, `helper-text`, `hint-text`
- Actions: `primary-button`, `secondary-button`, `danger-button`
- Status and selection: `state-pill`, `state-pill-active`, `state-pill-empty`, `status-info`, `status-success`, `status-warning`, `status-error`
- Lists and empty states: `app-list-view`, `app-list-cell-content`, `empty-state-label`

## Behavior Boundaries

- Leave config persistence in `ConfigService`.
- Leave launching in `LauncherService`.
- Leave validation in `ValidationService`.
- Keep `MainController` and `AppEntryDialogController` focused on UI state, dialog flow, and status text.

## Interaction Areas To Preserve

- Application list selection and empty state.
- App entry dialog save and cancel flow.
- Default proxy, custom proxy, and mode switching area.
- Launch action and status feedback area.
