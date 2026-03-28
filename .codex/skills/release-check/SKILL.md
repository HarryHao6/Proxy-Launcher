---
name: release-check
description: Run a release-readiness check for Proxy Launcher. Use when Codex prepares a tag, package, installer, changelog update, or GitHub publication and needs to verify README commands, version consistency, CI and packaging scripts, native-packaging prerequisites, and whether implemented behavior still matches MVP claims.
---

# Release Check

## Core Workflow

1. Read `README.md`, `pom.xml`, `CHANGELOG.md`, `.github/workflows/ci.yml`, and the release-facing scripts under `scripts/`.
2. Confirm the documented run, test, and packaging commands still match the repository.
3. Confirm the project version, artifact names, entrypoint class, and packaging output paths are consistent across docs and scripts.
4. Compare README MVP claims against the implemented code paths and tests.
5. Run the relevant validation commands for the release scope.
6. Separate hard release blockers from follow-up polish before reporting.

## Load References Only When Needed

- Read [references/release-surfaces.md](references/release-surfaces.md) when you need the exact release-facing files, commands, artifact expectations, or native-packaging prerequisites.
- Use that reference before validating a package, installer, changelog, or GitHub release draft.
