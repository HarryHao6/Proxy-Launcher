package com.proxylauncher.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WindowSizeAdvisorTest {
    @Test
    public void recommendsAnInitialWindowSizeBasedOnScreenBounds() {
        WindowSizeAdvisor.WindowSize windowSize = WindowSizeAdvisor.recommend(1920, 1080);

        assertEquals(1420, windowSize.width());
        assertEquals(880, windowSize.height());
    }

    @Test
    public void keepsTheInitialWindowSizeWithinTheAvailableScreenBounds() {
        WindowSizeAdvisor.WindowSize windowSize = WindowSizeAdvisor.recommend(1280, 720);

        assertEquals(1180, windowSize.width());
        assertEquals(620, windowSize.height());
    }
}
