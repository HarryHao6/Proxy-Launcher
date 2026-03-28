package com.proxylauncher.model;

public class LaunchRequest {
    private final AppEntry appEntry;
    private final ProxyMode proxyMode;
    private final String defaultProxy;
    private final String customProxy;

    public LaunchRequest(AppEntry appEntry, ProxyMode proxyMode, String defaultProxy, String customProxy) {
        this.appEntry = appEntry;
        this.proxyMode = proxyMode;
        this.defaultProxy = defaultProxy == null ? "" : defaultProxy;
        this.customProxy = customProxy == null ? "" : customProxy;
    }

    public AppEntry getAppEntry() {
        return appEntry;
    }

    public ProxyMode getProxyMode() {
        return proxyMode;
    }

    public String getDefaultProxy() {
        return defaultProxy;
    }

    public String getCustomProxy() {
        return customProxy;
    }
}

