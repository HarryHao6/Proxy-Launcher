---
name: ui-modernization
description: Modernize or restyle Proxy Launcher JavaFX screens without rewriting their core behavior. Use when Codex needs to improve FXML layout, spacing, visual hierarchy, list-cell presentation, dialog structure, CSS-backed styling, or interaction clarity while preserving existing config, validation, and launch flows.
---

# UI Modernization

## Core Workflow

1. Read the affected FXML, controller, and `app.css` before editing.
2. State the intended layout structure and the UI files that will change.
3. Preserve the current launch, save, and validation behavior unless the task explicitly asks for behavior changes.
4. Move styling into `src/main/resources/com/proxylauncher/ui/app.css` instead of expanding inline styles.
5. Strengthen grouping, spacing, and action hierarchy before inventing new UI affordances.
6. Keep controller edits limited to UI state, selection flow, and view coordination.
7. Verify the final layout still reads clearly on the existing desktop window size.

## Load References Only When Needed

- Read [references/ui-surfaces.md](references/ui-surfaces.md) when you need the shared JavaFX files, style classes, or controller boundary reminders.
- Use that reference before introducing new CSS classes, reworking list or dialog layout, or touching both FXML and controller code.
