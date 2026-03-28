---
name: feature-implementation
description: Implement or extend Proxy Launcher features in small, safe increments. Use when Codex needs to add product behavior, expand config or launch flows, update JavaFX interactions, or wire new repository capabilities while preserving the project's layer rules, service boundaries, and validation flow.
---

# Feature Implementation

## Core Workflow

1. Restate the exact user-visible behavior before editing.
2. Read the current code path and identify which layer owns the change.
3. List the concrete files to edit and keep the patch scoped to that list.
4. Land the smallest working skeleton first.
5. Fill in the behavior only after the skeleton compiles or clearly hangs together.
6. Add or update tests for changed service or utility behavior.
7. Update docs when commands or user-facing behavior change.
8. Run validation before finishing and inspect failures instead of hand-waving them away.

## Load References Only When Needed

- Read [references/repository-boundaries.md](references/repository-boundaries.md) when you need the Proxy Launcher layer rules, file ownership, or validation expectations.
- Use that reference before changing `model`, `service`, `ui`, or `util` boundaries, or when a feature touches config, launch, or validation flow.
