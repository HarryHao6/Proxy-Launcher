package com.proxylauncher.model;

public enum ProxyMode {
    DEFAULT("Use Default Proxy"),
    CUSTOM("Use Custom Proxy"),
    NONE("Do Not Use Proxy");

    private final String label;

    ProxyMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
