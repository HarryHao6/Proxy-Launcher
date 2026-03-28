package com.proxylauncher.model;

import java.util.List;

public class AppConfig {
    private String defaultProxy = "";
    private List<AppEntry> appEntries = List.of();

    public AppConfig() {
    }

    public String getDefaultProxy() {
        return defaultProxy;
    }

    public void setDefaultProxy(String defaultProxy) {
        this.defaultProxy = defaultProxy == null ? "" : defaultProxy;
    }

    public List<AppEntry> getAppEntries() {
        return appEntries;
    }

    public void setAppEntries(List<AppEntry> appEntries) {
        this.appEntries = appEntries == null ? List.of() : List.copyOf(appEntries);
    }
}
