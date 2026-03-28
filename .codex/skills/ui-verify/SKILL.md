---
name: ui-verify
description: Verify Proxy Launcher JavaFX UI changes. Use when Codex modifies FXML layouts, `app.css`, dialogs, controls, or controller-level UI wiring and needs to review layout clarity, interaction flow, status and error messaging, config persistence, and the behavior of `DEFAULT`, `CUSTOM`, and `NONE` launch modes.
---

# UI Verify

## Core Workflow

1. Open the changed FXML, controller, and stylesheet files and identify the controls or flows that moved.
2. Check whether the main window and dialog layout still read clearly at a normal desktop size.
3. Walk the add, edit, delete, and launch flows in code or in the running UI.
4. Check whether empty states, disable states, helper text, and status messages explain the next action.
5. Check whether config values load through `ConfigService` and save back through the same service path.
6. Check whether launch requests still flow through `LauncherService`.
7. Check whether validation still flows through `ValidationService`.
8. Check whether `DEFAULT`, `CUSTOM`, and `NONE` still map to the intended proxy behavior.

## Load References Only When Needed

- Read [references/ui-review-surfaces.md](references/ui-review-surfaces.md) when you need the concrete files, expected flows, or review heuristics for Proxy Launcher UI changes.
- Use that reference before reviewing a layout-only patch, a dialog change, or any edit that touches launch-mode behavior.
