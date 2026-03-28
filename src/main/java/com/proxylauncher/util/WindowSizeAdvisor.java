package com.proxylauncher.util;

public final class WindowSizeAdvisor {
    private static final double EDGE_MARGIN = 100;
    private static final double MIN_WIDTH = 1060;
    private static final double MAX_WIDTH = 1420;
    private static final double MIN_HEIGHT = 620;
    private static final double MAX_HEIGHT = 880;

    private WindowSizeAdvisor() {
    }

    public static WindowSize recommend(double screenWidth, double screenHeight) {
        int width = (int) Math.round(clamp(screenWidth - EDGE_MARGIN, MIN_WIDTH, MAX_WIDTH));
        int height = (int) Math.round(clamp(screenHeight - EDGE_MARGIN, MIN_HEIGHT, MAX_HEIGHT));
        return new WindowSize(width, height);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record WindowSize(int width, int height) {
    }
}
