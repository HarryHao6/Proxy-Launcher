package com.proxylauncher.model;

public class AppEntry {
    private String name = "";
    private String executablePath = "";
    private String arguments = "";
    private String notes = "";

    public AppEntry() {
    }

    public AppEntry(String name, String executablePath, String arguments, String notes) {
        this.name = safe(name);
        this.executablePath = safe(executablePath);
        this.arguments = safe(arguments);
        this.notes = safe(notes);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = safe(name);
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = safe(executablePath);
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = safe(arguments);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = safe(notes);
    }

    public AppEntry copy() {
        return new AppEntry(name, executablePath, arguments, notes);
    }

    @Override
    public String toString() {
        return name.isBlank() ? executablePath : name;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
