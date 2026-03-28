# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

- Fix Windows installer version ordering so release installers sort above older snapshot installers
- Keep the published EXE asset name semver-friendly while using a higher internal MSI version for upgrades

## [0.1.0] - 2026-03-28

- Bootstrap Proxy Launcher as a JavaFX desktop MVP repository
- Add JSON persistence, validation, tests, packaging scripts, and GitHub CI
- Migrate the project to Java 17 with OpenJFX-based runtime setup
- Support parallel Windows release channels for portable `app-image` and installer `exe` distribution
